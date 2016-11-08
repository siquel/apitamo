package org.verohallinto.apitamoclient.katso;

import org.verohallinto.apitamoclient.dto.ApitamoInDto;
import org.verohallinto.apitamoclient.dto.KatsoDto;
import org.verohallinto.apitamoclient.dto.KatsoOutDto;
import org.verohallinto.apitamoclient.dto.SoapVastausDto;
import org.verohallinto.apitamoclient.yleiset.Vakiot;
import org.verohallinto.apitamoclient.katso.sanoma.Apitamo;
import org.verohallinto.apitamoclient.katso.sanoma.Sasl;
import org.verohallinto.apitamoclient.katso.sanoma.Wsidp;

import javax.xml.soap.SOAPMessage;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * <p>
 * Katso-sisään- ja uloskirjautuminen.
 * </p>
 * (c) 2013 Tietokarhu Oy
 * <p/>
 */
public class Kirjaudu {

  private static final Logger log = Logger.getLogger(Kirjaudu.class.getName());

  private Kirjaudu() {

  }

  /**
   * Katso-sisäänkirjautuminen.
   * </p>
   *
   * @param katso
   *          {@code KatsoDto} Katso-kirjautumistiedot.
   */
  public static boolean sisaan(final KatsoDto katso) {

    boolean ok;
    SOAPMessage sm = null;
    final SoapVastausDto saslVastaus = Sasl.laheta(katso);

    if ((ok = saslVastaus.onOk())) {
      final SoapVastausDto apitamoVastaus = Apitamo.kirjaudu(annaTyhjatApitamoTiedot(), katso);

      final SoapVastausDto wsidpVastaus = Wsidp.laheta(katso);

      if ((ok = wsidpVastaus.onOk())) {
        // Sisäänkirjautuminen ok
        katso.getOut().setTunnistusOk(true);
      }
    }

    return ok;
  }

  /**
   * Katso-uloskirjautuminen.
   * </p>
   *
   * @param dto
   *          {@code KatsoDto} Katso-kirjautumistiedot.
   */
  public static void ulos(final KatsoDto dto) {

    dto.setOut(new KatsoOutDto(Vakiot.SOVELLUS, dto.getOut().getTunnistustapa()));

  }

  private static ApitamoInDto annaTyhjatApitamoTiedot() {
    ApitamoInDto dto = new ApitamoInDto();

    dto.setData("tyhja".getBytes());
    dto.setAssertion(null);
    dto.setKieli("fi");
    dto.setLiiteTiedostot(new Vector<String>());
    dto.setSuunta(1);

    return dto;
  }
}
