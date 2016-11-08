package org.verohallinto.apitamoclient.dto;

/**
 * <p>
 * </p>
 * (c) 2014 Tietokarhu Oy
 */
public class LiiteDto {

  private String liiteNimi;
  private boolean vastaanottoOk;
  private String info;

  public String getLiiteNimi() {
    return liiteNimi;
  }

  public void setLiiteNimi(String liiteNimi) {
    this.liiteNimi = liiteNimi;
  }

  public boolean isVastaanottoOk() {
    return vastaanottoOk;
  }

  public void setVastaanottoOk(boolean vastaanottoOk) {
    this.vastaanottoOk = vastaanottoOk;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }
}
