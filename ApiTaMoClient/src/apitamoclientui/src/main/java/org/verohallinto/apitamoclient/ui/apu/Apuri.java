package org.verohallinto.apitamoclient.ui.apu;

import org.verohallinto.apitamoclient.ui.yleiset.Vakiot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * <p>staattisia apumetodeita</p>
 * (c) 2013 Tietokarhu Oy
 */
public class Apuri {

    /**
     * Tutkii, onko annettu merkki numero (0-9).
     * </p>
     *
     * @param c <code>char</code> tutkittava merkki.
     * @return true, jos merkki on numero, muuten false.
     */
    public static boolean onNumero(final char c) {

        return c >= '0' && c <= '9';
    }

    /**
     * Tarkastaa onko, annettu parametri numeerinen.
     * </p>
     *
     * @param arvo {@code String} tarkastettava parametri.
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
     * Tutkii, onko annettu merkkijono numeerinen.
     * </p>
     *
     * @param s <code>String</code> tutkittava merkkijono.
     * @return true, jos merkkijono on numeerinen, muuten false.
     */
    public static boolean onNumeerinen(final String s) {

        if (s != null && s.length() > 0) {
            for (char c : s.toCharArray()) {
                if (c < '0' || c > '9') {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public static String annaAika(){
        SimpleDateFormat ISO8601UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        ISO8601UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        String now = ISO8601UTC.format(new Date());

    	return now;
    }

    /**
     * Tarkastaa onko annettu merkkijono hyväksyttävä true-arvo.
     * </p>
     *
     * @param s {@code String} tarkastettava merkkijono.
     * @return true, jos annettu argumentti on true-arvo, muuten false.
     */
    public static boolean tosi(final String s) {

        return "true".equalsIgnoreCase(s) || "1".equalsIgnoreCase(s);
    }

    /**
     * Palauttaa annetun poikkeuksen merkkijonona.
     * </p>
     *
     * @param e {@code Exception} merkkijonoksi purettav poikkeus.
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
}
