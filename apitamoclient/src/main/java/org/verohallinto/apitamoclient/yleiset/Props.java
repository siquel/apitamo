package org.verohallinto.apitamoclient.yleiset;

import org.verohallinto.apitamoclient.apu.Apuri;
import org.verohallinto.apitamoclient.apu.TiedostoApu;
import org.verohallinto.apitamoclient.dto.KatsoInDto;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * </p>
 * (c) 2013 Tietokarhu Oy
 * <p/>
 */
public class Props {

  private static final Logger log = Logger.getLogger(Props.class.getName());
  private static String sovellusId;
  private static String apitamoPohja;
  private static String apitamoPyyntoPohja;
  private static String katsoVirhePohja;
  private static String katsoSaslPohja;
  private static String katsoWsidpPohja;

  private static String saslURL;
  private static String wsidpURL;
  private static String apitamoURL;

  // Tunnistustapakohtaiset tiedot
  private static Map<String, Properties> props;
  private static Map<String, Properties> tekstit;

  // Sekalaista
  private static boolean lokitaSanomat;
  private static String sanomaHakemisto;
  private static boolean puraTamoTulos;

  static {
    props = Collections.synchronizedMap(new HashMap<String, Properties>());
    tekstit = Collections.synchronizedMap(new HashMap<String, Properties>(Vakiot.KIELET.size()));
    // Sovelluksen tiedot
    sovellusId = Vakiot.SOVELLUS.toLowerCase();
    lueProperties(sovellusId);
    // Katso SOAP-sanomapohjat
    katsoVirhePohja = TiedostoApu.luePolusta(anna(Vakiot.KATSO_XML_VIRHE));
    katsoSaslPohja = TiedostoApu.luePolusta(anna(Vakiot.KATSO_XML_SASL));
    katsoWsidpPohja = TiedostoApu.luePolusta(anna(Vakiot.KATSO_XML_WSIDP));
    apitamoPyyntoPohja = TiedostoApu.luePolusta(anna(Vakiot.APITAMO_PYYNTO_POHJA));
    apitamoPohja = TiedostoApu.luePolusta(anna(Vakiot.APITAMO_POHJA));
    //
    lueTekstit();
  }

  private Props() {

  }

  /**
   * Sovelluksen staattisten tietojen alustus.
   * </p>
   *
   * @param id
   *          {@code String} alustettavien tietojen id.
   */
  public static void alusta(final String id, final KatsoInDto in) {

    lueProperties(id);

    lokitaSanomat = in.isLogMessages();
    sanomaHakemisto = in.getMessageDirectory();
    puraTamoTulos = in.isPuraTamoTulos();
    saslURL = in.getSASLURL();
    wsidpURL = in.getWSIDPURL();
    apitamoURL = in.getAPITAMOURL();
  }

  /**
   * Hakee annetun avaimen mukaisen properties arvon sovellus id:n perusteella.
   * </p>
   *
   * @param avain
   *          {@code String} tiedon avain.
   * @return Annatun avaimen arvo tai null, jos tietoa ei löydy.
   */
  public static String anna(final String avain) {

    return anna(sovellusId, avain);
  }

  /**
   * Hakee annetun avaimen mukaisen properties arvon.
   * </p>
   *
   * @param id
   *          {@code String} tunnistus id.
   * @param avain
   *          {@code String} tiedon avain.
   * @return Annatun avaimen arvo tai null, jos tietoa ei löydy.
   */
  public static String anna(final String id, final String avain) {

    if (props.containsKey(id)) {
      final Properties p = props.get(id);

      if (p.containsKey(avain)) {
        return (String) p.get(avain);
      } else {
        log.severe("anna() - Avainta ei löydy: id='" + id + "', avain='" + avain + "'.");
      }
    } else {
      log.severe("anna() - Tunnusta ei löydy: id='" + id + "'.");
    }

    return null;
  }

  /**
   * Hakee annetun avaimen mukaisen properties int-arvon sovellus id:n
   * perusteella.
   * </p>
   *
   * @param avain
   *          {@code String} tiedon avain.
   * @param oletus
   *          {@code int} oletusarvo, joka palautetaan, jos tietoa ei löydy..
   * @return Avaimen mukainen arvo, tai annettu oletusarvo, jos tietoa ei löydy.
   */
  public static int annaInt(final String avain, final int oletus) {

    return annaInt(sovellusId, avain, oletus);
  }

