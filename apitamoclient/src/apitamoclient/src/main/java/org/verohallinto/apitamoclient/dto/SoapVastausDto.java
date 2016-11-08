package org.verohallinto.apitamoclient.dto;

import javax.xml.soap.SOAPMessage;
import java.io.Serializable;

/**
 * <p>SOAP-vastaussanoman tiedot.</p>
 * (c) 2013 Tietokarhu Oy
 * <p/>
 */
public class SoapVastausDto implements Serializable {

    private static final long serialVersionUID = 933336594314267538L;
    private boolean ok; // Vastauksen status
    private SOAPMessage sanoma; // vastauksena saatu sanoma

    public SoapVastausDto() {

    }

    public boolean onOk() {

        return ok;
    }

    public void setOk(boolean ok) {

        this.ok = ok;
    }

    public SOAPMessage getSanoma() {

        return sanoma;
    }

    public void setSanoma(SOAPMessage sanoma) {

        this.sanoma = sanoma;
    }
}
