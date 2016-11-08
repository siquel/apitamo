package org.verohallinto.apitamoclient.dto;

import org.verohallinto.apitamoclient.yleiset.Vakiot;

import javax.xml.soap.SOAPElement;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Katso-sis��n-/uloskirjautumisen paluutiedot.
 * </p>
 * (c) 2014 Tietokarhu Oy
 * <p/>
 */
public class KatsoOutDto implements Serializable {

  private static final long serialVersionUID = 8423746017358557131L;
  public static final String VIESTI_EROTIN = "@@";
  private String tunnistusId; // K�ytett�v�n tunnistustavan id. Esim.
                              // 'jakelin-sftp'
  private int tunnistustapa; // Tunnistautumisessa k�ytett�v� menetelm� esim.
                             // Katso PWD, Katso-OTP, KTYVI...
  private boolean vahvaTunnistus; // true = tunnistauduttu vahvalla
                                  // tunnistusmenetelm�ll�, false = heikompi
                                  // tunnistautuminen
  private String tfiKid; // K�ytt�j�tunnus, jolla on kirjauduttu palveluun
  private String tfiPersonname; // K�ytt�j�n nimi
  private String tfiVersion; // Ohjelmistoversio?
  private String info; // Mahdollinen info-tieto esim. otp-vaihtuvien
                       // salasanojen loppumisesta
  private String virhe; // Virheilmoitus tunnistautumisen ep�onnistumisesta
  private SOAPElement assertion; // AuthnRespond-sanoman
                                 // 'saml:Assertion'-elementti. V�litet��n
                                 // l�hetyksen yhteydess� ApiTaMolle
  private Date assertionDeadline; // kellonaina, jolloin assertio vanhenee
  private boolean tunnistusOk; // true = k�ytt�j� on sis��nkirjautunut, false =
                               // kirjautuminen ep�onnistui

  // syit� tunnistuksen ep�onnistumiseen
  private boolean otpVirhe; // OTP-salasanassa on virhe
  private boolean salasanaVirhe; // Kiinte�ss� salasanassa on virhe
  private boolean katsoIdVirhe; // katsoID:ss� on jotain h�ikk��

  public KatsoOutDto() {

  }

  public KatsoOutDto(String tunnistusId, int tunnistustapa) {

    this.tunnistusId = tunnistusId;
    this.tunnistustapa = tunnistustapa;
  }

  public boolean onTunnistusOk() {

    return tunnistusOk;
  }

  public String getTunnistusId() {

    return tunnistusId;
  }

  public void setTunnistusId(String tunnistusId) {

    this.tunnistusId = tunnistusId;
  }

  public int getTunnistustapa() {

    return tunnistustapa;
  }

  public void setTunnistustapa(int tunnistustapa) {

    this.tunnistustapa = tunnistustapa;
  }

  public void setTunnistusOk(boolean tunnistusOk) {

    this.tunnistusOk = tunnistusOk;
  }

  public boolean onVahvaTunnistus() {

    return vahvaTunnistus;
  }

  public void setVahvaTunnistus(boolean vahvaTunnistus) {

    this.vahvaTunnistus = vahvaTunnistus;
  }

  public String getTfiKid() {

    return tfiKid;
  }

  public void setTfiKid(String tfiKid) {

    this.tfiKid = tfiKid;
  }

  public String getTfiPersonname() {

    return tfiPersonname;
  }

  public void setTfiPersonname(String tfiPersonname) {

    this.tfiPersonname = tfiPersonname;
  }

  public String getTfiVersion() {

    return tfiVersion;
  }

  public void setTfiVersion(String tfiVersion) {

    this.tfiVersion = tfiVersion;
  }

  public String getInfo() {

    return info;
  }

  public void setInfo(String info) {

    this.info = (this.info == null ? info : this.info + VIESTI_EROTIN + info);
  }

  public String getVirhe() {

    return virhe;
  }

  public void setVirhe(String virhe) {

    this.virhe = (this.virhe == null ? virhe : this.virhe + VIESTI_EROTIN + virhe);
  }

  public SOAPElement getAssertion() {
    return assertion;
  }

  public void setAssertion(SOAPElement assertion) {
    this.assertion = assertion;
  }

  public Date getAssertionDeadline() {
    return assertionDeadline;
  }

  public void setAssertionDeadline(Date assertionDeadline) {
    this.assertionDeadline = assertionDeadline;
  }

  public boolean isSalasanaVirhe() {
    return salasanaVirhe;
  }

  public void setSalasanaVirhe(boolean salasanaVirhe) {
    this.salasanaVirhe = salasanaVirhe;
  }

  public boolean isOtpVirhe() {
    return otpVirhe;
  }

  public void setOtpVirhe(boolean otpVirhe) {
    this.otpVirhe = otpVirhe;
  }

  public boolean isKatsoIdVirhe() {
    return katsoIdVirhe;
  }

  public void setKatsoIdVirhe(boolean katsoIdVirhe) {
    this.katsoIdVirhe = katsoIdVirhe;
  }

  @Override
  public String toString() {

    return "KatsoOutDto{" + Vakiot.SYS_LF + "tunnistusId='" + tunnistusId + '\'' + Vakiot.SYS_LF + "tunnistustapa="
        + tunnistustapa + Vakiot.SYS_LF + "tunnistusOk=" + tunnistusOk + Vakiot.SYS_LF + "vahvaTunnistus="
        + vahvaTunnistus + Vakiot.SYS_LF + "tfiKid='" + tfiKid + '\'' + Vakiot.SYS_LF + "tfiPersonname='"
        + tfiPersonname + '\'' + Vakiot.SYS_LF + "tfiVersion='" + tfiVersion + '\'' + Vakiot.SYS_LF + "info='" + info
        + '\'' + Vakiot.SYS_LF + "virhe='" + virhe + '\'' + Vakiot.SYS_LF + '}';
  }
}
