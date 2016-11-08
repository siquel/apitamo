package org.verohallinto.apitamoclient.katso.sanoma;

import org.verohallinto.apitamoclient.apu.Apuri;
import org.verohallinto.apitamoclient.apu.LokiApu;
import org.verohallinto.apitamoclient.apu.SanomaApu;
import org.verohallinto.apitamoclient.dto.*;
import org.verohallinto.apitamoclient.yleiset.Nimiavaruus;
import org.verohallinto.apitamoclient.yleiset.Props;
import org.verohallinto.apitamoclient.yleiset.Vakiot;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.soap.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>Apitamo-sanoman lähetys</p>
 * (c) 2014 Tietokarhu Oy
 */
public class Apitamo {

    private static final Logger log = Logger.getLogger(Apitamo.class.getName());

    // ilmoitusten lähetys
    private static final String XPATH_ILMOITUS_VASTAANOTTO = "ApiTaMoTulos/kuittaus/vastaanotto/text()";
    private static final String XPATH_ILMOITUS_VASTAANOTTO_INFO = "ApiTaMoTulos/kuittaus/info/text()";
    private static final String XPATH_ILMOITUS_VASTAANOTTO_AIKA = "ApiTaMoTulos/kuittaus/aika/text()";
    private static final String XPATH_ILMOITUS_VASTAANOTTO_CHECKSUM = "ApiTaMoTulos/kuittaus/checksum/text()";
    private static final String XPATH_ILMOITUS_VASTAANOTTO_ILMOITUSTUNNISTE = "ApiTaMoTulos/kuittaus/ilmoitustunniste/text()";
    private static final String XPATH_ILMOITUS_VASTAANOTTO_NOUTOTUNNISTE = "ApiTaMoTulos/kuittaus/noutotunniste/text()";
    private static final String XPATH_ILMOITUS_VASTAANOTTO_TAMOTULOS = "ApiTaMoTulos/TamoTulos";

    // Tamo-tulos
    private static final String XPATH_TARKISTUKSENTULOS_TILA = "TarkistuksenTulos/text()";
    private static final String XPATH_TARKISTUKSENTULOS_VIRHEELLISIA = "TarkistuksenTulos/@virheellisia";
    private static final String XPATH_TARKISTUKSENTULOS_OIKEELLISIA = "TarkistuksenTulos/@oikeellisia";
    private static final String XPATH_TARKISTUKSENTULOS_TIETUEKPL = "TarkistuksenTulos/@tietuekpl";

    //Lomakkeet
    private static final String XPATH_LOMAKKEET = "Lomake";
    private static final String XPATH_LOMAKKE_VUOSI = "@vuosi";
    private static final String XPATH_LOMAKKE_TILA = "@tila";
    private static final String XPATH_LOMAKKE_SELITE = "@selite";
    private static final String XPATH_LOMAKKE_TIETUETUNNUS = "@nimi";
    private static final String XPATH_LOMAKKE_YTUNNUS = "@asiakas";

    // virherivit
    private static final String XPATH_VIRHERIVI = "Virherivi";
    private static final String XPATH_VIRHERIVI_TUNNUS = "@tunnus";
    private static final String XPATH_VIRHERIVI_RIVINRO = "@rivinro";
    private static final String XPATH_VIRHERIVI_TIETO = "Tieto/text()";
    private static final String XPATH_VIRHERIVI_SELITYS = "Selitys/text()";

    // Liitteet
    private static final String XPATH_LIITEET = "ApiTaMoTulos/kuittaus/liitteet/liite";
    private static final String XPATH_LIITE_NIMI = "tiedosto/text()";
    private static final String XPATH_LIITE_VASTAANOTTO = "vastaanotto/text()";
    private static final String XPATH_LIITE_INFO = "info/text()";

    // ilmoitusten nouto
    private static final String XPATH_NOUTO_ILMOITUKSET = "Vastaus/ilmoitus/text()";
    private static final String XPATH_NOUTO_AIKA = "Vastaus/aika/text()";
    private static final String XPATH_NOUTO_TUNNISTE = "Vastaus/noutotunniste/text()";
    private static final String XPATH_NOUTO_INFO = "Vastaus/info/text()";

    /**
     * Apitamo-sanoman XPath-käsittelyssä käytettävät Namespace-määritykset
     */
    private static final Nimiavaruus NAMESPACE_CONTEXT;

