package org.verohallinto.apitamoclient.ui.yleiset;

import org.verohallinto.apitamoclient.ui.apu.Apuri;
import org.verohallinto.apitamoclient.ui.apu.TiedostoApu;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * </p>
 * (c) 2014 Tietokarhu Oy Date: 26.6.2014 - 10:18 $Rev$
 */
public class props {
  private static final Logger log = Logger.getLogger(props.class.getName());
  private static String sovellusId;

  // Tunnistustapakohtaiset tiedot
  private static Map<String, java.util.Properties> props;

  // Sekalaista
  private static boolean lokitaSanomat;
  private static String sanomaHakemisto;
  private static boolean puraTamoTulos;

  static {
    props = Collections.synchronizedMap(new HashMap<String, java.util.Properties>());

    // Sovelluksen tiedot
    sovellusId = Vakiot.SOVELLUS.toLowerCase();
    lueProperties(sovellusId);
  }

  private props() {

  }

  /**
   * Sovelluksen staattisten tietojen alustus.
   * </p>
   */
  public static void alusta() {
    lokitaSanomat = Apuri.tosi(anna(sovellusId, Vakiot.LOKITA_KATSO_SANOMAT));
    sanomaHakemisto = anna(sovellusId, Vakiot.SANOMA_TALLENNUS_HAKEMISTO);
    if (sanomaHakemisto.length() == 0) {
      sanomaHakemisto = System.getProperty("user.dir") + "/msg/";
    }

    File f = new File(sanomaHakemisto);
    if (!f.exists()) {
      f.mkdir();
    }

    puraTamoTulos = Apuri.tosi(anna(sovellusId, Vakiot.PURA_TAMO_TULOS));
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
      final java.util.Properties p = props.get(id);

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

  public static boolean lokitaSanomat() {

    return lokitaSanomat;
  }

  public static String annaSanomaHakemisto() {
    return sanomaHakemisto;
  }

  public static boolean puraTamoTulos() {
    return puraTamoTulos;
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
      final java.util.Properties p = TiedostoApu.lueProperties(tiedosto);

      if (p.size() == 0) {
        log.warning("lueProperties() - props on tyhjä: '" + tiedosto + "'.");
      }

      props.put(id, p);
    } else {
      log.info("lueProperties() - props-tiedosto on jo ladattu. Id=" + id);
    }

    if (log.isLoggable(Level.FINEST)) {
      log.finest("lueProperties() - alkaa. Ladataan: '" + id + ".properties'.");
    }
  }
}
