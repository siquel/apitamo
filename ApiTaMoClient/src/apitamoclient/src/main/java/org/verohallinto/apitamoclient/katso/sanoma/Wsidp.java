package org.verohallinto.apitamoclient.katso.sanoma;

import org.verohallinto.apitamoclient.apu.Apuri;
import org.verohallinto.apitamoclient.apu.LokiApu;
import org.verohallinto.apitamoclient.apu.SanomaApu;
import org.verohallinto.apitamoclient.dto.KatsoDto;
import org.verohallinto.apitamoclient.dto.SoapVastausDto;
import org.verohallinto.apitamoclient.yleiset.Nimiavaruus;
import org.verohallinto.apitamoclient.yleiset.Props;
import org.verohallinto.apitamoclient.yleiset.Vakiot;
import org.w3c.dom.Document;

import javax.xml.soap.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * <p>AuthnRequest-sanoman lähetys ja vastaussanoman käsittely.</p>
 * (c) 2014 Tietokarhu Oy
 * <p/>
 */
public class Wsidp {

    private static final Logger log = Logger.getLogger(Wsidp.class.getName());
    private static final String XPATH_STATUS_CODE = "samlp:Response/samlp:Status/samlp:StatusCode/@Value";
    private static final String XPATH_SAML_ASSERTION = "samlp:Response/saml:Assertion";
    private static final String XPATH_IN_RESPONSE_TO = "samlp:Response/@InResponseTo";
    private static final String XPATH_TFI_KID = "saml:AttributeStatement/saml:Attribute[@Name='tfi.kid']";
    private static final String XPATH_ISSUER2 = "saml:Issuer/text()";
    private static final String XPATH_TFI_PERSONNAME = "saml:AttributeStatement/saml:Attribute[@Name='tfi.personname']";
    private static final String XPATH_TFI_VERSION = "saml:AttributeStatement/saml:Attribute[@Name='tfi.version']";
    private static final String XPATH_XML_SEC = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String XPATH_AUTHN_REQUEST_ID = "samlp:AuthnRequest/@ID";
    private static final String XPATH_ASSERTION_DEADLINE = "saml:Conditions/@NotOnOrAfter";

    /**
     * AuthnRespond-sanoman XPath-käsittelyssä käytettävät Namespace-määritykset
     */
    private static final Nimiavaruus NAMESPACE_CONTEXT;

