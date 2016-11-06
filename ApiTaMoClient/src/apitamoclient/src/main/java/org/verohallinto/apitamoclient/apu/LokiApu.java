package org.verohallinto.apitamoclient.apu;

import org.verohallinto.apitamoclient.yleiset.Props;
import org.verohallinto.apitamoclient.yleiset.Vakiot;
import org.w3c.dom.Document;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import java.io.*;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * <p>Staattisia apumetodeita sanomien lokitukseen.</p>
 * (c) 2013 Tietokarhu Oy
 * <p/>
 */
public class LokiApu {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(TiedostoApu.class.getName());
    private static int jnro = 0; // Lokitettavan sanoman järjestysnumero

    private LokiApu() {

    }

    /**
     * Pyyntösanoman tyypin mukaisen etuliitteen palauteu.
     * </p>
     *
     * @param tyyppi {@code int} sanoman tyyppi.
     * @return Pyyntösanoman mukainen etuliite.
     */
    public static String annaEtuliite(final int tyyppi) {

        if (tyyppi == Vakiot.KATSO_PYYNTO_SASL) {
            return "SASL-";
        } else if (tyyppi == Vakiot.KATSO_PYYNTO_WSIDP) {
            return "WSIDP-";
        } else if (tyyppi == Vakiot.APITAMO_PYYNTO) {
            return "APITAMO-";
        }

        return "XXXX-";
    }

    /**
     * Sanoman debug-lokitus tiedostoon.
     * </p>
     *
     * @param tiedosto {@code String} kirjoitettava tiedosto.
     * @param sanoma   {@code SOAPMessage} lokitettava sanoma.
     */
    public static String lokitaSanoma(final String tiedosto, final SOAPMessage sanoma) {

        if (log.isLoggable(Level.FINEST)) {
            log.finest("lokitaSanoma() - alkaa: " + tiedosto);
        }

        BufferedWriter bw = null;
        String nimi = "";

        try {
            nimi = Props.annaSanomaHakemisto() + annaLokiJnro() + "-" + tiedosto;
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nimi), Vakiot.SANOMAMERKISTO));

            if (sanoma != null) {
                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                sanoma.writeTo(bos);
                final String s = new String(bos.toByteArray(), Vakiot.SANOMAMERKISTO);

                if (log.isLoggable(Level.FINEST)) {
                    log.finest("lokitaSanoma() - Sanoman pituus=" + s.length());
                }

                bw.write(s);
            } else {
                bw.write("null");
            }

            bw.flush();

        } catch (Exception e) {
            log.severe(Apuri.annaPoikkeus(e));
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignore) {
                }

                bw = null;
            }
        }

        if (log.isLoggable(Level.FINEST)) {
            log.finest("lokitaSanoma() - loppu.");
        }

        return nimi;
    }

    /**
     * Elementin debug-lokitus tiedostoon.
     * </p>
     *
     * @param tiedosto  {@code String} kirjoitettava tiedosto.
     * @param elementti {@code SOAPElement} lokitettava elementti.
     */
    public static void lokitaElementti(final String tiedosto, final SOAPElement elementti) {

        if (elementti != null) {
            BufferedWriter bw = null;

            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(annaLokiJnro() + "-" + tiedosto),
                                                               Vakiot.SANOMAMERKISTO));
                bw.write(SanomaApu.elementtiMerkkijonoksi(elementti));
                bw.flush();
            } catch (Exception e) {
                log.severe(Apuri.annaPoikkeus(e));
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException ignore) {
                    }

                    bw = null;
                }
            }
        } else {
            log.finest("lokitaElementti(): Elementti on tyhjä.");
        }
    }

    /**
     * XML-dokumentin debug-lokitus tiedostoon.
     * </p>
     *
     * @param tiedosto {@code String} kirjoitettava tiedosto.
     * @param doc      {@code SOAPElement} lokitettava dokumentti.
     */
    public static void lokitaDokumentti(final String tiedosto, final Document doc) {

        if (doc != null) {
            BufferedWriter bw = null;

            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(annaLokiJnro() + "-" + tiedosto),
                                                               Vakiot.SANOMAMERKISTO));
                bw.write(SanomaApu.dokumenttiMerkkijonoksi(doc));
                bw.flush();
            } catch (Exception e) {
                log.severe(Apuri.annaPoikkeus(e));
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException ignore) {
                    }

                    bw = null;
                }
            }
        } else {
            log.severe("lokitaElementti(): Elementti on tyhjä.");
        }
    }

    public static void lokitaOtsikot(final MimeHeaders headers, final int tyyppi) {

        log.info("lokitaOtsikot() - alkaa. Tyyppi=" + tyyppi);

        if (headers != null) {
            final Iterator iter = headers.getAllHeaders();

            while (iter.hasNext()) {
                MimeHeader header = (MimeHeader)iter.next();
                log.info("\t'" + header.getName() + "'='" + header.getValue() + "'");
            }
        } else {
            log.info("lokitaOtsikot() - Ei otsikkotietoja.");
        }

        log.info("lokitaOtsikot() - loppu.");
    }

    /**
     * Debug-lokien järjestysnomeron käsittely.
     * </p>
     *
     * @return Järjestyksessä seuraava lokinumero.
     */
    private static synchronized String annaLokiJnro() {

        if (jnro >= Integer.MAX_VALUE) {
            jnro = 0;
        }

        jnro++;

        return jnro < 10 ? "0" + jnro : String.valueOf(jnro);
    }
}
