package org.verohallinto.apitamoclient.ui;

import org.verohallinto.apitamoclient.ApitamoClient;
import org.verohallinto.apitamoclient.dto.*;
import org.verohallinto.apitamoclient.ui.apu.Apuri;
import org.verohallinto.apitamoclient.ui.yleiset.Vakiot;
import org.verohallinto.apitamoclient.ui.yleiset.props;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * <p>
 * Esimerkkitoteutus ApitamoClient -luokan käytöstä
 * </p>
 * (c) 2014 Tietokarhu Oy Date: 25.6.2014 - 13:34 $Rev$
 */
public class ApitamoClientUI {

  public static void main(String[] args) {
    BufferedReader stdin;
    String cmd = "";
    KatsoInDto katsoIn = new KatsoInDto();
    KatsoOutDto katsoOut = new KatsoOutDto();
    ApitamoOutDto apitamoOut;

    // luetaan propertyt
    props.alusta();

    // Alustetaan lähetystiedot
    katsoIn.setSASLURL(props.anna(Vakiot.KATSO_SASL_URL));
    katsoIn.setWSIDPURL(props.anna(Vakiot.KATSO_WSIDP_URL));
    katsoIn.setAPITAMOURL(props.anna(Vakiot.APITAMO_URL));
    katsoIn.setLogMessages(props.lokitaSanomat());
    katsoIn.setMessageDirectory(props.annaSanomaHakemisto());
    katsoIn.setPuraTamoTulos(props.puraTamoTulos());

    do {
      try {
        System.out.print("ApiTaMoCL> ");
        stdin = new BufferedReader(new InputStreamReader(System.in));
        cmd = stdin.readLine();

        cmd = cmd.trim();

        if (cmd != null && cmd.length() > 0) {
          if (cmd.equalsIgnoreCase("login")) {
            katsoOut = tunnistaudu(katsoIn);

            if (katsoOut.onTunnistusOk()) {
              // eipä tarvii täällä tehdä mitään
              System.out.println("Tunnistautuminen onnistui.");
              System.out.println("Tunnistautunut henkilo: " + katsoOut.getTfiPersonname());
              System.out.println("Katso ID: " + katsoOut.getTfiKid());
              System.out.println("Katso versio: " + katsoOut.getTfiVersion());
              System.out.println("Sanomat kirjoitettu hakemistoon: " + props.annaSanomaHakemisto());
            } else {
              System.out.println("Tunnistautuminen epaonnistui.");
              System.out.println("Syy: " + katsoOut.getVirhe());
            }
          } else if (cmd.equalsIgnoreCase("Laheta")) {
            if (!katsoOut.onTunnistusOk()) {
              System.out.println("Vaaditaan tunnistautuminen, anna tunnistustiedot.");
              katsoOut = tunnistaudu(katsoIn);

              if (katsoOut != null) {
                System.out.println("Sanomat kirjoitettu hakemistoon: " + props.annaSanomaHakemisto());
              }
            } else {
              // käyttäjä on tunnistautunut mutta tutkitaan että assertio on
              // voimassa
              SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
              int valid = katsoOut.getAssertionDeadline().compareTo(df.parse(Apuri.annaAika()));

              if (valid <= 0) {
                System.out.println("Assertio on vanhentunut. Anna tunnistustiedot uudelleen.");
                katsoOut = tunnistaudu(katsoIn);

                if (katsoOut != null) {
                  System.out.println("Sanomat kirjoitettu hakemistoon: " + props.annaSanomaHakemisto());
                }
              }
            }

            if (katsoOut.onTunnistusOk()) {
              apitamoOut = laheta(katsoOut);
              if (apitamoOut != null) {
                if (apitamoOut.isLahetysTapahtumaOk()) {
                  System.out.println("Lahetystapahtuma onnistui.");
                  System.out.println("Aineistoja tarkastettiin: " + apitamoOut.getTamoTulos().getTietueKpl() + "kpl.");
                  System.out.println("Näistä oikeellisia oli: " + apitamoOut.getTamoTulos().getOikeellisia() + "kpl.");
                  System.out
                      .println("Näistä virheellisiä oli: " + apitamoOut.getTamoTulos().getVirheellisia() + "kpl.");
                  System.out.println("");

                  if (apitamoOut.isAineistonVastaanottoOk()) {
                    System.out.println("Lähetetyt ilmoitukset on siirretty Verohallinnon käsittelyyn.");
                    System.out.println("Tarkemman erittelyn näet tiedostosta: ." + apitamoOut.getVastausSanomanNimi());
                    System.out.println("");
                  } else {
                    System.out.println("Aineistoa ei vastaanotettu. Syy: " + apitamoOut.getInfo());

                    if (props.puraTamoTulos() && (apitamoOut.getTamoTulos().getLomakkeet() != null
                        && apitamoOut.getTamoTulos().getLomakkeet().size() > 0)) {

                      for (LomakeDto lomake : apitamoOut.getTamoTulos().getLomakkeet()) {
                        String tila = lomake.isLomakkeenTilaOk() ? "Ok" : "Virhe";
                        System.out.println(
                            "Asiakas: " + lomake.getyTunnus() + ", Lomake: " + lomake.getSelite() + ", Tila: " + tila);
                        System.out.println("");
                      }
                    }

                    System.out.println("Tarkemman erittelyn näet tiedostosta: ." + apitamoOut.getVastausSanomanNimi());
                    System.out.println("");
                  }

                  if (apitamoOut.isLiitteitaVastaanotettu()) {
                    System.out.println("Yksi tai useampi liitetiedosto on siirretty Verohallinnon käsittelyyn.");
                    for (LiiteDto liite : apitamoOut.getLiitteet()) {
                      System.out.println("Liitetiedoston nimi: " + liite.getLiiteNimi());
                      System.out.println("Vastaanotto: " + liite.isVastaanottoOk());
                      System.out.println("Info: " + liite.getInfo());
                      System.out.println("");
                      System.out
                          .println("Tarkemman erittelyn näet tiedostosta: ." + apitamoOut.getVastausSanomanNimi());
                    }
                  } else {
                    if (apitamoOut.getLiitteet().size() > 0) {
                      System.out.println("Liitetiedostoja ei vastaanotettu.");
                      for (LiiteDto liite : apitamoOut.getLiitteet()) {
                        System.out.println("Liitetiedoston nimi: " + liite.getLiiteNimi());
                        System.out.println("Vastaanotto: " + liite.isVastaanottoOk());
                        System.out.println("Info: " + liite.getInfo());
                        System.out.println("");
                      }
                      System.out
                          .println("Tarkemman erittelyn näet tiedostosta: ." + apitamoOut.getVastausSanomanNimi());
                    }
                  }
                } else {
                  System.out.println("Lähetystapahtuma epäonnistui.");
                  System.out.println("Syy: " + apitamoOut.getInfo());
                }
              }
            }
          } else if (cmd.equalsIgnoreCase("Hae")) {
            if (!katsoOut.onTunnistusOk()) {
              System.out.println("Vaaditaan tunnistautuminen, anna tunnistustiedot.");
              katsoOut = tunnistaudu(katsoIn);

              if (katsoOut != null) {
                System.out.println("Sanomat kirjoitettu hakemistoon: " + props.annaSanomaHakemisto());
              }
            } else {
              // käyttäjä on tunnistautunut mutta tutkitaan että assertio on
              // voimassa
              SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
              int valid = katsoOut.getAssertionDeadline().compareTo(df.parse(Apuri.annaAika()));

              if (valid <= 0) {
                System.out.println("Assertio on vanhentunut. Anna tunnistustiedot uudelleen.");
                katsoOut = tunnistaudu(katsoIn);

                if (katsoOut != null) {
                  System.out.println("Sanomat kirjoitettu hakemistoon: " + props.annaSanomaHakemisto());
                }
              }
            }

            if (katsoOut.onTunnistusOk()) {
              apitamoOut = Hae(katsoOut);

              if (apitamoOut != null) {
                if (apitamoOut.isLahetysTapahtumaOk()) {
                  if (apitamoOut.getInfo() != null && apitamoOut.getInfo().length() > 0) {
                    System.out.println(apitamoOut.getInfo());
                  } else {
                    System.out.println("Nouto onnistui.");

                    if (props.puraTamoTulos()) {
                      System.out.println("Ilmoituksia noudettiin " + apitamoOut.getIlmoitukset().size() + " kpl.");
                      System.out.println("");
                      for (String ilmoitus : apitamoOut.getIlmoitukset()) {
                        System.out.println(ilmoitus);
                      }
                      System.out.println("");
                    }
                  }
                  System.out.println("ApiTaMon vastauksen loydat tiedostosta: " + apitamoOut.getVastausSanomanNimi());
                } else {
                  System.out.println("Nouto epäonnistui.");
                  System.out.println("Syy: " + apitamoOut.getVirheMsg());
                }
              }
            }
          } else if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")) {
            // lopetetaan ohjelma
            System.out.println("Poistutaan...");
          } else if (cmd.equalsIgnoreCase("help") || cmd.equalsIgnoreCase("?")) {
            // tulostetaan komennot
            System.out.println();
            System.out.println("Komennot:");
            System.out.println();
            System.out
                .println("	Login         - Ohjelma kysyy tunnistustiedot ja tunnistautuu WSIDP:lle");
            System.out
                .println("	Laheta        - Lahetetaan ilmoitus- ja/tai liitetiedostoja ApiTaMolle");
            System.out
                .println("	Hae           - Vastausaineiston haku (esim. veronumerot ja suorasiirrot)");
            System.out.println("	Kayttaja      - Nayttaa viimeiseksi kirjautuneen kayttajan tiedot");
            System.out.println("	Help tai ?    - Tulostaa tiedot kaytettavista komennoista");
            System.out.println("	Exit tai quit - Lopettaa ohjelman");
            System.out.println();
          } else if (cmd.equalsIgnoreCase("kayttaja")) {
            // tulostetaan kirjautuneen käyttäjän tiedot
            System.out.println();
            if (katsoOut.onTunnistusOk()) {
              System.out.println("Viimeksi kirjautunut kayttaja:");
              System.out.println("KATSO username: " + katsoOut.getTfiKid());
              System.out.println("Nimi: " + katsoOut.getTfiPersonname());
            } else {
              System.out.println("Ei kirjautunutta kayttajaa");
            }
            System.out.println();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } while (!cmd.equalsIgnoreCase("exit") && !cmd.equalsIgnoreCase("quit"));

    // poistutaan lopuksi
    System.exit(0);
  }

  private static StringBuffer lueLahdeTiedot(String tied) {
    InputStream in;
    Reader reader;
    BufferedReader breader = null;
    String rivi;
    StringBuffer sbuf = new StringBuffer();

    try {
      in = new FileInputStream(tied);
      reader = new InputStreamReader(in, "ISO-8859-1");
      breader = new BufferedReader(reader);

      // tyhjätään sbuf varmuuden vuoksi
      sbuf.setLength(0);

      rivi = "";
      while ((rivi = breader.readLine()) != null) {
        sbuf.append(rivi);
        sbuf.append("\r\n");
      }

      return sbuf;
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return null;
    } finally {
      try {
        if (breader != null)
          breader.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  private static KatsoOutDto tunnistaudu(KatsoInDto in) {
    String ktunnus;
    String salasana;
    String otp;
    String loginType;
    BufferedReader stdin;

    try {
      System.out.println("Valitse tunnistautumistapa: ");
      System.out.println("");
      System.out.println("	1 - KATSO OTP");
      System.out.println("	2 - KATSO password");
      System.out.println("");
      System.out.print("Anna valinta> ");
      stdin = new BufferedReader(new InputStreamReader(System.in));
      loginType = stdin.readLine();
      loginType = loginType.trim();

      if (loginType == null || loginType.length() == 0 || (!loginType.equals("1") && !loginType.equals("2"))) {
        return null;
      } else if (loginType.equalsIgnoreCase("quit") || loginType.equalsIgnoreCase("exit")) {
        System.exit(0);
      }
      in.setTunnistustapa(Integer.parseInt(loginType));

      System.out.print("KATSO kayttajatunnus> ");

      stdin = new BufferedReader(new InputStreamReader(System.in));
      ktunnus = stdin.readLine();
      ktunnus = ktunnus.trim();

      if (ktunnus == null || ktunnus.length() == 0) {
        return null;
      } else if (ktunnus.equalsIgnoreCase("quit") || ktunnus.equalsIgnoreCase("exit")) {
        System.exit(0);
      }
      in.setKayttajatunnus(ktunnus);

      System.out.print("KATSO salasana> ");

      stdin = new BufferedReader(new InputStreamReader(System.in));
      salasana = stdin.readLine();
      salasana = salasana.trim();
      System.out.print("\b");

      if (salasana == null || salasana.length() == 0) {
        return null;
      } else if (salasana.equalsIgnoreCase("quit") || salasana.equalsIgnoreCase("exit")) {
        System.exit(0);
      }
      in.setSalasana(salasana);

      if (loginType.equals("1")) {
        System.out.print("KATSO otp> ");
        stdin = new BufferedReader(new InputStreamReader(System.in));
        otp = stdin.readLine();
        otp = otp.trim();
        System.out.print("\b");

        if (otp == null || otp.length() == 0) {
          return null;
        } else if (otp.equalsIgnoreCase("quit") || otp.equalsIgnoreCase("exit")) {
          System.exit(0);
        }
      } else {
        otp = "";
      }
      in.setOtpSalasana(otp);

      // kun kaikki on kyselty, kirjaudutaan sisään
      return ApitamoClient.katsoSisaan(in);

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }

  private static int keraaLiitteet(Vector<String> liitteet) {
    String apu;
    int liiteLkm;
    BufferedReader stdin;

    try {
      // tyhjennetään aluksi
      liitteet.removeAllElements();
      liiteLkm = 0;

      System.out.print("Kuinka monta liitetta lahetetaan?: ");
      stdin = new BufferedReader(new InputStreamReader(System.in));
      apu = stdin.readLine();
      apu = apu.trim();

      liiteLkm = Integer.parseInt(apu);

      // luetaan liitteet
      for (int i = 0; i < liiteLkm; i++) {
        System.out.print("Anna " + (i + 1) + " liitetiedoston nimi polun kanssa: ");
        stdin = new BufferedReader(new InputStreamReader(System.in));
        liitteet.addElement(stdin.readLine());
      }

      // tarkastetaan että liitteet ovat oikeita tiedostoja
      for (int i = 0; i < liitteet.size(); i++) {
        File file = new File(liitteet.elementAt(i));
        if (file.exists() && file.canRead()) {
          // kaikki ok
        } else {
          System.out.println();
          System.out.println("Liitetiedostoa '" + liitteet.elementAt(i)
              + "' ei loydy tai lukeminen on estetty, liitetta ei laheteta.");

          // poistetaan se vektorista
          liitteet.removeElementAt(i);
        }
      }

      return liitteet.size();
    } catch (NumberFormatException nfe) {
      System.out.println("Liitteiden lukumaara pitaa antaa numeroilla");
      return 0;
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
      return 0;
    }
  }

  private static ApitamoOutDto laheta(KatsoOutDto katsoOut) {

    ApitamoInDto apitamoIn = new ApitamoInDto();
    String _hakemisto;
    String hakemisto;
    String tmp;
    StringBuffer data = new StringBuffer();

    boolean onFile;
    Vector<String> liitteet = new Vector<String>();
    int liiteLkm;
    BufferedReader stdin;
    String erotin;
    String kieli;
    String email;

    erotin = System.getProperty("file.separator");

    // suunta on lähetys
    apitamoIn.setSuunta(Vakiot.SUUNTA_LAHETYS);

    // laitetaan mukaan Tunnistuksesta saatu assertio
    apitamoIn.setAssertion(katsoOut.getAssertion());

    try {
      System.out.print("Anna lahetettava ilmoitustiedosto polun kanssa: ");
      stdin = new BufferedReader(new InputStreamReader(System.in));
      _hakemisto = stdin.readLine();

      _hakemisto = _hakemisto.trim();

      onFile = _hakemisto.length() > 0;

      if (onFile) {
        // tarkastettava tiedosto
        if (_hakemisto.contains(erotin) || _hakemisto.contains("/")) {
          hakemisto = _hakemisto;
        } else {
          hakemisto = System.getProperty("user.dir") + _hakemisto;
        }

        // tutkitaan että annettu tiedosto on olemassa ja sitä voi lukea...
        File tiedosto = new File(hakemisto);
        if (tiedosto.exists() && tiedosto.canRead()) {
          // luetaan tiedosto
          data.setLength(0);
          data = lueLahdeTiedot(hakemisto);

          if (data.length() == 0) {
            System.out.println();
            System.out.println("Tiedosto '" + tiedosto + "' on tyhja!");
            System.out.println("Lahetys eponnistui.");
            return null;
          } else {
            apitamoIn.setData(data.toString().getBytes("ISO-8859-1"));
          }
        } else {
          System.out.println();
          System.out.println("Tiedostoa '" + tiedosto + "' ei loydy!");
          System.out.println("Lahetys epaonnistui.");
          return null;
        }
      }

      // kysellään mahdolliset liitteet
      System.out.print("Lahetetaanko liitetiedostoja (k/e)?: ");
      stdin = new BufferedReader(new InputStreamReader(System.in));
      tmp = stdin.readLine();
      tmp = tmp.trim();

      // tyhjennetään nämä varmuuden vuoksi
      liiteLkm = 0;
      liitteet.removeAllElements();

      if (tmp.equalsIgnoreCase("k")) {
        liiteLkm = keraaLiitteet(liitteet);
        System.out.println(liiteLkm + " liitetiedostoa luettiin onnistuneesti.");
        apitamoIn.setLiiteTiedostot(liitteet);
      } else {
        apitamoIn.setLiiteTiedostot(new Vector<String>(0));
      }

      do {
        System.out.println("Valitse kieli, jolla vastaukset halutaan: ");
        System.out.println("");
        System.out.println("	1 - Suomi");
        System.out.println("	2 - Ruotsi");
        System.out.println("	3 - Englanti");
        System.out.println("	4 - Lopeta");
        System.out.println("");
        System.out.print("Anna valinta> ");
        stdin = new BufferedReader(new InputStreamReader(System.in));
        kieli = stdin.readLine().trim();

        if (!kieli.equals("1") && !kieli.equals("2") && !kieli.equals("3") && !kieli.equals("4")) {
          System.out.println("Virheellinen valinta!");
          System.out.println("");
        }
      } while (!kieli.equals("1") && !kieli.equals("2") && !kieli.equals("3") && !kieli.equals("4"));

      if (kieli.equals("1")) {
        apitamoIn.setKieli("fi");
      } else if (kieli.equals("2")) {
        apitamoIn.setKieli("sv");
      } else if (kieli.equals("3")) {
        apitamoIn.setKieli("en");
      } else if (kieli.equals("4")) {
        return null;
      }

      // kysell��n email
      System.out.println(
          "Jos olet lahettamassa palautettavaa aineistoa voit saada sahkopostiisi ilmoituksen aineiston valmistuttua.");
      System.out.println(
          "Jos olet lahettamassa Rakentamisen tiedonantomenettelyyn liittyvaa perusilmoitusta voit saada sahkopostiisi aineiston ilmoitustunnisteen.");
      System.out.println(
          "Mikäli et halua ilmoitusta sahkopostiisi tai olet lahettamassa muunlaisia ilmoituksia, paina suoraan <enter>");
      System.out.println("");

      System.out.print("Anna sahkopostiosoite> ");
      stdin = new BufferedReader(new InputStreamReader(System.in));
      email = stdin.readLine().trim();

      if (email.length() > 0) {
        apitamoIn.setEmail(email);
      } else {
        apitamoIn.setEmail("");
      }

      if (onFile || liiteLkm > 0) {
        return ApitamoClient.Laheta(apitamoIn);
      } else {
        System.out.println("Ei lahetettavaa tietoa.");
        return null;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static ApitamoOutDto Hae(KatsoOutDto katsoOutDto) {
    ApitamoInDto apitamoIn = new ApitamoInDto();
    StringBuffer data = new StringBuffer();
    BufferedReader stdin;

    // suunta on nouto
    apitamoIn.setSuunta(Vakiot.SUUNTA_NOUTO);

    try {
      // valmiin aineiston nouto
      System.out.print("Anna aineiston noutotunniste: ");
      stdin = new BufferedReader(new InputStreamReader(System.in));
      data.setLength(0);
      data.append(stdin.readLine().trim());

      if (data.length() > 0) {
        apitamoIn.setData(data.toString().getBytes("ISO-8859-1"));
        apitamoIn.setKieli("fi");
        apitamoIn.setLiiteTiedostot(new Vector<String>());
        apitamoIn.setAssertion(katsoOutDto.getAssertion());

        return ApitamoClient.Laheta(apitamoIn);

      } else {
        System.out.println("Ei haettavaa noutotunnistetta.");
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
