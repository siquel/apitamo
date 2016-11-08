package org.verohallinto.apitamoclient.dto;

/**
 * <p></p>
 * (c) 2014 Tietokarhu Oy
 */
public class ApitamoDto {

    private ApitamoInDto in;
    private ApitamoOutDto out;

    public ApitamoDto() {
    }

    public ApitamoInDto getIn() { return in; }

    public void setIn(ApitamoInDto in) {
        this.in = in;
    }

    public ApitamoOutDto getOut() {
        return out;
    }

    public void setOut(ApitamoOutDto out) {
        this.out = out;
    }
}
