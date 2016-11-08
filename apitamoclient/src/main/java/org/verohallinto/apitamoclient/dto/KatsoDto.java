package org.verohallinto.apitamoclient.dto;

import org.verohallinto.apitamoclient.yleiset.Vakiot;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Katso-tunnistautumistiedot.
 * </p>
 * (c) 2013 Tietokarhu Oy
 * <p/>
 */
public class KatsoDto implements Serializable {

  private static final long serialVersionUID = -7721737194592608921L;
  // SASLRequest
  private String saslMsgID; // SASLRequest-sanomalle annettu sanomatunniste

  // AuthnRequest
  private String authnMsgID; // AuthnRequest-sanomalle annettu sanomatunniste
  private Date voimassa; // Tunnistautumisen viimeinen voimassaoloaika (nyt + 10
                         // min.)

  //
  private KatsoInDto in; // Sis��nkirjautumistiedot
  private KatsoOutDto out; // Kirjautumisen paluutiedot

  public KatsoDto() {

  }

  public String getSaslMsgID() {

    return saslMsgID;
  }

  public void setSaslMsgID(String saslMsgID) {

    this.saslMsgID = saslMsgID;
  }

  public String getAuthnMsgID() {

    return authnMsgID;
  }

  public void setAuthnMsgID(String authnMsgID) {

    this.authnMsgID = authnMsgID;
  }

  public Date getVoimassa() {

    return voimassa;
  }

  public void setVoimassa(Date voimassa) {

    this.voimassa = voimassa;
  }

  public KatsoInDto getIn() {

    return in;
  }

  public void setIn(KatsoInDto in) {

    this.in = in;
  }

  public KatsoOutDto getOut() {

    return out;
  }

  public void setOut(KatsoOutDto out) {

    this.out = out;
  }

  @Override
  public String toString() {

    return "KatsoDto{" + Vakiot.SYS_LF + "saslMsgID='" + saslMsgID + '\'' + Vakiot.SYS_LF + "authnMsgID='" + authnMsgID
        + '\'' + Vakiot.SYS_LF + "voimassa=" + voimassa + Vakiot.SYS_LF + "in=" + in + Vakiot.SYS_LF + "out=" + out
        + Vakiot.SYS_LF + '}';
  }
}
