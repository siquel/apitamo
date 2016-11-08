package org.verohallinto.apitamoclient.yleiset;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * <p>
 * Sovelluksen vakiot.
 * </p>
 * (c) 2013 Tietokarhu Oy
 */
public class Vakiot {

  public static final int SUUNTA_LAHETYS = 1;
  public static final int SUUNTA_NOUTO = 2;

  public static final String SYS_LF = System.getProperty("line.separator");
  // Properties-tiedot
  public static final String SOVELLUS = "apitamo";
  public static final String OLETUSKIELI = "fi";
  public static final List<String> KIELET = Arrays.asList("fi", "sv", "en");
  public static final Locale LOCALE = new Locale("fi", "FI");
  public static final int TEKSTITUNNISTE_PIT = 3;
  //
  public static final String KATSO_AIKAVYOHYKE = "UTC"; // Katso-aikaleimoissa
                                                        // käytettävä
                                                        // aikavyöhyke
  public static final String KATSO_AIKALEIMA = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; // Katso-sanomissa
                                                                               // käytettävän
                                                                               // aikaleiman
                                                                               // muoto
  //
  public static final String SANOMAMERKISTO = "UTF-8"; // Sanomissa käytettävä
                                                       // merkistö
  public static final String SOAP_ENV_CLIENT = "SOAP-ENV:Client"; // Virhesanoman
                                                                  // koodi
  public static final String SOAP_ENV_SERVER = "SOAP-ENV:Server"; // Virhesanoman
                                                                  // koodi
  public static final Charset ISO88591_CHARSET = Charset.forName("ISO-8859-1");
  public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
  // Tunnistautumistavat
  public static final int KATSO_OTP = 1; // Katso-tunnistautumismenetelmä Katso
                                         // OTP
  public static final int KATSO_PWD = 2; // Katso-tunnistautumismenetelmä Katso
                                         // PWD
  public static final List<Integer> KATSO_TUNNISTUSTAVAT = Arrays.asList(KATSO_OTP, KATSO_PWD);
  public static final String KATSO_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
  public static final char KATSO_SASL_EROTIN = 0x00; // SaslRequest-sanoman
                                                     // tunnistautumismerkkijonon
                                                     // erotinmerkki

  // Katso-sanomatyypit
  public static final int KATSO_PYYNTO_SASL = 1; // Sanomatyyppi = SaslRequest
  public static final int KATSO_PYYNTO_WSIDP = 2; // Sanomatyyppi = AuthnRequest
  public static final int APITAMO_PYYNTO = 4; // Sanomatyyppi = Apitamo pyynto
  // Katso-sanomapohjat
  public static final String KATSO_XML_VIRHE = "katso.soap.virhe.pohja";
  public static final String KATSO_XML_SASL = "katso.sasl.request.pohja";
  public static final String KATSO_XML_WSIDP = "katso.wsidp.request.pohja";
  public static final String APITAMO_POHJA = "apitamo.sanoma.pohja";
  public static final String APITAMO_PYYNTO_POHJA = "apitamo.pyynto.sanoma.pohja";
}
