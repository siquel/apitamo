#APITAMOCLIENT 

[![Build Status](https://travis-ci.org/siren/apitamo.svg?branch=master)](https://travis-ci.org/siren/apitamo)

##Yleiskuvaus
Tässä dokumentissa on kuvattu Ilmoitin.fi-palvelun ApiTaMo Web Service –rajapinnan käytön helpottamiseksi
toteutetun ApiTaMoClient –sovelluksen toiminta. Dokumentin tarkoitus on auttaa hyödyntäviä tahoja
toteuttamaan lähettävässä päässä vaadittavat toi- minnallisuudet.

##Org.verohallinto.apitamoclient–paketin käyttö
Lataa ApitaMoClient –paketti Ilmoitin.fi –palvelun kehittäjä -sivuilta osoitteesta 
https://www.ilmoitin.fi/kehittajat/Komponentit. Liitä paketti sovellukseesi joko suoraan läh-dekoodina tai jar-pakettina.

Tutustu myös ApiTaMo-rajapinnan tekniseen kuvaukseen, jossa on kuvattu ApiTaMo- rajapinnan toiminta.
ApiTamoClient vaatii toimiakseen Java 1.6:n tai uudemman.

##Tunnistautuminen
Käyttäjän tunnistuksessa hyödynnetään Tunnistus.fi-palvelun web service -rajapintoja.
Luo koodissasi ilmentymä org.verohallinto.apitamoclient.dto.KatsoInDto –luokasta.

`KatsoInDto katsoIn = new KatsoInDto();`
katsoIn oliota käytetään välittämään tunnistuksessa tarvittavat tiedot ApiTaMoClient:lle.

Osa tarvittavista tiedoista on ns. pysyvämpiä ja ne kannattaa tallettaa esim. tietokantaan tai properties –tiedostoon. Asetetaan niille arvot:

`katsoIn.setSASLURL(”<sasl kutsun osoite>”);`
- Pyydä osoite tamo.tk@vero.fi

`katsoIn.setWSIDPURL(”<wsidp kutsun osoite >”);`
- Pyydä osoite tamo.tk@vero.fi

`katsoIn.setAPITAMOURL(”<apitamo kutsun osoite >”);`
- Pyydä osoite tamo.tk@vero.fi

`katsoIn.setLogMessages(<true/false>);`
- True arvolla lokittaa kaikki ApiTaMoClient-paketin lähettämät ja vastaanottamat sa- nomat

`katsoIn.setMessageDirectory(”<polku haluttuun sanomahakemistoon>”);`
- Hakemisto, johon yllämainitut sanomat tallennetaan

`katsoIn.setPuraTamoTulos(<true/false>);`
- True arvolla ApiTaMo –rajapinnan palauttama tarkastustulos puretaan java.util.list –
muotoon. False arvolla käytössä on raaka SOAP sanoma.

Seuraavat tiedot ovat ns. käyttäjäkohtaisia ja niiden pysyvämpi säilytys sovelluksessa ei
ole välttämättä järkevää. Asetetaan niille arvot:

`katsoIn.setTunnistustapa(1 tai 2);`
- 1 = Katso otp ja 2 = Katso pwd

`katsoIn.setKayttajatunnus(”kayttajatunnus”);`
- Katso käyttäjätunnus

`katsoIn.setSalasana(”salasana”);`
- Katso kiinteä salasana

`katsoIn.setOtpSalasana(”OTP salasana”);`
- Jos tunnistustapa on otp niin Katso kertakäyttösalasana muuten jätä tyhjäksi

Nyt on kaikki tunnistautumiseen tarvittavat tiedot asetettu, joten hoidetaan itse tunnistautuminen.

`KatsoOutDto katsoOut = ApitamoClient.katsoSisaan(katsoIn);`
- Yllä kutsuttu metodi palauttaa `org.verohallinto.apitamoclient.dto.KatsoOutDto` –tyyppisen olion, joka 
sisältää seuraavat hyödylliset tiedot:

`tunnistusOK` – true jos käyttäjä on onnistuneesti kirjautunut palveluun, false jos kirjautuminen epäonnistui

`tfiKid` – KatsoID, jolla palveluun kirjauduttiin

`tfiPersonname – KatsoID:n haltijan nimi

`info` – Mahdollinen info-tieto esim. vaihtuvien salasanojen vähyydestä

`virhe` – virheilmoitus tunnistautumisen epäonnistumisesta

`assertion` – Aineiston lähetyksessä ApiTaMolle välitettävä assertio assertionDeadline – Assertion vanhentumisaika
Onnistuneen kirjautumisen palveluun voi siis tarkistaa helposti tyyliin: 

```
if (tunnistusOK) {
  // kaikki ok 
} else {
  // jotain vialla
}
```

##Aineiston lähetys
Luo koodissasi ilmentymä `org.verohallinto.apitamoclient.dto.ApitamoInDto –luokasta.

`ApitamoInDto apitamoIn = new ApitamoInDto();`

Asetetaan suunnaksi lähetys
`apitamoIn.setSuunta(1);`
- Suunta 1 = aineiston lähetys, 2 = paluuaineiston nouto

Välitetään Apitamolle tunnistuksesta saatu assertio
`apitamoIn.setAssertion(katsoOut.getAssertion());`

Seuraavaksi kerätään tiedot lähetettävästä aineistosta.

`apitamoIn.setData(byte array);`
- Lähetettävä ilmoitusaineisto. Huomaa, että merkistön pitää olla ISO-8859-1 muodossa.

- Jos olet lähettämässä vain liitetiedostoja anna arvoksi tyhjä bittitaulukko.
`apitamoIn.setLiiteTiedostot(Vector<String>());`

- Hakemistopolut lähetettäviin liitetiedostoihin. Huomaa, että vain pdf –muodossa ole-vat liitteet hyväksytään.
- Mikäli et ole lähettämässä liitetiedostoja, anna arvoksi tyhjä vektori
- Liitetiedostojen pitää lisäksi olla oikein nimettyjä verohallinnon ohje: [Liitetiedostojen
nimeäminen] (https://www.vero.fi/download/Sahkoisen_tuloveroilmoituksen_sahkoiset_liitteet/%7B692F17BF-AA6C-4856-9F68-31598AAD075F%7D/7737) 

`apitamoIn.setKieli("fi");`
- Kieli, jolla Apitamon tarkastustulokset halutaan (fi/sv/en). apitamoIn.setEmail(email);
- Jos olet lähettämässä aineistoa, johon liittyy paluuaineisto (esim. suorasiirrot tai ve- ronumeropyynnöt), 
voit saada sähköpostiisi ilmoituksen kun aineisto on noudettavissa
- Jos olet lähettämässä Rakentamisen tiedonantomenettelyyn liittyvää perusilmoitusta, voit saada sähköpostiisi
aineistolle annetun ilmoitustunnisteen.
- Jos haluat antaa useamman kuin yhden osoitteen, erottele ne toisistaan puolipisteellä (;).
- Mikäli et ole lähettämässä yllämainittuja aineistoja tai et halua sähköpostitiedotteita, alusta tieto
tyhjällä merkkijonolla.

Kaikki tarvittavat tiedot on nyt kerätty, joten hoidetaan aineiston lähetys.

`ApitamoOutDto apitamoOut = ApitamoClient.Laheta(apitamoIn);`
Yllä kutsuttu metodi palauttaa org.verohallinto.apitamoclient.dto.ApitamoOutDto – tyyppisen olion, 
joka sisältää seuraavat hyödylliset tiedot:

`apitamoOut.isLahetysTapahtumaOk();`
- Tieto itse lähetystapahtuman onnistumisesta (true/false). Huom! True ei tarkoita että aineistoa olisi 
välttämättä otettu vastaan!
Jos edellä mainittu kutsu palautti false arvon, voi syytä tutkia kutsumalla apitamoOut.getInfo();
Jos lähetystapahtuma onnistui, voi sisältöä purkaa auki enemmän. 

```
TamoTulosDto tamoTulos = apitamoOut.getTamoTulos(); 
tamoTulos.getTietueKpl();
```
- Tarkastettujen ilmoitusten lukumäärä

`tamoTulos.getOikeellisia();`
- Muodollisesti oikeellisten ilmoitusten lukumäärä

`tamoTulos.getVirheellisia();`
- Muodollisesti virheellisten ilmoitusten lukumäärä

Jos olet lähettänyt aineistoa, johon liittyy paluuaineisto, saat noutotunnisteen selville seuraavasti:
`apitamoOut.getNoutoTunniste();`
- Palauttaa aineistoon liittyvän noutotunnisteen. Mikäli olet antanut lähetysvaiheessa sähköpostiosoitteesi,
lähettää järjestelmä sinulle sähköpostin kun aineisto on noudet- tavissa. Halutessasi voit noutaa aineiston myös 
Ilmoitin.fi –palvelun www –liittymän kautta osoitteesta https://www.ilmoitin.fi. Kirjaudu palveluun
lähetyksessä käyttämäläsi Katso –tunnisteella niin järjestelmä näyttää etusivulla ilmoituksen mikäli aineisto on
noudettavissa.

Jos olet lähettänyt rakentamisen tiedonantomenettelyyn liittyvän perusilmoituksen, saat ilmoitustunnisteen selville 
seuraavasti:
`apitamoOut.getIlmoitusTunniste();`
- Palauttaa aineistoon liittyvän ilmoitustunnisteen. Mikäli olet antanut lähetysvaiheessa sähköpostiosoitteesi,
lähettää järjestelmä sinulle ilmoitustunnisteen sähköpostilla. Il- moitustunnisteen voi tarkastaa myös Ilmoitin.fi-
palvelun www –liittymässä osoittees- sa https://www.ilmoitin.fi. Kirjaudu palveluun lähetyksessä käyttämälläsi
Katso – tunnisteella ja valitse vasemmasta valikosta ”Arkisto”. Arkistossa pystyt selaamaan lähettämiäsi ilmoituksia ja
tarkastamaan niiden ilmoitustunnisteen.

##Ilmoitusten vastaanoton tutkiminen
ApiTaMo -rajapinta ottaa vastaan ilmoituksia vain ja ainoastaan jos kaikki ilmoitukset ovat olleet muodollisesti oikeellisia. Jos ilmoituksissa on ollut yksikin virhe, ei mitään ilmoituk- sia oteta vastaan ja samoin liitetiedostot hylätään. Toisaalta taas jos liitetiedostossa on virhe, voidaan ilmoitukset kuitenkin ottaa vastaan jos ne ovat muodollisesti oikeellisia. Li- säksi yhden liitetiedoston hylkääminen ei estä muiden liitteiden vastaanottoa.
Aineiston vastaanoton voi tutkia seuraavasti

`apitamoOut.isAineistonVastaanottoOk();`
- Palauttaa true/false riippuen onko Ilmoitukset otettu vastaan vai ei

`apitamoOut.getVastausSanomanNimi();`
- Jos palvelun alustusparametreissa on LogMessages arvoksi asetettu true, palauttaa sanoman nimen josta Apitamon tarkastustulos löytyy. Muuten palauttaa null.
Mikäli ilmoituksia ei otettu vastaan voi syytä tutkia alustusparametreista riippuen joko raa- asta SOAP-sanomasta

`apitamoOut.getVastausSanoma();`

tai listaksi puretusta sanomasta

`tamoTulos().getLomakkeet();`
Yllä oleva kutsu palauttaa java.util.list tyyppisen listan org.verohallinto.apitamoclient.dto.LomakeDto tyyppisiä
olioita. Yksittäisestä ilmoituksesta voi tutkia seuraavat asiat:

`lomake.getyTunnus();`
- Mitä y-tunnusta ilmoitus koskee

`lomake.getSelite();`
- Lomakkeen selväkielinen nimi (esim. Arvonlisäveron kausiveroilmoitus).

`lomake.isLomakkeenTilaOk();`
- Onko kyseinen lomake muodollisesti oikeellinen vai virheellinen

Mikäli ilmoitus on virheellinen, voi virherivejä tutkia kutsumalla:
`lomake.getVirheet();`

Kutsu palauttaa java.util.List tyyppisen listan `org.verohallinto.apitamoclient.dto.VirheriviDto` tyyppisiä olioita. 
Yksittäisestä virherivistä voi tutkia seuraavat asiat:

`virherivi.getTunnus();`
- Missä tunnuksessa/positiossa virhe on

`virherivi.getRivinro();`
- Millä rivillä virhe on

`virherivi.getTieto();`
- Virheellinen tieto

`virherivi.getSelitys();`
- Selväkielinen selite virheestä

Mikäli ilmoituksissa on ollut virheitä, pitää ne korjata aineiston muodostaneessa ohjelmassa ja tehdä lähetys
sitten uudelleen.

Onnistuneet lähetykset voi tarkastaa myös Ilmoitin.fi palvelun www-liittymässä osoitteessa
https://www.ilmoitin.fi. Kirjaudu palveluun lähetyksessä käytetyllä Katso -tunnisteella ja valitse vasemmasta
valikosta ”Arkisto”. Arkistossa voit selata lähetettyjä ilmoituksia ja lii- tetiedostoja ajanjakson tai ilmoituslajin
mukaan.

##Liitetiedostojen vastaanoton tutkiminen
ApiTaMo –rajapinta ei tutki liitetiedostojen asiasisältöä. Ainoat syyt minkä takia liitteet voidaan hylätä ovat
- Samaan aikaan lähetetyt ilmoitukset on myös hylätty
- Liitetiedosto on nimetty väärin (verohallinnon ohje Liitetiedostojen nimeäminen)
- Liitetiedosto on väärää tiedostotyyppiä (vain pdf hyväksytään)
- Käytetyllä Katso -tunnisteella ei ole valtuuksia lähettää kyseisiä liitetiedostoja
- Pdf –tiedosto on korruptoitunut eikä aukea

Liitetiedostojen vastaanoton voi tutkia seuraavasti:

`apitamoOut.isLiitteitaVastaanotettu();`
- True jos vähintään yksi liitetiedosto on vastaanotettu muuten false

Yksittäistä liitetiedostoa pääsee tutkimaan seuraavasti

`apitamoOut.getLiitteet();`
Kutsu palauttaa java.util.list tyyppisen listan `org.verohallinto.apitamoclient.dto.LiiteDto` tyyppisiä olioita.

Yksittäisestä liitetiedostosta voi tutkia seuraavat asiat:

`liite.getLiiteNimi();`
- Liitetiedoston nimi

`liite.isVastaanottoOk();`
- True jos liite on otettu vastaan muuten false 

`liite.getInfo();`
- Mahdollinen syy liitteen hylkäämiseen

Mikäli liitetiedosto on hylätty, pitää se korjata ja lähettää uudelleen. Liitetiedostoon liittyviä ilmoituksia ei
kuitenkaan tarvitse/pidä lähettää uudelleen jos ne on vastaanotettu ensim- mäisellä lähetyskerralla.

##Paluuaineiston nouto
Luo koodissasi ilmentymä 'org.verohallinto.apitamoclient.dto.ApitamoInDto' –luokasta.

`ApitamoInDto apitamoIn = new ApitamoInDto();`

Asetetaan suunnaksi paluuaineiston nouto

`apitamoIn.setSuunta(2);`
- Suunta 1 = aineiston lähetys, 2 = paluuaineiston nouto

`apitamoIn.setData(byte array);`

- Aineiston noutotunniste. Huomaa, että merkistön pitää olla ISO-8859-1 muodossa.

Välitetään Apitamolle tunnistuksesta saatu assertio
`apitamoIn.setAssertion(katsoOut.getAssertion());`

`apitamoIn.setKieli("fi");`
- Kieli, jolla Apitamon tulokset halutaan (fi/sv/en).

`apitamoIn.setLiiteTiedostot(new Vector<String>());`
- Anna arvoksi tyhjä vektori

Nyt on tarvittavat tiedot kerätty joten hoidetaan itse nouto:

`ApitamoOutDto apitamoOut = ApitamoClient.Laheta(apitamoIn);`
- Yllä kutsuttu metodi palauttaa `org.verohallinto.apitamoclient.dto.ApitamoOutDto` – tyyppisen olion.

##Paluuaineiston noudon onnistumisen tutkiminen
Paluuaineiston noudon onnistumisen voi varmistaa tutkimalla ensin onko itse lähetystapahtuma onnistunut

`apitamoOut.isLahetysTapahtumaOk();`
- Mikäli yllä oleva kutsu palautti false –arvon, voi syytä tutkia kutsumalla

`apitamoOut.getVirheMsg();`
- tutkimalla info –kentän sisältöä voidaan varmentaa onko aineiston nouto onnistunut. Jos info kentässä on sisältöä, ei aineistoa ole saatu noudettua.

`apitamoOut.getInfo();`
- Info kentässä voi palautua seuraavat syyt noudon epäonnistumiseen
 - Aineisto ei ole vielä noudettavissa
 - Käytetyllä Katso –tunnisteella ei ole valtuuksia noudettavaan aineistoon o Tuntematon noutotunniste

Jos nouto on onnistunut voi noudettuja paluuaineistoja tutkia alustusparametreista riippu- en joko raaka SOAP 
–sanomasta tai kutsumalla

`apitamoOut.getIlmoitukset();`
- Kutsu palauttaa java.util.List<String> tyyppisen listan noudetuista paluuaineistoista.

##org.verohallinto.apitamoclient.ui –paketti
ApiTaMoClient.ui –paketti on esimerkinomainen yksinkertainen malli varsinaisen ApiTa- MoClient –paketin käytöstä. 
Ui pakettiin on toteutettu merkkipohjainen käyttöliittymä, jonka avulla voi perehtyä varsinaisen client –luokan
toimintaan.

Käyttöliittymään on toteutettu seuraavat komennot:

Login – Kirjautuminen palveluun Katso OTP tai PWD tunnuksilla

Laheta – Aineiston lähetys ApiTaMo –rajapintaan

Hae – Valmiin aineiston nouto. Noutaaksesi aineiston, tarvitset lähetyksen yhteydessä saamasi noutotunnisteen.

Kayttaja – Tulostaa palveluun kirjautuneen käyttäjän tiedot

Help tai ? - Tulostaa ohjeen mahdollisista toiminnoista Exit tai quit – Lopettaa sovelluksen
