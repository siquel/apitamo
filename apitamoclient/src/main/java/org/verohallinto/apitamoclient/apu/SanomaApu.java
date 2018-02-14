package org.verohallinto.apitamoclient.apu;

import org.verohallinto.apitamoclient.yleiset.Props;
import org.verohallinto.apitamoclient.yleiset.Vakiot;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * SOAP-sanomien käsittelyssä käytettävät staattiset apumetodit.
 * </p>
 * (c) 2013 Tietokarhu Oy
 * <p/>
 */
public class SanomaApu {

  private static final Logger log = Logger.getLogger(SanomaApu.class.getName());

  private SanomaApu() {

  }

  /**
   * SOAP-virhesanoman muodostus annetulla kodilla ja ilmoituksella.
   * </p>
   *
   * @param koodi
   *          {@code String} sanoman virhekoodi.
   * @param selite
   *          {@code String} sanoman virheselite.
   * @return Sanoma SOAPFault-elementillä varustettuna tai virhetilanteessa
   *         null.
   */
  public static SOAPMessage annaVirhesanoma(final String koodi, final String selite) {

    if (log.isLoggable(Level.FINEST)) {
      log.finest("annaVirhesanoma(): koodi='" + koodi + "', selite='" + selite + '\'');
    }

    final String pohja = Props.annaKatsoVirhePohja();
    final String[] args = new String[] { koodi, Apuri.iso885912Utf8(selite) };
    final String muokattu = new MessageFormat(pohja).format(args);
    return SanomaApu.merkkijonoSanomaksi(muokattu);
  }

  /**
   * Palauttaa SOAP-virhesanoman virheilmoituksen annetusta sanomasta.
   * </p>
   *
   * @param sanoma
   *          {@code SOAPMessage} virhesanoma.
   * @return Sanoman virheilmoitus jos löytyy, muuten null.
   */
  public static String annaSanomanVirhe(final SOAPMessage sanoma) {

    try {
      if (sanoma != null && sanoma.getSOAPBody() != null && sanoma.getSOAPBody().hasFault()) {
        final SOAPFault virhe = sanoma.getSOAPBody().getFault();

        return virhe.getFaultString();
      }
    } catch (SOAPException e) {
      log.severe(Apuri.annaPoikkeus(e));
    }

    return null;
  }

  /**
   * Annetun SOAP-sanoman muunto tavuiksi.
   * </p>
   *
   * @param sanoma
   *          {@code SOAPMessage} muunnettava SOAP-sanoma.
   * @return Annettu sanoma tavuina tai virhetilanteessa null.
   */
  public static byte[] sanomaTavuiksi(final SOAPMessage sanoma) {

    if (sanoma != null) {
      final ByteArrayOutputStream out = new ByteArrayOutputStream();

      try {
        sanoma.writeTo(out);
        return out.toByteArray();
      } catch (Exception e) {
        log.severe(Apuri.annaPoikkeus(e));
      }
    } else if (log.isLoggable(Level.FINEST)) {
      log.finest("sanomaTavuiksi(): Sanoma on tyhjä.");
    }

    return null;
  }

  /**
   * SOAP-sanoman muunto merkkijonoksi.
   * </p>
   *
   * @param sanoma
   *          {@code SOAPMessage} muunnettava SOAP-sanoma.
   * @return Annettu sanoma merkkijonona tai virhetilanteessa null.
   */
  public static String sanomaMerkkijonoksi(final SOAPMessage sanoma) {

    if (sanoma != null) {
      final ByteArrayOutputStream out = new ByteArrayOutputStream();

      try {
        sanoma.writeTo(out);

        return out.toString(Vakiot.SANOMAMERKISTO);
      } catch (Exception e) {
        log.severe(Apuri.annaPoikkeus(e));
      }
    }

    return null;
  }

