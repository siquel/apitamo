package org.verohallinto.apitamoclient.katso.sanoma;

import org.verohallinto.apitamoclient.apu.Apuri;
import org.verohallinto.apitamoclient.apu.LokiApu;
import org.verohallinto.apitamoclient.apu.SanomaApu;
import org.verohallinto.apitamoclient.dto.KatsoDto;
import org.verohallinto.apitamoclient.dto.KatsoInDto;
import org.verohallinto.apitamoclient.dto.SoapVastausDto;
import org.verohallinto.apitamoclient.yleiset.Base64;
import org.verohallinto.apitamoclient.yleiset.Nimiavaruus;
import org.verohallinto.apitamoclient.yleiset.Props;
import org.verohallinto.apitamoclient.yleiset.Vakiot;

import javax.xml.soap.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * <p>SASLRequest-sanoman lähetys ja vastaussanoman käsittely.</p>
 * (c) 2013 Tietokarhu Oy
 * <p/>
 */
public class Sasl {

    private static final Logger log = Logger.getLogger(Sasl.class.getName());
    private static final String XPATH_SAML_ASSERTION = "sa:SASLResponse/wsa:EndpointReference/wsa:Metadata/disco:SecurityContext/sec:Token/saml:Assertion";
    private static final String XPATH_LU_STATUS = "sa:SASLResponse/lu:Status";
    private static final String XPATH_SA_DATA = "sa:SASLResponse/sa:Data/text()";
    private static final String XPATH_RELATES_TO = "wsa:RelatesTo/text()";

    /**
     * Sanoman XPath-käsittelyssä käytettävät Namespace-määritykset
     */
    private static final Nimiavaruus NAMESPACE_CONTEXT;

    static {
        NAMESPACE_CONTEXT = new Nimiavaruus();
        NAMESPACE_CONTEXT.setNamespace("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
        NAMESPACE_CONTEXT.setNamespace("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        NAMESPACE_CONTEXT.setNamespace("wsa", "http://www.w3.org/2005/08/addressing");
        NAMESPACE_CONTEXT.setNamespace("sbf", "urn:liberty:sb");
        NAMESPACE_CONTEXT.setNamespace("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        NAMESPACE_CONTEXT.setNamespace("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        NAMESPACE_CONTEXT.setNamespace("sa", "urn:liberty:sa:2006-08");
        NAMESPACE_CONTEXT.setNamespace("lu", "urn:liberty:util:2006-08");
        NAMESPACE_CONTEXT.setNamespace("disco", "urn:liberty:disco:2006-08");
        NAMESPACE_CONTEXT.setNamespace("sec", "urn:liberty:security:2006-08");
        NAMESPACE_CONTEXT.setNamespace("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        NAMESPACE_CONTEXT.setNamespace("ds", "http://www.w3.org/2000/09/xmldsig#");
    }

    private Sasl() {

    }

    /**
     * SASLRequest-pyyntösanoman lähetys.
     * </p>
     *
     * @param katso {@code KatsoDto} käyttäjän tiedot.
     * @return Vastaus sanoma SASLRequest-pyyntöön.
     */
    public static SoapVastausDto laheta(final KatsoDto katso) {

        final SoapVastausDto dto = new SoapVastausDto();
        SOAPMessage vastaus;

        try {
            // Muodostetaan lähetettävä sanoma
            final SOAPMessage pyynto = muodostaPyynto(katso);

            if (Props.lokitaSanomat()) {
                LokiApu.lokitaSanoma(LokiApu.annaEtuliite(Vakiot.KATSO_PYYNTO_SASL) + "Pyynto_" + System.currentTimeMillis() + ".xml", pyynto);
            }

            // Lähetetään sanoma
            final String url = Props.getSaslURL();
            vastaus = sendMessage(pyynto, url);

            if (Props.lokitaSanomat()) {
                LokiApu.lokitaSanoma(LokiApu.annaEtuliite(Vakiot.KATSO_PYYNTO_SASL) + "Vastaus_" + System.currentTimeMillis() + ".xml", vastaus);
            }

            final SOAPBody runko = vastaus.getSOAPBody();

            if (!runko.hasFault()) {
                final XPath xp = XPathFactory.newInstance().newXPath();
                xp.setNamespaceContext(NAMESPACE_CONTEXT);
                // Käsitellään ja tarkastetaan vastaussanoma
                kasitteleVastaus(katso, vastaus, xp);

                if (!katso.getOut().isOtpVirhe() && !katso.getOut().isSalasanaVirhe() && !katso.getOut().isKatsoIdVirhe()) {
                    katso.getIn().setSaslAssertion(annaAssertion(katso.getIn().getKieli(), runko, xp));
                    dto.setOk(true);
                }
            } else {
                katso.getOut().setVirhe(SanomaApu.annaSanomanVirhe(vastaus));
            }
        } catch (Exception e) {
            katso.getOut().setVirhe(e.getMessage());
            vastaus = SanomaApu.annaVirhesanoma(Vakiot.SOAP_ENV_CLIENT, katso.getOut().getVirhe());
            log.severe("laheta() - " + Apuri.annaPoikkeus(e));
        }

        dto.setSanoma(vastaus);

        return dto;
    }

    /**
     * SASLRequest-sanoman muodosta.
     * </p>
     *
     * @param katso {@code KatsoDto} käyttäjän tiedot.
     * @return SASLRequest-sanoma käyttäjän tiedoilla.
     * @throws RuntimeException Sanomapohjaa ei löydy.
     * @throws java.io.IOException      tunnistautumismerkkijonon koodaus epäonnistui.
     * @throws javax.xml.soap.SOAPException    Sanoman tallennus epäonnistui.
     */
    private static SOAPMessage muodostaPyynto(final KatsoDto katso) throws IOException, SOAPException {

        SOAPMessage sanoma;
        final String pohja = Props.annaKatsoSaslPohja();
        final String saData;
        final String authMethod;

        if (pohja != null) {
            authMethod = katso.getIn().getTunnistustapa() == Vakiot.KATSO_OTP ? "KATSO" : "PLAIN";
            katso.setSaslMsgID(Apuri.annaId(30));
            saData = annaTunnistautuminen(katso.getIn());
            final String[] ss = new String[]{katso.getSaslMsgID(), // wsa:MessageID
                                             Apuri.annaKatsoAikaleima(), // wsu:Created
                                             authMethod, // sa:SASLRequest
                                             saData}; // sa:Data

            if (!Apuri.onArvot(ss, 4)) {
                // txt.019=Sanomapohjan '{0}' parametrien määrä on virheellinen.
                throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 19, new String[]{"SASLRequest"}));
            }

            final String s = new MessageFormat(pohja).format(ss);

            sanoma = SanomaApu.merkkijonoSanomaksi(s);
        } else {
            // txt.101=SASLRequest-sanomapohjaa ei löydy.
            throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 102));
        }

        return sanoma;
    }

