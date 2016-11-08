package org.verohallinto.apitamoclient.apu;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Staattisia apumetodeja jar-paketin sisäisten tiedostojen käsittelyyn.</p>
 * (c) 2013 Tietokarhu Oy
 * <p/>
 */
public class TiedostoApu {

    private static final Logger log = Logger.getLogger(TiedostoApu.class.getName());
    private static final int PUSKURIN_KOKO = 2048;

    private TiedostoApu() {

    }

    /**
     * Lukee annetun properties-tiedoton luokkapolusta.
     * </p>
     *
     * @param tiedosto {@code String} luettava properties-tiedosto.
     * @return Properties-tiedot.
     */
    public static Properties lueProperties(final String tiedosto) {

        final Properties props = new Properties();

        try {
            props.load(new LuokkaApu().annaLataaja().getResourceAsStream(tiedosto));
        } catch (Exception e) {

        }

        return props;
    }

    /**
     * Luekee tiedoston luokkapolusta ja palauttaa sen merkkijonona.
     * </p>
     *
     * @param tiedosto {@code String} etsittävä tiedosto.
     * @return Tiedoston sisältö merkkijonona, jos löytyi, muuten null.
     */
    public static String luePolusta(final String tiedosto) {

        if (log.isLoggable(Level.FINEST)) {
            log.finest("haeTiedosto() - alkaa: tiedosto=" + tiedosto);
        }

        InputStream is = null;
        String paluu;

        try {
            is = new LuokkaApu().annaLataaja().getResourceAsStream(tiedosto);
            final StringBuilder sb = new StringBuilder(PUSKURIN_KOKO);
            byte[] tavut = new byte[PUSKURIN_KOKO];
            int lkm;

            while ((lkm = is.read(tavut)) != -1) {
                sb.append(new String(tavut, 0, lkm));
            }

            paluu = sb.toString();
        } catch (Exception e) {
            paluu = null;

            log.severe(Apuri.annaPoikkeus(e));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }

                is = null;
            }
        }

        if (log.isLoggable(Level.FINEST)) {
            log.finest("haeTiedosto() - loppu.");
        }
        return paluu;
    }
}
