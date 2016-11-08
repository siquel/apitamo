package org.verohallinto.apitamoclient.apu;

import org.verohallinto.apitamoclient.yleiset.Base64;
import org.verohallinto.apitamoclient.yleiset.Vakiot;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * <p>
 * staattisia apumetodeita
 * </p>
 * (c) 2013 Tietokarhu Oy
 */
public class Apuri {

  private static final String NOLLAT = "00000000000000000000";

  public static String annaAika() {

    SimpleDateFormat ISO8601UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    ISO8601UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
    String now = ISO8601UTC.format(new Date());

    return now;
  }

  public static String annaID() {
    String ID = "";
    StringBuffer sbuf = new StringBuffer();
    Random rand = new Random();
    byte[] data = null;

    try {
      // luodaan 30 numeroinen sarja
      for (int i = 0; i < 30; i++) {
        sbuf.append(rand.nextInt(10));
      }
      data = sbuf.toString().getBytes();
      ByteArrayOutputStream out_bytes = new ByteArrayOutputStream();
      OutputStream out = new Base64.OutputStream(out_bytes);
      out.write(data);
      out.close();

      ID = out_bytes.toString();
      return ID;
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  public static String decode(String strData) {
    try {
      byte[] data = null;

      data = strData.getBytes();

      ByteArrayOutputStream out_bytes = new ByteArrayOutputStream();
      OutputStream out = new Base64.OutputStream(out_bytes, Base64.DECODE);
      out.write(data);
      out.close();

      return out_bytes.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  /**
   * Palauttaa annetun poikkeuksen merkkijonona.
   * </p>
   *
   * @param e
   *          {@code Exception} merkkijonoksi purettav poikkeus.
   * @return Poikkeus merkkijonona.
   */
  public static String annaPoikkeus(final Exception e) {

    if (e != null) {
      final StringBuilder sb = new StringBuilder(4096);
      sb.append(e.getClass().getCanonicalName()).append(": ");
      sb.append(e.getMessage()).append(Vakiot.SYS_LF);
      final StackTraceElement[] ste = e.getStackTrace();

      for (StackTraceElement element : ste) {
        sb.append('\t');
        sb.append(element.getClassName());
        sb.append(".<");
        sb.append(element.getMethodName());
        sb.append("> (");
        sb.append(element.getFileName());
        sb.append(':');
        sb.append(element.getLineNumber());
        sb.append(')');
        sb.append(Vakiot.SYS_LF);
      }

      return sb.toString();
    }

    return Vakiot.class.getCanonicalName() + ".annaPoikkeus(): Poikkeus on tyhjä.";
  }

  /**
   * Tarkastaa onko annettu merkkijono hyväksyttävä true-arvo.
   * </p>
   *
   * @param s
   *          {@code String} tarkastettava merkkijono.
   * @return true, jos annettu argumentti on true-arvo, muuten false.
   */
  public static boolean tosi(final String s) {

    return "true".equalsIgnoreCase(s) || "1".equalsIgnoreCase(s);
  }

  /**
   * Tarkastaa onko, annettu parametri numeerinen.
   * </p>
   *
   * @param arvo
   *          {@code String} tarkastettava parametri.
   * @return true, jos annettu parametri on numeerinen, muuten false.
   */
  public static boolean onNumero(final String arvo) {

    if (arvo != null && arvo.length() > 0) {
      for (char c : arvo.toCharArray()) {
        if (c < '0' || c > '9') {
          return false;
        }
      }

      return true;
    }

    return false;
  }

  /**
   * Etunollien lisäys annettuun numeroon.
   * </p>
   *
   * @param nro
   *          {@code int} etunollitettava numero.
   * @param koko
   *          {@code int} haluttu tiedon pituus etunollineen.
   * @return Annettu numero etunollitettuna.
   */
  public static String etunollat(final int nro, final int koko) {

    final String s = String.valueOf(nro);
    final int x = koko - s.length();

    if (x > 0 && x <= NOLLAT.length()) {
      return NOLLAT.substring(0, x) + s;
    }

    return s;
  }

  /**
   * Tarkastaa onko annettu merkkijono null tai tyhjää täynnä.
   * </p>
   *
   * @param s
   *          {@code String} tarkastettava merkkijono.
   * @return true = merkkijono on tyhjä, muuten false.
   */
  public static boolean onTyhja(final String s) {

    return s == null || s.trim().length() == 0;
  }

  /**
   * Muodostaa annetun pituuden mukaisen sanoma-id:n.
   * </p>
   *
   * @param pituus
   *          {@code int} muodostettavan id:n pituus.
   * @return Satunnaisluku id.
   */
  public static synchronized String annaId(final int pituus) {

    String paluu;
    final StringBuilder sb = new StringBuilder(pituus);
    final Random rand = new Random(System.currentTimeMillis());

    // Luodaan pyydetyn pituinen numerosarja
    for (int i = 0; i < pituus; i++) {
      sb.append(rand.nextInt(10));
    }

    try {
      paluu = Base64.encode64(sb.toString());
      Thread.sleep(11);
    } catch (Exception e) {
      paluu = null;
    }

    return paluu;
  }

  /**
   * Muodostaa formatoidun Katso-aikaleiman nykyhetkestä.
   * </p>
   *
   * @return Formatoitu aikaleima.
   */
  public static String annaKatsoAikaleima() {

    final SimpleDateFormat iso8601utc = new SimpleDateFormat(Vakiot.KATSO_AIKALEIMA);
    iso8601utc.setTimeZone(TimeZone.getTimeZone(Vakiot.KATSO_AIKAVYOHYKE));

    return iso8601utc.format(new Date());
  }

  /**
   * Tarkastaa, että annettuun taulukkoon on asetettu oikea määrä arvoja.
   * </p>
   *
   * @param ss
   *          {@code String}[] tarkastettavat arvot.
   * @param koko
   *          {@code int} arvojen oletettu määrä.
   * @return true = taulukossa on oikea määrä arvoje, false = arvojen määrä on
   *         virheellinen.
   */
  public static boolean onArvot(final String[] ss, final int koko) {

    if (ss != null && ss.length == koko) {
      for (String s : ss) {
        if (s == null || s.trim().length() == 0) {
          return false;
        }
      }

      return true;
    }

    return false;
  }

  /**
   * Katso-aikaleiman muunto Date-objektiksi.
   * </p>
   *
   * @param aikaleima
   *          {@code String} muunnettava aikaleima.
   * @return Päiväys jos muunto onnistui, muuten null.
   */
  public static Date annaKatsoAika(final String aikaleima) {

    final SimpleDateFormat iso8601utc = new SimpleDateFormat(Vakiot.KATSO_AIKALEIMA);
    iso8601utc.setTimeZone(TimeZone.getTimeZone(Vakiot.KATSO_AIKAVYOHYKE));

    try {
      return iso8601utc.parse(aikaleima);
    } catch (ParseException e) {

    }

    return null;
  }

  /**
   * UTF-8 markistön konvertointi ISO-8859-1 muotoon.
   * </p>
   *
   * @param s
   *          {@code String} konvertoitava merkkijono.
   * @return ISO8859-1 muotoon konvertoitu merkkijono.
   */
  public static String utf82Iso88591(final String s) {

    if (s != null && s.length() > 0) {
      return new String(s.getBytes(Vakiot.UTF8_CHARSET), Vakiot.ISO88591_CHARSET);
    }

    return s;
  }

  /**
   * ISO-8859-1 markistön konvertointi UTF-8 muotoon.
   * </p>
   *
   * @param s
   *          {@code String} konvertoitava merkkijono.
   * @return UTF-8 muotoon konvertoitu merkkijono.
   */
  public static String iso885912Utf8(final String s) {

    if (s != null && s.length() > 0) {
      return new String(s.getBytes(Vakiot.ISO88591_CHARSET), Vakiot.UTF8_CHARSET);
    }

    return s;
  }
}