    /**
     * SASL-kirjautumisessa käytettävän tunnistusmerkkijonon muodostus.
     * </p>
     *
     * @param dto {@code TunnistusDto} Tunnistautumistiedot.
     * @return Tunnistautumismerkkijono tai virhetilanteessa null.
     * @throws java.io.IOException      tunnistautumismerkkijonon koodaus epäonnistui.
     * @throws RuntimeException Virheellinen tunnistatumistapa.
     */
    private static String annaTunnistautuminen(final KatsoInDto dto) throws IOException {

        String saData;

        switch (dto.getTunnistustapa()) {
            case Vakiot.KATSO_PWD:
                saData = Vakiot.KATSO_SASL_EROTIN +
                         "katsole:" +
                         dto.getKayttajatunnus() +
                         Vakiot.KATSO_SASL_EROTIN +
                         dto.getSalasana();
                break;
            case Vakiot.KATSO_OTP:
                saData = dto.getKayttajatunnus() +
                         Vakiot.KATSO_SASL_EROTIN +
                         dto.getSalasana() +
                         Vakiot.KATSO_SASL_EROTIN +
                         dto.getOtpSalasana();
                break;
            default:
                saData = null;
                break;
        }

        if (saData == null) {
            // txt.102=Virheellinen tunnistautumistapa: '{0}'.
            throw new RuntimeException(Props.teksti(dto.getKieli(), 102, new String[]{String.valueOf(dto.getTunnistustapa())}));
        }

        saData = Base64.encode64(saData);

        return saData;
    }

    /**
     * SASLRespond-sanoman sanoman käsittely.
     * </p>
     *
     * @param katso   {@code KatsoDto} käyttäjän tiedot.
     * @param vastaus {@code SOAPMessage} SASLRespond-sanoma.
     * @param xp      {@code XPath} XPath-instanssi.
     * @throws javax.xml.soap.SOAPException            SOAP-sanoman käsittelyvirhe.
     * @throws javax.xml.xpath.XPathExpressionException Sanoman XPath-käsittelyssä tapahtui virhe.
     * @throws java.io.IOException              base64-koodaus/dekoodaus epäonnistui.
     */
    private static void kasitteleVastaus(final KatsoDto katso, final SOAPMessage vastaus, final XPath xp)
            throws XPathExpressionException, SOAPException, IOException {

        // Käsitellään sanoman otsikkotiedot ja runko
        tarkastaOtsikot(katso, vastaus, xp);
    }