    static {
        NAMESPACE_CONTEXT = new Nimiavaruus();
        NAMESPACE_CONTEXT.setNamespace("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
        NAMESPACE_CONTEXT.setNamespace("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        NAMESPACE_CONTEXT.setNamespace("ecp", "urn:oasis:names:tc:SAML:2.0:profiles:SSO:ecp");
        NAMESPACE_CONTEXT.setNamespace("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        NAMESPACE_CONTEXT.setNamespace("samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        NAMESPACE_CONTEXT.setNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
        NAMESPACE_CONTEXT.setNamespace("xs", "http://www.w3.org/2001/XMLSchema");
        NAMESPACE_CONTEXT.setNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        // SASLResponse-sanoman 'saml:Assertion'
        NAMESPACE_CONTEXT.setNamespace("sa", "urn:liberty:sa:2006-08");
        NAMESPACE_CONTEXT.setNamespace("wsa", "http://www.w3.org/2005/08/addressing");
        NAMESPACE_CONTEXT.setNamespace("disco", "urn:liberty:disco:2006-08");
        NAMESPACE_CONTEXT.setNamespace("sec", "urn:liberty:security:2006-08");
    }

    private Wsidp() {

    }

    /**
     * SASLRequest-sanoman lähetys.
     * </p>
     *
     * @param katso {@code KatsoDto} käyttäjän tiedot.
     * @return Vastaus sanoma AuthnRequest-pyyntöön.
     */
    public static SoapVastausDto laheta(final KatsoDto katso) {

        final SoapVastausDto dto = new SoapVastausDto();
        SOAPMessage vastaus;
        final XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(NAMESPACE_CONTEXT);

        try {
            final SOAPMessage pyynto = muodostaPyynto(katso, xp);

            if (Props.lokitaSanomat()) {
                LokiApu.lokitaSanoma(LokiApu.annaEtuliite(Vakiot.KATSO_PYYNTO_WSIDP) + "Pyynto_" + System.currentTimeMillis() + ".xml", pyynto);
            }

            final String url = Props.getWsidpURL();
            vastaus = sendMessage(pyynto, url);

            if (Props.lokitaSanomat()) {
                LokiApu.lokitaSanoma(LokiApu.annaEtuliite(Vakiot.KATSO_PYYNTO_WSIDP) + "Vastaus_" + System.currentTimeMillis() + ".xml", vastaus);
            }

            final SOAPBody runko = vastaus.getSOAPBody();

            if (!runko.hasFault()) {
                // Käsitellään ja tarkastetaan vastaussanoma
                kasitteleVastaus(katso, vastaus, xp);
                katso.getOut().setAssertion(annaAssertio(vastaus));
                dto.setOk(true);
            } else {
                katso.getOut().setVirhe(SanomaApu.annaSanomanVirhe(vastaus));
            }
        } catch (Exception e) {
            katso.getOut().setVirhe(e.getMessage());
            vastaus = SanomaApu.annaVirhesanoma(Vakiot.SOAP_ENV_CLIENT, katso.getOut().getVirhe());
            log.severe("muodostaPyynto() - " + Apuri.annaPoikkeus(e));
        }

        dto.setSanoma(vastaus);

        return dto;
    }

    /**
     * AuthnRequest-sanoman muodostus.
     * </p>
     *
     * @param katso {@code KatsoDto} käyttäjän tiedot.
     * @return AuthnRequest-sanoma käyttäjän tiedoilla.
     * @throws RuntimeException AuthnRequest-sanoman allekirjoitus epäonnistui.
     */
    private static SOAPMessage muodostaPyynto(final KatsoDto katso, final XPath xp)
            throws SOAPException, XPathExpressionException, ParseException {

        final SOAPElement se = (SOAPElement)katso.getIn().getMessage().getSOAPBody().getFirstChild();
        final Document doc = SanomaApu.elementtiDokumentiksi(se);
        katso.setAuthnMsgID(xp.evaluate(XPATH_AUTHN_REQUEST_ID, doc));

        final SOAPMessage sanoma = katso.getIn().getMessage();
        final SOAPEnvelope envelope = sanoma.getSOAPPart().getEnvelope();

        // Poistetaan alkuperäinen header
        final SOAPHeader he = envelope.getHeader();
        he.detachNode();

        //  Lisätään uusi header
        final SOAPHeader uusiHeader = envelope.addHeader();
        uusiHeader.setPrefix("SOAP-ENV");

        // Lisätään otsikkoon 'Security'-elementti.
        final Name nimi = SOAPFactory.newInstance().createName("Security", "", XPATH_XML_SEC);

        // Lisätään assertion SASL-vastauksesta
        final SOAPHeaderElement sec = uusiHeader.addHeaderElement(nimi);
        sec.addChildElement(katso.getIn().getSaslAssertion());

        return sanoma;
    }

    /**
     * AuthnRespond-sanoman tarkastus.
     * </p>
     *
     * @param katso   {@code KatsoDto} Katso-tunnistautumisessa käytettävät tiedot..
     * @param vastaus {@code SOAPMessage} vastaanotettu sanoma.
     * @param xp      {@code XPath} XPath-instanssi.
     * @throws javax.xml.xpath.XPathExpressionException Sanoman XPath-käsittelyssä tapahtui virhe.
     * @throws javax.xml.soap.SOAPException            Sanoman SOAPBody-elementin haussa tapahtui virhe.
     * @throws java.text.ParseException           Päiväyksen parsinta epäonnistui.
     * @throws RuntimeException         Vastaussanoman käsittelyssä tapahtui virhe.
     */
    private static void kasitteleVastaus(final KatsoDto katso, final SOAPMessage vastaus, final XPath xp)
            throws SOAPException, XPathExpressionException, ParseException {

        // Tarkastetaan allekirjooitus
        final SOAPElement se = (SOAPElement)vastaus.getSOAPBody().getFirstChild();
        final Document doc = SanomaApu.elementtiDokumentiksi(se);

        // Tarkastetaan lähetetyn ja vastaanotetun sanoman id:t
        final String responseTo = xp.evaluate(XPATH_IN_RESPONSE_TO, doc);

        if (responseTo != null && responseTo.equals(katso.getAuthnMsgID())) {
            // Tarkastetaan paluukoodi
            samlpStatusCodeOk(katso.getIn().getKieli(), doc, xp);
            // Tarkastetaan Assertion-elementti
            samlAssertionOk(katso, doc, xp);
        } else {
            // txt.205=AuthnRespond: Tunnistautuminen epäonnistui. Virheellinen {0}: Odotettu='{1}', palautettu='{2}'.
            String[] args = new String[]{"ID", katso.getAuthnMsgID(), responseTo};
            throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 205, args));
        }

    }

    /**
     * AuthnRespond-sanoman 'samlp:StatusCode'-elemntin arvon tutkinta.
     * </p>
     *
     * @param kieli {@code String} ilmoitusten kielikoodi.
     * @param doc   {@code Document} vastaanotetun sanoman SOAPBody-elementti XML-dokumenttinä.
     * @param xp    {@code XPath} XPath-instanssi.
     * @throws javax.xml.xpath.XPathExpressionException Sanoman XPath-käsittelyssä tapahtui virhe.
     * @throws RuntimeException         Tunnistautuminen epäonnistui tai Sanoman 'samlp:StatusCode'-elementin 'Value'-attribuutti puuttuu tai se on tyhjä.
     */
    private static void samlpStatusCodeOk(final String kieli, final Document doc, final XPath xp)
            throws SOAPException, XPathExpressionException {

        final String status = xp.evaluate(XPATH_STATUS_CODE, doc);

        if (status != null) {
            if (!Vakiot.KATSO_SUCCESS.equalsIgnoreCase(status)) {
                // txt.206=AuthnRespond: Tunnistautuminen epäonnistui. Elermentti 'samlp:StatusCode'='{0}'.
                throw new RuntimeException(Props.teksti(kieli, 206, new String[]{status}));
            }
        } else {
            // txt.204=AuthnRespond-sanoman '{0}'-elementti tai -attribuutti puuttuu tai se on tyhjä.
            throw new RuntimeException(Props.teksti(kieli, 204, new String[]{XPATH_STATUS_CODE}));
        }
    }

    /**
     * AuthnRespond-sanoman 'saml:Assertion'-elementin tarkasta. Mahdollinen virhe palautetaan SoapVastausDto-objektissa.
     * </p>
     *
     * @param katso {@code KatsoDto} Katso-tunnistautumisessa käytettävät tiedot..
     * @param doc   {@code Document} vastaanotetun sanoman SOAPBody-elementti XML-dokumenttinä.
     * @param xp    {@code XPath} XPath-instanssi.
     * @throws javax.xml.xpath.XPathExpressionException Sanoman XPath-käsittelyssä tapahtui virhe.
     * @throws javax.xml.soap.SOAPException            Sanoman SOAPBody-elementin haussa tapahtui virhe.
     * @throws RuntimeException         Sanoman 'saml:Assertion'-elementti puuttuu tai se on tyhjä.
     */
    private static void samlAssertionOk(final KatsoDto katso, final Document doc, final XPath xp)
            throws XPathExpressionException, SOAPException, ParseException {

        final org.w3c.dom.Node n = (org.w3c.dom.Node)xp.evaluate(XPATH_SAML_ASSERTION, doc, XPathConstants.NODE);

        if (n != null && n.hasChildNodes()) {
            samlAttribute(katso, xp, n);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            katso.getOut().setAssertionDeadline(df.parse(xp.evaluate(XPATH_ASSERTION_DEADLINE, n)));
        } else {
            // txt.204=AuthnRespond-sanoman '{0}'-elementti tai -attribuutti puuttuu tai se on tyhjä.
            throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 204, new String[]{XPATH_SAML_ASSERTION}));
        }
    }

    /**
     * AuthnRespond-sanoman 'saml:Attribute'-elementtien käsittely.
     * </p>
     * <ul>Käsiteltävät attribuutit:
     * <li>tfi.kid</li>
     * <li>tfi.personname</li>
     * <li>tfi.version</li>
     * </ul>
     *
     * @param katso {@code KatsoDto} Katso-tunnistautumisessa käytettävät tiedot..
     * @param xp    {@code XPath} XPath-instanssi.
     * @param n     {@code Node} 'saml:Assertion'-elementti.
     * @throws javax.xml.xpath.XPathExpressionException Sanoman XPath-käsittelyssä tapahtui virhe.
     * @throws RuntimeException         Sanoman 'saml:Attribute'-elementin tietoja ei löydy tai tiedot ovat puutteelliset.
     */
    private static void samlAttribute(final KatsoDto katso, final XPath xp, final org.w3c.dom.Node n)
            throws XPathExpressionException {

        final String tfiKid = xp.evaluate(XPATH_TFI_KID, n);
        final String tfiPersonname = xp.evaluate(XPATH_TFI_PERSONNAME, n);
        final String tfiVersion = xp.evaluate(XPATH_TFI_VERSION, n);

        if (tfiKid != null && tfiPersonname != null) {
            katso.getOut().setTfiKid(tfiKid);
            katso.getOut().setTfiPersonname(tfiPersonname);
            katso.getOut().setTfiVersion(tfiVersion);
        } else {
            // txt.208=AuthnRespond-sanoman '{0}' tai '{1}' -elementtien tietoja ei löydy tai tiedot ovat puutteelliset.
            throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 208, new String[]{XPATH_TFI_KID, XPATH_TFI_PERSONNAME}));
        }

    }

    private static SOAPElement annaAssertio (final SOAPMessage msg) throws SOAPException {

        Iterator childElems = msg.getSOAPBody().getChildElements();

        SOAPElement child, child2;
        while(childElems.hasNext()){
            child = (SOAPElement) childElems.next();
            if (child.getNodeName().equalsIgnoreCase("samlp:Response")){
                Iterator childs = child.getChildElements();
                while (childs.hasNext()) {
                    child = (SOAPElement) childs.next();
                    if (child.getNodeName().equalsIgnoreCase("saml:Assertion")){
                        return child;
                    }
                }
            }
        }

        return null;
    }

    /**
    * SOAP sanoman lähetys.
    *
    * @param req    <code>SOAPMessage</code> Lähetettävä SOAP sanoma
    * @param osoite <code>String</code> Osoite, johon sanoma lähetetään
    * @return       <code>SOAPMessage</code> Vastaus lähetettyyn sanomaan
    */
    private static SOAPMessage sendMessage(SOAPMessage req, String osoite){
    	SOAPConnection connection = null;
        SOAPMessage vastaus = null;
    	try{
    		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
    		connection = soapConnectionFactory.createConnection();

    		URL endpoint = new URL(osoite);
            vastaus = connection.call(req, endpoint);
    	}catch(Exception e){
            log.severe("sendMessage() - " + Apuri.annaPoikkeus(e));
            vastaus = SanomaApu.annaVirhesanoma(Vakiot.SOAP_ENV_SERVER, e.getMessage());
    	}finally{
    		try{
    			connection.close();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}
        return vastaus;
    }
}
