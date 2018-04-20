package org.verohallinto.apitamoclient.apu;

import javax.activation.DataSource;

public interface IAttachment {

    enum AttachmentType {
        FILESYSTEM,
        BYTE_ARRAY
    }

    AttachmentType getType();

    String getFilename();

    DataSource getDataSource();

    boolean isValid();
}
