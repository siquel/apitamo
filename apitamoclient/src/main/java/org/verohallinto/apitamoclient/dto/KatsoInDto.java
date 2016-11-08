package org.verohallinto.apitamoclient.dto;

import org.verohallinto.apitamoclient.yleiset.Vakiot;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import java.io.Serializable;

/**
 * <p>
 * Katso-sis��nkirjautumistiedot.
 * </p>
 * (c) 2014 Tietokarhu Oy
 * <p/>
 */
public class KatsoInDto implements Serializable {

  private static final long serialVersionUID = -2789820328087434310L;

  // n�m� asetettava ennen tunnistautumista
  private String kieli; // Ilmoitusten kieli (fi, sv tai en)
  private int tunnistustapa; // Tunnistautumisessa k�ytett�v� menetelm� esim.
                             // Katso PWD tai Katso-OTP
  private String kayttajatunnus; // Katso k�ytt�j�tunnus
  private String salasana; // Katso-salasana
  private String otpSalasana; // Vaihtuva otp-salasana

  // palveluiden osoitteet ja sanomien lokitus
  private String SASLURL; // SASL palvelun osoite
  private String WSIDPURL; // WSIDP palvelun osoite
  private String APITAMOURL; // APITAMO palvelun osoite
  private boolean logMessages; // Lokitetaanko l�hetett�v�t ja vastaanotettavat
                               // sanomat
  private String messageDirectory; // Hakemisto johon sanomat kirjoitetaan

  // Tamo tarkastustulos purku
  private boolean puraTamoTulos; // Puretaanko TaMon tarkastustulos listaksi

  // sis�iseen k�ytt��n
  private SOAPElement saslAssertion; // SASLRespond-sanoman
                                     // 'saml:Assertion'-elementti
  private SOAPMessage message; // ApiTaMolta saatu AuthnRequest sanoma

  public KatsoInDto() {

  }

  public String getKieli() {

    return kieli;
  }

  public void setKieli(String kieli) {

    this.kieli = kieli;
  }

  public int getTunnistustapa() {

    return tunnistustapa;
  }

  public void setTunnistustapa(int tunnistustapa) {

    this.tunnistustapa = tunnistustapa;
  }

  public String getKayttajatunnus() {

    return kayttajatunnus;
  }

  public void setKayttajatunnus(String kayttajatunnus) {

    this.kayttajatunnus = kayttajatunnus;
  }

  public String getSalasana() {

    return salasana;
  }

  public void setSalasana(String salasana) {

    this.salasana = salasana;
  }

  public String getOtpSalasana() {

    return otpSalasana;
  }

  public void setOtpSalasana(String otpSalasana) {

    this.otpSalasana = otpSalasana;
  }

  public SOAPElement getSaslAssertion() {

    return saslAssertion;
  }

  public void setSaslAssertion(SOAPElement saslAssertion) {

    this.saslAssertion = saslAssertion;
  }

  public SOAPMessage getMessage() {
    return message;
  }

  public void setMessage(SOAPMessage message) {
    this.message = message;
  }

  public String getSASLURL() {
    return SASLURL;
  }

  public void setSASLURL(String SASLURL) {
    this.SASLURL = SASLURL;
  }

  public String getWSIDPURL() {
    return WSIDPURL;
  }

  public void setWSIDPURL(String WSIDPURL) {
    this.WSIDPURL = WSIDPURL;
  }

  public boolean isLogMessages() {
    return logMessages;
  }

  public void setLogMessages(boolean logMessages) {
    this.logMessages = logMessages;
  }

  public String getAPITAMOURL() {
    return APITAMOURL;
  }

  public void setAPITAMOURL(String APITAMOURL) {
    this.APITAMOURL = APITAMOURL;
  }

  public String getMessageDirectory() {
    return messageDirectory;
  }

  public void setMessageDirectory(String messageDirectory) {
    this.messageDirectory = messageDirectory;
  }

  public boolean isPuraTamoTulos() {
    return puraTamoTulos;
  }

  public void setPuraTamoTulos(boolean puraTamoTulos) {
    this.puraTamoTulos = puraTamoTulos;
  }

  @Override
  public String toString() {

    return "KatsoInDto{" + Vakiot.SYS_LF + "kieli='" + kieli + '\'' + Vakiot.SYS_LF + "tunnistustapa=" + tunnistustapa
        + Vakiot.SYS_LF + "kayttajatunnus='" + kayttajatunnus + '\'' + Vakiot.SYS_LF + "salasana='" + salasana + '\''
        + Vakiot.SYS_LF + "otpSalasana='" + otpSalasana + '\'' + Vakiot.SYS_LF + '}';
  }
}