  /**
   * Integer-arvon palautus properties-tiedoista.
   * </p>
   *
   * @param id
   *          {@code String} tunnistus id.
   * @param avain
   *          {@code String} tiedon avain.
   * @param oletus
   *          {@code int} oletusarvo, joka palautetaan, jos tietoa ei löydy..
   * @return Avaimen mukainen arvo, tai annettu oletusarvo, jos tietoa ei löydy.
   */
  public static int annaInt(final String id, final String avain, final int oletus) {

    final String arvo = anna(id, avain);

    if (Apuri.onNumero(arvo)) {
      return Integer.parseInt(arvo);
    }

    return oletus;
  }

  /**
   * Kielikohtaisen tekstin haku.
   * </p>
   *
   * @param kieli
   *          {@code String} kielikoodi, jolla teksti halutaan.
   * @param nro
   *          {@code int} halutun tekstin numero.
   * @return Haluttu teksti jos löytyy, muuten null.
   */
  public static String teksti(final String kieli, final int nro) {

    return teksti(kieli, nro, null);
  }

  /**
   * Kielikohtaisen tekstin haku.
   * </p>
   *
   * @param kieli
   *          {@code String} kielikoodi, jolla teksti halutaan.
   * @param nro
   *          {@code int} halutun tekstin numero.
   * @param param
   *          {@code String}[] mahdolliset korvausparametrit.
   * @return Haluttu teksti jos löytyy, muuten null.
   */
  public static String teksti(final String kieli, final int nro, final Object[] param) {

    final Properties p = tekstit.containsKey(kieli) ? tekstit.get(kieli) : tekstit.get(Vakiot.OLETUSKIELI);

    if (p != null) {
      final String avain = "txt." + Apuri.etunollat(nro, Vakiot.TEKSTITUNNISTE_PIT);

      if (p.containsKey(avain)) {
        final String txt = p.getProperty(avain);

        if (param != null && param.length > 0) {
          return new MessageFormat(txt, Vakiot.LOCALE).format(param);
        } else {
          return txt;
        }
      } else {
        log.severe("teksti() - Tekstiä '" + avain + "' ei löydy kielellä: '" + kieli + "'.");
      }
    } else {
      log.severe("teksti() - Tekstejä ei löydy kielellä: '" + kieli + "'.");
    }

    return null;
  }

  public static String annaKatsoVirhePohja() {

    return katsoVirhePohja;
  }

  public static String annaKatsoSaslPohja() {

    return katsoSaslPohja;
  }

  public static String annaKatsoWsidpPohja() {

    return katsoWsidpPohja;
  }

  public static String annaApitamoPohja() {

    return apitamoPohja;
  }

  public static String annaApitamoPyyntoPohja() {

    return apitamoPyyntoPohja;
  }

  public static boolean lokitaSanomat() {

    return lokitaSanomat;
  }

  public static String getSaslURL() {
    return saslURL;
  }

  public static String getWsidpURL() {
    return wsidpURL;
  }

  public static String getApitamoURL() {
    return apitamoURL;
  }

  public static boolean PuraTamoTulos() {
    return puraTamoTulos;
  }

  public static String annaSanomaHakemisto() {
    return sanomaHakemisto;
  }

  /**
   * Lukee annetun properties-tiedoton luokkapolusta.
   * </p>
   *
   * @param id
   *          {@code String} luettavan properties-tiedoston sovellustunnus.
   */
  private static void lueProperties(final String id) {

    if (log.isLoggable(Level.FINEST)) {
      log.finest("lueProperties() - alkaa. Ladataan: '" + id + ".properties'.");
    }

    if (!props.containsKey(id)) {
      final String tiedosto = id + ".properties";
      final Properties p = TiedostoApu.lueProperties(tiedosto);

      if (p.size() == 0) {
        log.warning("lueProperties() - Properties on tyhjä: '" + tiedosto + "'.");
      }

      props.put(id, p);
    } else {
      log.info("lueProperties() - Properties-tiedosto on jo ladattu. Id=" + id);
    }

    if (log.isLoggable(Level.FINEST)) {
      log.finest("lueProperties() - alkaa. Ladataan: '" + id + ".properties'.");
    }
  }

  /**
   * Kielikohtaisten tekstien luku.
   */
  private static void lueTekstit() {

    for (String kieli : Vakiot.KIELET) {
      String tiedosto = "tekstit_" + kieli + ".properties";
      tekstit.put(kieli, TiedostoApu.lueProperties(tiedosto));
    }
  }
}