  /**
   * Annetun SOAP-sanoman muunto striimiksi.
   * </p>
   *
   * @param sanoma
   *          {@code SOAPMessage} muunnettava SOAP-sanoma.
   * @return Annettu sanoma striiminä tai virhetilanteessa null.
   */
  public static InputStream sanomaStriimiksi(final SOAPMessage sanoma) {

    if (sanoma != null) {
      final String s = sanomaMerkkijonoksi(sanoma);

      if (s != null && s.length() != 0) {
        return new ByteArrayInputStream(s.getBytes());
      }
    }

    return null;
  }

  /**
   * Annetun merkkijonon muunto SOAP-sanomaksi.
   * </p>
   *
   * @param sanoma
   *          {@code String} muunnettava merkkijono.
   * @return Annettu sanoma merkkijonona tai virhetilanteessa null.
   */
  public static SOAPMessage merkkijonoSanomaksi(final String sanoma) {

    if (sanoma != null && sanoma.trim().length() != 0) {
      try {
        // Muodostetaan SOAP-sanoma
        final SOAPMessage message = MessageFactory.newInstance().createMessage();
        // mimic writeTo() behaviour and append header so
        // sending requests works without logging
        message.getMimeHeaders().addHeader("SOAPAction", "\"\"");
        // Muodostetaan viestin sisältö lataamalla muokattu sisältö
        // SOAP-sanomaan
        final SOAPPart part = message.getSOAPPart();
        part.setContent(new StreamSource(new ByteArrayInputStream(sanoma.getBytes())));
        return message;
      } catch (Exception e) {
        log.severe(Apuri.annaPoikkeus(e));
      }
    }

    return null;
  }

  /**
   * Annetun SOAP-elementin muunto dokumentiksi.
   * </p>
   *
   * @param elementti
   *          {@code SOAPElement} muunnettava SOAP-elementti.
   * @return Elementti dokumenttina, tai virhetilanteessa null.
   */
  public static Document elementtiDokumentiksi(final SOAPElement elementti) {

    Document doc;

    try {
      doc = merkkijonoDokumentiksi(elementtiMerkkijonoksi(elementti));
    } catch (Exception e) {
      doc = null;
      log.severe(Apuri.annaPoikkeus(e));
    }

    return doc;
  }

  /**
   * Muuntaa annetun SOAP-elementin merkkijonoksi.
   * </p>
   *
   * @param elementti
   *          {@code SOAPElement} muunnettava elementti.
   * @return Merkkijonoksi muunnettu SOAP-elementti tai virhetilanteessa null.
   */
  public static String elementtiMerkkijonoksi(final SOAPElement elementti) {

    if (elementti != null) {
      try {
        final Transformer trans = TransformerFactory.newInstance().newTransformer();
        final StringWriter sw = new StringWriter();
        trans.transform(new DOMSource(elementti), new StreamResult(sw));

        return sw.toString();
      } catch (Exception e) {
        log.severe(Apuri.annaPoikkeus(e));
      }
    }

    return null;
  }

  /**
   * Muuntaa annetun XML-dokumentin merkkijonoksi.
   * </p>
   *
   * @param doc
   *          {@code SOAPElement} muunnettava XML-dokumentti.
   * @return Merkkijonoksi muunnettu XML-dokumentti tai null, jos muunnos
   *         epäonnistui.
   */
  public static String dokumenttiMerkkijonoksi(final Document doc) {

    if (doc != null) {
      try {
        final StringWriter sw = new StringWriter();
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));

        return sw.toString();
      } catch (TransformerException e) {
        log.severe(Apuri.annaPoikkeus(e));
      }
    }

    return null;
  }

  /**
   * Muuntaa annetun merkkijonon XML-dokumentiksi.
   * </p>
   *
   * @param s
   *          {@code String} muunnettava merkkijono.
   * @return XML-dokumentti tai null, jos muunnos epäonnistui.
   */
  public static Document merkkijonoDokumentiksi(final String s) {

    try {
      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      final ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes(Vakiot.SANOMAMERKISTO));

      return dbf.newDocumentBuilder().parse(bais);
    } catch (Exception e) {
      log.severe(Apuri.annaPoikkeus(e));
    }

    return null;
  }
}