    static {
        NAMESPACE_CONTEXT = new Nimiavaruus();
        NAMESPACE_CONTEXT.setNamespace("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
        NAMESPACE_CONTEXT.setNamespace("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        NAMESPACE_CONTEXT.setNamespace("tns", "http://www.vero.fi/xmlschema/ApiTaMo");
    }

    private Apitamo() {
    }

    /**
     * Apitamo-kirjautumissanoman lähetys.
     * </p>
     *
     * @param apitamoDto {@code ApitamoInDto} kutsun tiedot.
     * @return Vastaus sanoma SASLRequest-pyyntöön.
     */
    public static SoapVastausDto kirjaudu(final ApitamoInDto apitamoDto, final KatsoDto katso) {

        final SoapVastausDto dto = new SoapVastausDto();
        SOAPMessage vastaus;

        try {
            // Muodostetaan lähetettävä sanoma
            final SOAPMessage pyynto = muodostaKirjautumisPyynto(apitamoDto);

            if (Props.lokitaSanomat()) {
                LokiApu.lokitaSanoma(LokiApu.annaEtuliite(Vakiot.APITAMO_PYYNTO) + "Pyynto_" + System.currentTimeMillis() + ".xml", pyynto);
            }

            //lähetetään sanoma
            final String url = Props.getApitamoURL();
            vastaus = sendMessage(pyynto, url);

            if (Props.lokitaSanomat()) {
                LokiApu.lokitaSanoma(LokiApu.annaEtuliite(Vakiot.APITAMO_PYYNTO) + "Vastaus_" + System.currentTimeMillis() + ".xml", vastaus);
            }

            final SOAPBody runko = vastaus.getSOAPBody();

            if (!runko.hasFault()) {
                katso.getIn().setMessage(vastaus);
                dto.setOk(true);
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
     * Apitamo-kirjautumissanoman lähetys.
     * </p>
     *
     * @param apitamoDto {@code ApitamoInDto} kutsun tiedot.
     * @return Vastaus sanoma SASLRequest-pyyntöön.
     */
    public static SoapVastausDto laheta(final ApitamoDto apitamoDto) {

        final SoapVastausDto dto = new SoapVastausDto();
        SOAPMessage vastaus;
        String nimi = "";
        final XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(NAMESPACE_CONTEXT);

        try {
            // Muodostetaan lähetettävä sanoma
            final SOAPMessage pyynto = muodostaLahetysPyynto(apitamoDto, xp);

            if (Props.lokitaSanomat()) {
                LokiApu.lokitaSanoma(LokiApu.annaEtuliite(Vakiot.APITAMO_PYYNTO) + "Pyynto_" + System.currentTimeMillis() + ".xml", pyynto);
            }

            //lähetetään sanoma
            final String url = Props.getApitamoURL();
            //vastaus = HttpAsiakas.lahetaKatso(apitamoDto.getIn().getKieli(), url, pyynto, Vakiot.APITAMO_PYYNTO);
            vastaus = sendMessage(pyynto, url);

            if (Props.lokitaSanomat()) {
                nimi = LokiApu.lokitaSanoma(LokiApu.annaEtuliite(Vakiot.APITAMO_PYYNTO) + "Lopputulos_" + System.currentTimeMillis() + ".xml", vastaus);
            }

            final SOAPBody runko = vastaus.getSOAPBody();

            if (!runko.hasFault()) {
                kasitteleVastaus(apitamoDto, vastaus, xp);
                dto.setOk(true);
                apitamoDto.getOut().setVastausSanomanNimi(nimi);
            } else {
                apitamoDto.getOut().setVirheMsg(SanomaApu.annaSanomanVirhe(vastaus));
            }
        } catch (Exception e) {
            apitamoDto.getOut().setVirheMsg(e.getMessage());
            vastaus = SanomaApu.annaVirhesanoma(Vakiot.SOAP_ENV_CLIENT, apitamoDto.getOut().getVirheMsg());
            log.severe("laheta() - " + Apuri.annaPoikkeus(e));
        }
        dto.setSanoma(vastaus);
        apitamoDto.getOut().setVastausSanoma(vastaus);

        return dto;
    }

    /**
     * Apitamo tyhjän kirjautumissanoman muodostus.
     * </p>
     *
     * @param dto {@code ApitamoInDto} lähetystiedot tiedot.
     * @return tyhjä Apitamo-sanoma.
     * @throws RuntimeException Sanomapohjaa ei löydy.
     * @throws java.io.IOException      tunnistautumismerkkijonon koodaus epäonnistui.
     * @throws javax.xml.soap.SOAPException    Sanoman tallennus epäonnistui.
     */
    private static SOAPMessage muodostaKirjautumisPyynto(final ApitamoInDto dto) throws IOException, SOAPException {

        SOAPMessage sanoma;
        String pohja;
        String s;

        pohja = Props.annaApitamoPohja();

        if (pohja != null) {

            final String[] ss = new String[]{//new String(dto.getData(), "ISO-8859-1"), // ilmoitusdata
                    dto.getKieli(), // kieli
                    "1", // tyyppi on aina 1
                    "0", // liitteet
                    "nan"}; // email

            if (!Apuri.onArvot(ss, 4)) {
                // txt.019=Sanomapohjan '{0}' parametrien määrä on virheellinen.
                throw new RuntimeException(Props.teksti(dto.getKieli(), 19, new String[]{"ApiTaMo"}));
            }

            s = new MessageFormat(pohja).format(ss);

            sanoma = SanomaApu.merkkijonoSanomaksi(s);

            if (sanoma.saveRequired()){
           		sanoma.saveChanges();
           	}

        } else {
            // txt.101=Apitamo-sanomapohjaa ei löydy.
            throw new RuntimeException(Props.teksti(dto.getKieli(), 401));
        }

        return sanoma;
    }

    /**
     * Apitamo lähetyssanoman muodostus.
     * </p>
     *
     * @param apitamo {@code ApitamoDto} käyttäjän tiedot.
     * @return Apitamo-sanoma.
     * @throws RuntimeException Sanomapohjaa ei löydy.
     * @throws java.io.IOException      tunnistautumismerkkijonon koodaus epäonnistui.
     * @throws javax.xml.soap.SOAPException    Sanoman tallennus epäonnistui.
     */
    private static SOAPMessage muodostaLahetysPyynto(final ApitamoDto apitamo, final XPath xp)
            throws IOException, XPathExpressionException, SOAPException {

        SOAPMessage sanoma;
        String pohja;
        String s;

        if (apitamo.getIn().getSuunta() == Vakiot.SUUNTA_LAHETYS) {
            pohja = Props.annaApitamoPohja();
        } else {
            pohja = Props.annaApitamoPyyntoPohja();
        }

        if (pohja != null) {
            if (apitamo.getIn().getSuunta() == Vakiot.SUUNTA_LAHETYS) {

                //lisätään muu sisältö paitsi itse ilmoitusdata. Se lisätään alempana
                final String[] ss = new String[]{apitamo.getIn().getKieli(), // kieli
                        "1", // tyyppi on aina 1
                        apitamo.getIn().getLiiteTiedostot().size() > 0 ? "1" : "0", // liitteet
                        apitamo.getIn().getEmail() != null && apitamo.getIn().getEmail().length() > 0 ? apitamo.getIn().getEmail() : ""}; // email

                s = new MessageFormat(pohja).format(ss);

            } else {
                final String[] ss = new String[]{new String(apitamo.getIn().getData(), "ISO-8859-1"), // ilmoitusdata
                        apitamo.getIn().getKieli()}; // kieli

                if (!Apuri.onArvot(ss, 2)) {
                    // txt.019=Sanomapohjan '{0}' parametrien määrä on virheellinen.
                    throw new RuntimeException(Props.teksti(apitamo.getIn().getKieli(), 19, new String[]{"ApiTaMo"}));
                }

                s = new MessageFormat(pohja).format(ss);
            }

            sanoma = SanomaApu.merkkijonoSanomaksi(s);

            SOAPHeader header = sanoma.getSOAPPart().getEnvelope().getHeader();

            if (header == null) {
                header = sanoma.getSOAPPart().getEnvelope().addHeader();
            }

            header.setPrefix("SOAP-ENV");

            if (apitamo.getIn().getAssertion() != null){
	           	//header alkaa
	           	header.addChildElement(apitamo.getIn().getAssertion());
            }else{
            	//poistetaan header jos ei ole assertiota
            	header.detachNode();
            }

            // lisätään itse ilmoitusdata vasta täällä että vältytään skandien (å, ä, ö) aiheuttamilta ongelmilta
            Node n;
            if (apitamo.getIn().getSuunta() == Vakiot.SUUNTA_LAHETYS) {
                n = (Node) sanoma.getSOAPBody().getElementsByTagName("ilmoitus").item(0);
            }else {
                n = (Node) sanoma.getSOAPBody().getElementsByTagName("noutotunniste").item(0);
            }
            if (apitamo.getIn().getData() != null && apitamo.getIn().getData().length > 0) {
                n.setTextContent(new String(apitamo.getIn().getData(), "ISO-8859-1"));
            } else {
                n.setTextContent("");
            }

            //lisätään mahdolliset liitteet
            if (apitamo.getIn().getSuunta() == Vakiot.SUUNTA_LAHETYS && apitamo.getIn().getLiiteTiedostot().size() > 0) {
                DataHandler dataHandler = null;
                AttachmentPart attachment = null;
                String filename = "";
                File file = null;

                for (String liite : apitamo.getIn().getLiiteTiedostot()) {
                    dataHandler = new DataHandler(new FileDataSource(liite));
                    attachment = sanoma.createAttachmentPart(dataHandler);

                    file = new File(liite);
                    filename = file.getName();

                    attachment.setContentId(filename);
                    attachment.setContentType("application/pdf");
                    sanoma.addAttachmentPart(attachment);
                }
            }

            if (sanoma.saveRequired()){
           		sanoma.saveChanges();
           	}

        } else {
            // txt.101=Apitamo-sanomapohjaa ei löydy.
            throw new RuntimeException(Props.teksti(apitamo.getIn().getKieli(), 401));
        }

        return sanoma;
    }



    private static void kasitteleVastaus(final ApitamoDto apitamo, final SOAPMessage vastaus, final XPath xp)
            throws SOAPException, XPathExpressionException, ParseException {

        final SOAPElement se = (SOAPElement)vastaus.getSOAPBody().getFirstChild();
        final Document doc = SanomaApu.elementtiDokumentiksi(se);

        if (apitamo.getIn().getSuunta() == Vakiot.SUUNTA_LAHETYS) {
            final boolean vastaanottoOk = Boolean.parseBoolean(xp.evaluate(XPATH_ILMOITUS_VASTAANOTTO, doc));

            if (vastaanottoOk) {
                apitamo.getOut().setAineistonVastaanottoOk(vastaanottoOk);
                apitamo.getOut().setAikaleima(xp.evaluate(XPATH_ILMOITUS_VASTAANOTTO_AIKA, doc));
                apitamo.getOut().setChecksum(xp.evaluate(XPATH_ILMOITUS_VASTAANOTTO_CHECKSUM, doc));
                apitamo.getOut().setIlmoitusTunniste(xp.evaluate(XPATH_ILMOITUS_VASTAANOTTO_ILMOITUSTUNNISTE, doc));
                apitamo.getOut().setNoutoTunniste(xp.evaluate(XPATH_ILMOITUS_VASTAANOTTO_NOUTOTUNNISTE, doc));
            } else {
                apitamo.getOut().setAineistonVastaanottoOk(vastaanottoOk);
                apitamo.getOut().setInfo(xp.evaluate(XPATH_ILMOITUS_VASTAANOTTO_INFO, doc));
            }

            final NodeList liitteet = (NodeList)xp.evaluate(XPATH_LIITEET, doc, XPathConstants.NODESET);
            List<LiiteDto> liiteLista = new ArrayList<LiiteDto>(liitteet.getLength());

            if (liitteet != null && liitteet.getLength() > 0) {
                for (int i = 0; i < liitteet.getLength(); i++) {
                    org.w3c.dom.Node liite = liitteet.item(i);
                    LiiteDto liiteDto = new LiiteDto();

                    liiteDto.setLiiteNimi(xp.evaluate(XPATH_LIITE_NIMI, liite));
                    liiteDto.setVastaanottoOk(Boolean.parseBoolean(xp.evaluate(XPATH_LIITE_VASTAANOTTO, liite)));
                    liiteDto.setInfo(xp.evaluate(XPATH_LIITE_INFO, liite));
                    liiteLista.add(liiteDto);

                    if (liiteDto.isVastaanottoOk()) {
                        apitamo.getOut().setLiitteitaVastaanotettu(true);
                    }
                }
            }
            apitamo.getOut().setLiitteet(liiteLista);
            KasitteleTamoTulos(apitamo, xp, (org.w3c.dom.Node)xp.evaluate(XPATH_ILMOITUS_VASTAANOTTO_TAMOTULOS, doc, XPathConstants.NODE));
        } else {
            apitamo.getOut().setInfo(xp.evaluate(XPATH_NOUTO_INFO, doc));
            apitamo.getOut().setAikaleima(xp.evaluate(XPATH_NOUTO_AIKA, doc));
            apitamo.getOut().setNoutoTunniste(xp.evaluate(XPATH_NOUTO_TUNNISTE, doc));

            if (Props.PuraTamoTulos()) {
                String ilmoitukset = xp.evaluate(XPATH_NOUTO_ILMOITUKSET, doc);
                if (ilmoitukset.length() > 0) {
                    String[] apu = ilmoitukset.split("\n");
                    List<String> ilm = new ArrayList<>(apu.length);
                    Collections.addAll(ilm, apu);
                    apitamo.getOut().setIlmoitukset(ilm);
                }
            }
        }
    }

    private static void KasitteleTamoTulos (final ApitamoDto apitamo, final XPath xp, final org.w3c.dom.Node n)
            throws SOAPException, XPathExpressionException, ParseException {

        TamoTulosDto tamoTulosDto = new TamoTulosDto();

        String ttulos = xp.evaluate(XPATH_TARKISTUKSENTULOS_TILA, n);

        if (ttulos.equalsIgnoreCase("OK") || ttulos.equalsIgnoreCase("VIRHE")) {
            tamoTulosDto.setTarkistuksenTulosOk(ttulos.equalsIgnoreCase("Ok"));
            tamoTulosDto.setVirheellisia(Long.parseLong(xp.evaluate(XPATH_TARKISTUKSENTULOS_VIRHEELLISIA, n)));
            tamoTulosDto.setOikeellisia(Long.parseLong(xp.evaluate(XPATH_TARKISTUKSENTULOS_OIKEELLISIA, n)));
            tamoTulosDto.setTietueKpl(Long.parseLong(xp.evaluate(XPATH_TARKISTUKSENTULOS_TIETUEKPL, n)));

            if (Props.PuraTamoTulos()) {
                List<LomakeDto> lomakkeet = new ArrayList<>();
                NodeList nl_lomakkeet = (NodeList) xp.evaluate(XPATH_LOMAKKEET, n, XPathConstants.NODESET);

                for (int i = 0; i < nl_lomakkeet.getLength(); i++) {
                    LomakeDto lomake = new LomakeDto();

                    org.w3c.dom.Node n1 = nl_lomakkeet.item(i);
                    lomake.setLomakeVuosi(xp.evaluate(XPATH_LOMAKKE_VUOSI, n1));
                    lomake.setLomakkeenTilaOk(xp.evaluate(XPATH_LOMAKKE_TILA, n1).equalsIgnoreCase("OK"));
                    lomake.setSelite(xp.evaluate(XPATH_LOMAKKE_SELITE, n1));
                    lomake.setTietueTunnus(xp.evaluate(XPATH_LOMAKKE_TIETUETUNNUS, n1));
                    lomake.setyTunnus(xp.evaluate(XPATH_LOMAKKE_YTUNNUS, n1));

                    List<VirheriviDto> virheRivit = new ArrayList<>();
                    NodeList nl_virheRivit = (NodeList) xp.evaluate(XPATH_VIRHERIVI, n1, XPathConstants.NODESET);

                    for (int j = 0; j < nl_virheRivit.getLength(); j++) {
                        VirheriviDto rivi = new VirheriviDto();

                        org.w3c.dom.Node n2 = nl_virheRivit.item(j);
                        rivi.setTunnus(xp.evaluate(XPATH_VIRHERIVI_TUNNUS, n2));
                        rivi.setRivinro(xp.evaluate(XPATH_VIRHERIVI_RIVINRO, n2));
                        rivi.setTieto(xp.evaluate(XPATH_VIRHERIVI_TIETO, n2));
                        rivi.setSelitys(xp.evaluate(XPATH_VIRHERIVI_SELITYS, n2));

                        virheRivit.add(rivi);
                    }
                    lomake.setVirheet(virheRivit);
                    lomakkeet.add(lomake);
                }

                tamoTulosDto.setLomakkeet(lomakkeet);
            }
        } else {
            // kyseessä ollut tyhjä kutsu (mahdollisesti vain liitteitä mukana)
            tamoTulosDto.setTarkistuksenTulosOk(false);
            tamoTulosDto.setVirheellisia(0);
            tamoTulosDto.setOikeellisia(0);
            tamoTulosDto.setTietueKpl(0);
        }
        apitamo.getOut().setTamoTulos(tamoTulosDto);
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
