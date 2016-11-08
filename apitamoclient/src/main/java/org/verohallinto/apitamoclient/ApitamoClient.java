package org.verohallinto.apitamoclient;

import org.verohallinto.apitamoclient.apu.Apuri;
import org.verohallinto.apitamoclient.dto.*;
import org.verohallinto.apitamoclient.katso.Kirjaudu;
import org.verohallinto.apitamoclient.katso.sanoma.Apitamo;
import org.verohallinto.apitamoclient.yleiset.Props;
import org.verohallinto.apitamoclient.yleiset.Vakiot;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * </p>
 * (c) 2014 Tietokarhu Oy Date: 25.6.2014 - 14:04 $Rev$
 */
public class ApitamoClient {
  private static final Logger log = Logger.getLogger(ApitamoClient.class.getName());

  private ApitamoClient() {
  }

  /**
   * Katso-sis��nkirjautumisen objektirajapinta.
   * </p>
   *
   * @param in
   *          {@code KatsoInDto} sis��nkirjautumistiedot.
   * @return true = kirjautuminen onnistui, false = kirjautuminen ep�onnistui.
   */
  public static KatsoOutDto katsoSisaan(final KatsoInDto in) {

    final KatsoDto katso = new KatsoDto();
    katso.setIn(in);
    katso.setOut(new KatsoOutDto(Vakiot.SOVELLUS, in.getTunnistustapa()));

    if (!Vakiot.KIELET.contains(in.getKieli())) {
      // Asetetaan oletuskieli
      in.setKieli(Vakiot.OLETUSKIELI);
    }

    // Alustetaan tunnistustapa
    Props.alusta(Vakiot.SOVELLUS, in);

    try {
      tarkastaKatso(in);
      // Kirjaudutaan sis��n
      Kirjaudu.sisaan(katso);
    } catch (Exception e) {
      katso.getOut().setVirhe(e.getMessage());
    }

    if (log.isLoggable(Level.INFO)) {
      log.info("katsoSisaan(): " + "Tunnus='" + katso.getIn().getKayttajatunnus() + "', tunnistus ok="
          + katso.getOut().onTunnistusOk() + ", vahva tunnistus=" + katso.getOut().onVahvaTunnistus()
          + (katso.getOut().getInfo() != null ? ", info='" + katso.getOut().getInfo() + "'" : "")
          + (katso.getOut().getVirhe() != null ? ", virhe='" + katso.getOut().getVirhe() + "'" : "") + ".");
    }

    return katso.getOut();
  }

  /**
   * Ilmoitusten l�hetys Apitamolle.
   * </p>
   *
   * @param in
   *          {@code ApitamoInDto} L�hetett�v�t tiedot
   * @return {@code ApitamoOutDto} ApiTaMon vastaus
   */
  public static ApitamoOutDto Laheta(ApitamoInDto in) {

    final ApitamoDto apitamo = new ApitamoDto();
    apitamo.setIn(in);
    apitamo.setOut(new ApitamoOutDto());

    apitamo.getOut().setSanomaHakemisto(Props.annaSanomaHakemisto());

    if (!Vakiot.KIELET.contains(in.getKieli())) {
      // Asetetaan oletuskieli
      in.setKieli(Vakiot.OLETUSKIELI);
    }

    if (in.getSuunta() != Vakiot.SUUNTA_LAHETYS && in.getSuunta() != Vakiot.SUUNTA_NOUTO) {
      // oletuksen l�hetell��n tietoa
      in.setSuunta(Vakiot.SUUNTA_LAHETYS);
    }

    try {
      tarkastaLahetysTiedot(apitamo);

      // Hoidetaan l�hetys
      SoapVastausDto vastaus = Apitamo.laheta(apitamo);

      if (vastaus.onOk()) {
        apitamo.getOut().setLahetysTapahtumaOk(true);
      }

    } catch (Exception e) {
      apitamo.getOut().setVirheMsg(e.getMessage());
    }

    return apitamo.getOut();
  }

