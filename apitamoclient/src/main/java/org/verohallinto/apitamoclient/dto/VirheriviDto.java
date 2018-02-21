package org.verohallinto.apitamoclient.dto;

import org.verohallinto.apitamoclient.yleiset.Vakiot;

/**
 * <p>
 * Lomakkeen yksittäinen virherivi
 * </p>
 * (c) 2014 Tietokarhu Oy
 */
public class VirheriviDto {

  private String tunnus; // Tunnus, jota virhe koskee
  private String rivinro; // rivinro, jolta virhe löytyy
  private String tieto; // Virheellinen tieto
  private String selitys; // Virheselite
  private String laji; // Virheen laji

  public String getTunnus() {
    return tunnus;
  }

  public void setTunnus(String tunnus) {
    this.tunnus = tunnus;
  }

  public String getRivinro() {
    return rivinro;
  }

  public void setRivinro(String rivinro) {
    this.rivinro = rivinro;
  }

  public String getTieto() {
    return tieto;
  }

  public void setTieto(String tieto) {
    this.tieto = tieto;
  }

  public String getSelitys() {
    return selitys;
  }

  public void setSelitys(String selitys) {
    this.selitys = selitys;
  }

  public String getLaji() {
    return laji;
  }

  public void setLaji(String laji) {
    this.laji = laji;
  }

  @Override
  public String toString() {
    return "VirheriviDto{" + Vakiot.SYS_LF + "tunnus='" + tunnus + '\'' + Vakiot.SYS_LF + "rivinro=" + rivinro
        + Vakiot.SYS_LF + "tieto=" + tieto + Vakiot.SYS_LF + "selitys=" + selitys + Vakiot.SYS_LF + "laji=" + laji + Vakiot.SYS_LF + '}';
  }
}
