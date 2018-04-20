package org.verohallinto.apitamoclient.apu;

import javax.activation.DataSource;

public class ByteArrayAttachment implements IAttachment {

    private String filename;
    private byte[] data;

    public ByteArrayAttachment(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }

    @Override
    public AttachmentType getType() {
        return AttachmentType.BYTE_ARRAY;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public DataSource getDataSource() {
        return new ByteArrayDataSource(data, "application/pdf");
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