  /**
   * Sis��nkirjautumistietojen tarkastus.
   * </p>
   *
   * @param apitamo
   *          {@code ApitamoDto} L�hetyksen tiedot.
   * @return {@code boolean} true = tiedot ok, muuten false
   */
  private static boolean tarkastaLahetysTiedot(final ApitamoDto apitamo) {

    if (apitamo.getIn() != null) {
      if (apitamo.getIn().getAssertion() == null
          || (apitamo.getIn().getData() == null && apitamo.getIn().getLiiteTiedostot().size() == 0)) {

        apitamo.getOut().setVirheMsg(Props.teksti(Vakiot.OLETUSKIELI, 402));
        return false;
      }
    } else {
      apitamo.getOut().setVirheMsg(Props.teksti(Vakiot.OLETUSKIELI, 402));
      return false;
    }

    return true;
  }

  /**
   * Sis��nkirjautumistietojen tarkastus.
   * </p>
   *
   * @param in
   *          {@code KatsoInDto} sis��nkirjautumistiedot.
   * @throws RuntimeException
   *           Kirjautumistiedot virheelliset.
   */
  private static void tarkastaKatso(final KatsoInDto in) {

    if (in != null) {
      final boolean ttOk = Vakiot.KATSO_TUNNISTUSTAVAT.contains(in.getTunnistustapa());

      if (Apuri.onTyhja(in.getKayttajatunnus()) || Apuri.onTyhja(in.getSalasana()) || !ttOk
          || (in.getTunnistustapa() == Vakiot.KATSO_OTP && Apuri.onTyhja(in.getOtpSalasana()))) {

        String ex = "Virheelliset tunnistautumistiedot:" + ", tunnistustapa=" + annaTapa(in.getTunnistustapa(), ttOk)
            + ", k�ytt�j�tunnus=" + annaPakollinen(in.getKayttajatunnus()) + ", salasana="
            + annaSalasana(in.getSalasana());

        if (in.getTunnistustapa() == Vakiot.KATSO_OTP) {
          ex += ", vaihtuvasalasana=" + annaSalasana(in.getOtpSalasana() + ".");
        } else {
          ex += '.';
        }

        throw new RuntimeException(ex);
      }
    } else {
      // txt.018=Tunnistautumistiedot on tyhj�.
      throw new RuntimeException(Props.teksti(Vakiot.OLETUSKIELI, 18));
    }

  }

  /**
   * Pakollisen tiedon tarkastus ja ilmoituksen palautus.
   *
   * @param s
   *          {@code String} tarkastettava pakollinen tieto.
   * @return Tiedon status merkkijonona.
   */
  private static String annaPakollinen(final String s) {

    return "'" + s + "' [" + Props.teksti(Vakiot.OLETUSKIELI, (Apuri.onTyhja(s) ? 16 : 17)) + "]";
  }

  /**
   * Pakollisen salasanatiedon tarkastus ja ilmoituksen palautus.
   *
   * @param s
   *          {@code String} tarkastettava pakollinen salasanatieto.
   * @return Tiedon status merkkijonona.
   */
  private static String annaSalasana(final String s) {

    final boolean on = Apuri.onTyhja(s);

    return "'" + (on ? s : "********") + "' [" + Props.teksti(Vakiot.OLETUSKIELI, (on ? 16 : 17)) + "]";
  }

  /**
   * Pakollisen tunnistustavan tarkastus ja ilmoituksen palautus.
   *
   * @param tapa
   *          {@code int} tarkastettava pakollinen tunnistustapa.
   * @param ttOk
   *          {@code boolean} tiedon tila.
   * @return Tiedon status merkkijonona.
   */
  private static String annaTapa(final int tapa, final boolean ttOk) {

    final int nro = (!ttOk ? 16 : 17);

    return String.valueOf(tapa) + " [" + Props.teksti(Vakiot.OLETUSKIELI, nro) + "]";
  }
}
