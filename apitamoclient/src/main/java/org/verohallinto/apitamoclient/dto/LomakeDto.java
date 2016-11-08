package org.verohallinto.apitamoclient.dto;

import org.verohallinto.apitamoclient.yleiset.Vakiot;

import java.util.List;

/**
 * <p>
 * Yksittäisen lomakkeen tarkastustulos
 * </p>
 * (c) 2014 Tietokarhu Oy
 */
public class LomakeDto {

  private boolean lomakkeenTilaOk; // Lomakekohtainen tarkistuksen tila
  private String lomakeVuosi; // Lomakkeen vuosi(esim. tuloveroilmoituksilla ja
                              // vuosi-ilmoituksilla)
  private String selite; // Lomakkeen nimi
  private String tietueTunnus; // Lomakkeen tietuetunnus
  private String yTunnus; // Y-tunnus, jota ilmoitus koskee
  private List<VirheriviDto> virheet; // Lomakkeen virheet listattuna

  public String getLomakeVuosi() {
    return lomakeVuosi;
  }

  public void setLomakeVuosi(String lomakeVuosi) {
    this.lomakeVuosi = lomakeVuosi;
  }

  public boolean isLomakkeenTilaOk() {
    return lomakkeenTilaOk;
  }

  public void setLomakkeenTilaOk(boolean lomakkeenTilaOk) {
    this.lomakkeenTilaOk = lomakkeenTilaOk;
  }

  public String getSelite() {
    return selite;
  }

  public void setSelite(String selite) {
    this.selite = selite;
  }

  public String getTietueTunnus() {
    return tietueTunnus;
  }

  public void setTietueTunnus(String tietueTunnus) {
    this.tietueTunnus = tietueTunnus;
  }

  public String getyTunnus() {
    return yTunnus;
  }

  public void setyTunnus(String yTunnus) {
    this.yTunnus = yTunnus;
  }

  public List<VirheriviDto> getVirheet() {
    return virheet;
  }

  public void setVirheet(List<VirheriviDto> virheet) {
    this.virheet = virheet;
  }

  @Override
  public String toString() {
    return "LomakeDto{" + Vakiot.SYS_LF + "lomakkeenTilaOk='" + lomakkeenTilaOk + '\'' + Vakiot.SYS_LF + "lomakeVuosi="
        + lomakeVuosi + Vakiot.SYS_LF + "selite=" + selite + Vakiot.SYS_LF + "tietueTunnus=" + tietueTunnus
        + Vakiot.SYS_LF + "yTunnus='" + yTunnus + '\'' + Vakiot.SYS_LF + "virheet='" + virheet + '\'' + Vakiot.SYS_LF
        + '}';
  }
}
