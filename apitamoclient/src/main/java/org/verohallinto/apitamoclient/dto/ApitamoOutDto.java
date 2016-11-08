package org.verohallinto.apitamoclient.dto;

import javax.xml.soap.SOAPMessage;
import java.util.List;

/**
 * <p>
 * </p>
 * (c) 2014 Tietokarhu Oy
 */
public class ApitamoOutDto {

  private boolean lahetysTapahtumaOk; // itse lähetystapahtuman tila
  private String virheMsg; // itse lähetystapahtumassa sattuneen virheen syy
  private boolean aineistonVastaanottoOk; // Ilmoitusten vastaanotto
  private boolean liitteitaVastaanotettu; // Yksi tai useampi liitetiedosto
                                          // otettu vastaan. Tarkempi erittely
                                          // löytyy "liitteet" -listasta.
  private List<LiiteDto> liitteet; // Liitteiden vastaanotto
  private String info; // Syy ilmoitusten hylkäämiseen
  private String aikaleima; // vastaanoton/hylkäämisen aikaleima
  private String checksum; // Ilmoitussisällön tarkistussumma
  private String ilmoitusTunniste; // mahdollinen RAKSI-ilmoitustunniste
  private String noutoTunniste; // Palautettavien aineistojen noutotunniste
  private TamoTulosDto tamoTulos; // TaMon palauttama tarkastustulos purettuna
  private String vastausSanomanNimi; // Sanoman nimi, joka sisältää varsinaisen
                                     // Apitamon vastauksen
  private String sanomaHakemisto; // Hakemisto, johon SOAP-sanomat kirjoitetaan
  private SOAPMessage vastausSanoma; // Apitamon vastaussanoma "raakana"

  // Aineiston nouto
  private List<String> ilmoitukset; // Noudetut aineistot listana

  public ApitamoOutDto() {
  }

  public boolean isLahetysTapahtumaOk() {
    return lahetysTapahtumaOk;
  }

  public void setLahetysTapahtumaOk(boolean lahetysTapahtumaOk) {
    this.lahetysTapahtumaOk = lahetysTapahtumaOk;
  }

  public boolean isAineistonVastaanottoOk() {
    return aineistonVastaanottoOk;
  }

  public void setAineistonVastaanottoOk(boolean aineistonVastaanottoOk) {
    this.aineistonVastaanottoOk = aineistonVastaanottoOk;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getAikaleima() {
    return aikaleima;
  }

  public void setAikaleima(String aikaleima) {
    this.aikaleima = aikaleima;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public String getIlmoitusTunniste() {
    return ilmoitusTunniste;
  }

  public void setIlmoitusTunniste(String ilmoitusTunniste) {
    this.ilmoitusTunniste = ilmoitusTunniste;
  }

  public String getNoutoTunniste() {
    return noutoTunniste;
  }

  public void setNoutoTunniste(String noutoTunniste) {
    this.noutoTunniste = noutoTunniste;
  }

  public List<LiiteDto> getLiitteet() {
    return liitteet;
  }

  public void setLiitteet(List<LiiteDto> liitteet) {
    this.liitteet = liitteet;
  }

  public String getVirheMsg() {
    return virheMsg;
  }

  public void setVirheMsg(String virheMsg) {
    this.virheMsg = virheMsg;
  }

  public TamoTulosDto getTamoTulos() {
    return tamoTulos;
  }

  public void setTamoTulos(TamoTulosDto tamoTulos) {
    this.tamoTulos = tamoTulos;
  }

  public String getVastausSanomanNimi() {
    return vastausSanomanNimi;
  }

  public void setVastausSanomanNimi(String vastausSanomanNimi) {
    this.vastausSanomanNimi = vastausSanomanNimi;
  }

  public String getSanomaHakemisto() {
    return sanomaHakemisto;
  }

  public void setSanomaHakemisto(String sanomaHakemisto) {
    this.sanomaHakemisto = sanomaHakemisto;
  }

  public SOAPMessage getVastausSanoma() {
    return vastausSanoma;
  }

  public void setVastausSanoma(SOAPMessage vastausSanoma) {
    this.vastausSanoma = vastausSanoma;
  }

  public boolean isLiitteitaVastaanotettu() {
    return liitteitaVastaanotettu;
  }

  public void setLiitteitaVastaanotettu(boolean liitteitaVastaanotettu) {
    this.liitteitaVastaanotettu = liitteitaVastaanotettu;
  }

  public List<String> getIlmoitukset() {
    return ilmoitukset;
  }

  public void setIlmoitukset(List<String> ilmoitukset) {
    this.ilmoitukset = ilmoitukset;
  }
}