    /**
     * Vastaussanoman otsikkotietojen tarkastus.
     * </p>
     *
     * @param katso   {@code KatsoDto} käyttäjän tiedot.
     * @param vastaus {@code SOAPMessage} SASLRequest-vastaus.
     * @param xp      {@code XPath} XPath-instanssi.
     * @throws javax.xml.soap.SOAPException            SOAPHeader-elementin haku epäonnistui.
     * @throws javax.xml.xpath.XPathExpressionException Sanoman XPath-käsittelyssä tapahtui virhe.
     * @throws RuntimeException         Vastauksen sanoma-id ei täsmää lähetetyn sanoman id:hen.
     * @throws java.io.IOException              base64-dekoodaus epäonnistui.
     */
    private static void tarkastaOtsikot(final KatsoDto katso, final SOAPMessage vastaus, final XPath xp)
            throws SOAPException, XPathExpressionException, IOException {

         // Tarkastetaan, että lähetetyn sanoman id vastaa saadun vastauksen id:tä
        final String relatesTo = xp.evaluate(XPATH_RELATES_TO, vastaus.getSOAPHeader());

        if (relatesTo != null) {
            if (katso.getSaslMsgID() != null && katso.getSaslMsgID().equals(relatesTo)) {
                // Tarkastetaan statuskoodi
                kasitteleStatus(katso, vastaus, xp);
            } else {
                // txt.104=SASLRespond-sanoman '{0}'-elementin arvo ei vastaa odotettua arvoa: Odotettu='{1}', palautettu='{2}'.
                String[] args = new String[]{XPATH_RELATES_TO, katso.getSaslMsgID(), relatesTo};
                throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 104, args));
            }
        } else {
            // txt.103=SASLRespond-sanoman '{0}'-elementtiä tai -attribuuttie ei löydy tai se on tyhjä.
            throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 103, new String[]{XPATH_RELATES_TO}));
        }

    }

    /**
     * Sanoman 'lu:Status'-elementin tarkastus. Mahdollinen virhe palautetaan SoapVastausDto-objektissa.
     * </p>
     *
     * @param katso   {@code KatsoDto} käyttäjän tiedot.
     * @param vastaus {@code SOAPMessage} SASLRequest-vastaus.
     * @param xp      {@code XPath} XPath-instanssi.
     * @throws javax.xml.soap.SOAPException            SOAPBody-elementin haku epäonnistui.
     * @throws javax.xml.xpath.XPathExpressionException Sanoman XPath-käsittelyssä tapahtui virhe.
     * @throws RuntimeException         Sanoman käsittelyssä tapauhtui virhe tai sanoma on virheellinen.
     * @throws java.io.IOException              base64-dekoodaus epäonnistui.
     */
    private static void kasitteleStatus(final KatsoDto katso, final SOAPMessage vastaus, final XPath xp)
            throws SOAPException, XPathExpressionException, IOException {

        final org.w3c.dom.Node n = (org.w3c.dom.Node)xp.evaluate(XPATH_LU_STATUS,
                                                                 vastaus.getSOAPBody(),
                                                                 XPathConstants.NODE);

        if (n != null) {
            final String code = xp.evaluate("@code", n);
            final String comment = xp.evaluate("@comment", n);

            if ("OK".equalsIgnoreCase(code)) {
                // Sisäänkirjautuminen ok
                if ("AccountIsExpiring".equalsIgnoreCase(comment)) {
                    // txt.105=Vaihtuvat Katso otp-salasanat ovat lopussa. Tulosta uusi lista osoitteessa: {0}.
                    katso.getOut().setInfo(Props.teksti(katso.getIn().getKieli(), 105));
                }
            } else if ("Abort".equalsIgnoreCase(code)) {
                // Virhe sisäänkirjautumisessa
                if (comment != null && comment.length() != 0) {
                    if ("Initialized".equalsIgnoreCase(comment)) {
                        // txt.106=SASLRespond: Käyttäjätunnus alustettu, mutta aktivoimatta.
                        katso.getOut().setKatsoIdVirhe(true);
                        katso.getOut().setVirhe(Props.teksti(katso.getIn().getKieli(), 106));
                    } else if ("Founded".equalsIgnoreCase(comment)) {
                        // txt.107=SASLRespond: Käyttäjätunnus perustettu, mutta aktivoimatta.
                        katso.getOut().setKatsoIdVirhe(true);
                        katso.getOut().setVirhe(Props.teksti(katso.getIn().getKieli(), 107));
                    } else if ("Locked".equalsIgnoreCase(comment)) {
                        // txt.108=SASLRespond: Käyttäjätunnus on lukittu.
                        katso.getOut().setKatsoIdVirhe(true);
                        katso.getOut().setVirhe(Props.teksti(katso.getIn().getKieli(), 108));
                    } else if ("Disabled".equalsIgnoreCase(comment)) {
                        // txt.109=SASLRespond: Käyttäjätunnus on peruutettu.
                        katso.getOut().setKatsoIdVirhe(true);
                        katso.getOut().setVirhe(Props.teksti(katso.getIn().getKieli(), 109));
                    } else if ("OutOfOTP".equalsIgnoreCase(comment)) {
                        // txt.110=SASLRespond: Vaihtuvat otp-salasanat ovat loppu.
                        katso.getOut().setKatsoIdVirhe(true);
                        katso.getOut().setVirhe(Props.teksti(katso.getIn().getKieli(), 110));
                    } else {
                        // txt.111=SASLRespond-sanoman '{0}'-elementin {1}-attribuutin arvo virheellinen: '{2}'.
                        String[] args = new String[]{"lu:Status", "comment", comment};
                        throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 111, args));
                    }
                } else {
                    // txt.112=SASLRespond: Virhe sisäänkirjautumisessa. '{0}'='{1}'.
                    katso.getOut().setSalasanaVirhe(true);
                    katso.getOut().setVirhe(Props.teksti(katso.getIn().getKieli(), 116, new String[]{"lu:Status", code}));
                }
            } else if ("Continue".equalsIgnoreCase(code)) {
                // Käyttäjätunnus ja salasana on ok, mutta vaihtuva salasana on väärin
                // Parsitaan vastaussanomasta oikea salasanan järjestysnumero
                final String saData = xp.evaluate(XPATH_SA_DATA, vastaus.getSOAPBody());

                if (saData != null && saData.length() != 0) {
                    // Palautetaan base64-koodattu merkkijono purettuna
                    // txt.113=Seuraavan vaihtuvan salasanan järjestysnumero on {0}.
                    final String purettu = Base64.decode64(saData);
                    katso.getOut().setOtpVirhe(true);
                    katso.getOut().setVirhe(Props.teksti(katso.getIn().getKieli(), 113, new String[]{purettu}));
                } else {
                    // txt.103=SASLRespond-sanoman '{0}'-elementtiä tai -attribuuttie ei löydy tai se on tyhjä.
                    throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 103, new String[]{XPATH_SA_DATA}));
                }
            } else {
                // txt.111=SASLRespond-sanoman '{0}'-elementin {1}-attribuutin arvo virheellinen: '{2}'.
                String[] args = new String[]{XPATH_LU_STATUS, "code", code};
                throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 103, args));
            }
        } else {
            // txt.103=SASLRespond-sanoman '{0}'-elementtiä tai -attribuuttie ei löydy tai se on tyhjä.
            throw new RuntimeException(Props.teksti(katso.getIn().getKieli(), 103, new String[]{XPATH_LU_STATUS}));
        }

    }

    /**
     * SASLRespond-sanoman 'saml:Assertion'-elementin palautus.
     * </p>
     *
     * @param kieli {@code String} ilmoitusten kielikoodi.
     * @param runko {@code SOAPMessage} SASLRespond-sanoman runkoelementti.
     * @param xp    {@code XPath} XPath-instanssi.
     * @return SASLRespond-sanoman 'saml:Assertion'-elementti annetusta sanomasta tai virhetilanteessa null.
     */
    private static SOAPElement annaAssertion(final String kieli, final SOAPBody runko, final XPath xp) {

        SOAPElement se;

        try {
            final org.w3c.dom.Node n = (org.w3c.dom.Node)xp.evaluate(XPATH_SAML_ASSERTION, runko, XPathConstants.NODE);

            if (n != null) {
                se = (SOAPElement)n;
            } else {
                throw new RuntimeException("SASLRespond-sanoman 'saml:Assertion'-elementtiä ei löydy.");
            }
        } catch (Exception e) {
            log.severe("annaAssertion() - " + Apuri.annaPoikkeus(e));
            // txt.115=SASLRespond-sanoman '{0}'-elementin käsittelyssä tapahtui virhe: {1}.
            throw new RuntimeException(Props.teksti(kieli, 115, new String[]{"saml:Assertion", e.getMessage()}));
        }

        return se;
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
