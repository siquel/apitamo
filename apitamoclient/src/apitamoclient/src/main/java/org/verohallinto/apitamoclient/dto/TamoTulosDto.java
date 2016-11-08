package org.verohallinto.apitamoclient.dto;

import org.verohallinto.apitamoclient.yleiset.Vakiot;

import java.util.List;

/**
 * <p>Koko aineiston tarkastustulos</p>
 * (c) 2014 Tietokarhu Oy
 */
public class TamoTulosDto {


    private boolean tarkistuksenTulosOk;    // koko tarkistuksen tulos.
    private long tietueKpl;                 // Ilmoituksia yhteensä
    private long oikeellisia;               // Oikeellisia ilmoituksia yhteensä
    private long virheellisia;              // Virheellisia ilmoituksia yhteensä
    private List<LomakeDto> lomakkeet;      // Lista aineiston lomakkeista

    public boolean isTarkistuksenTulosOk() {
        return tarkistuksenTulosOk;
    }

    public void setTarkistuksenTulosOk(boolean tarkistuksenTulosOk) {
        this.tarkistuksenTulosOk = tarkistuksenTulosOk;
    }

    public long getTietueKpl() {
        return tietueKpl;
    }

    public void setTietueKpl(long tietueKpl) {
        this.tietueKpl = tietueKpl;
    }

    public long getVirheellisia() {
        return virheellisia;
    }

    public void setVirheellisia(long virheellisia) {
        this.virheellisia = virheellisia;
    }

    public long getOikeellisia() {
        return oikeellisia;
    }

    public void setOikeellisia(long oikeellisia) {
        this.oikeellisia = oikeellisia;
    }

    public List<LomakeDto> getLomakkeet() {
        return lomakkeet;
    }

    public void setLomakkeet(List<LomakeDto> lomakkeet) {
        this.lomakkeet = lomakkeet;
    }

    @Override
    public String toString() {
        return "TamoTulosDto{" + Vakiot.SYS_LF +
               "tarkistuksenTulosOk='" + tarkistuksenTulosOk + '\'' + Vakiot.SYS_LF +
               "tietueKpl=" + tietueKpl + Vakiot.SYS_LF +
               "oikeellisia=" + oikeellisia + Vakiot.SYS_LF +
               "virheellisia=" + virheellisia + Vakiot.SYS_LF +
               "lomakkeet='" + lomakkeet + '\'' + Vakiot.SYS_LF +
               '}';
    }
}
