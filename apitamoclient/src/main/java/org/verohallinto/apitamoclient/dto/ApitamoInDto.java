package org.verohallinto.apitamoclient.dto;

import org.verohallinto.apitamoclient.yleiset.Vakiot;

import javax.xml.soap.SOAPElement;
import java.util.Vector;

/**
 * <p>
 * Apitamo sanoman tiedot
 * </p>
 * (c) 2014 Tietokarhu Oy
 */
public class ApitamoInDto {

  private byte[] data; // ilmoitusdata
  private SOAPElement assertion; // tunnistuksesta saatu assertio
  private Vector<String> liiteTiedostot; // liitetiedostot
  private int suunta; // lähetyksen tyyppi 1 = aineiston lähetys, 2 =
                      // palautettavan aineiston nouto
  private String kieli; // Kieli, jolla vastaukset halutaan
  private String email; // Raksin ja palautettavien aineistoiden sähköposti

  public ApitamoInDto() {
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public SOAPElement getAssertion() {
    return assertion;
  }

  public void setAssertion(SOAPElement assertion) {
    this.assertion = assertion;
  }

  public Vector<String> getLiiteTiedostot() {
    return liiteTiedostot;
  }

  public void setLiiteTiedostot(Vector<String> liiteTiedostot) {
    this.liiteTiedostot = liiteTiedostot;
  }

  public int getSuunta() {
    return suunta;
  }

  public void setSuunta(int suunta) {
    this.suunta = suunta;
  }

  public String getKieli() {
    return kieli;
  }

  public void setKieli(String kieli) {
    this.kieli = kieli;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {

    return "ApitamoInDto{" + Vakiot.SYS_LF + "kieli='" + kieli + '\'' + Vakiot.SYS_LF + "data='" + data + '\''
        + Vakiot.SYS_LF + "assertion=" + assertion + Vakiot.SYS_LF + "liiteTiedostot='" + liiteTiedostot.toString()
        + '\'' + Vakiot.SYS_LF + "suunta='" + suunta + '\'' + Vakiot.SYS_LF + "email='" + email + '\'' + Vakiot.SYS_LF
        + '}';
  }
}
