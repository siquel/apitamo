package org.verohallinto.apitamoclient.apu;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;

public class FileSystemAttachment implements IAttachment {

    private String path;

    public FileSystemAttachment(String path) {
        this.path = path;
    }

    @Override
    public AttachmentType getType() {
        return AttachmentType.FILESYSTEM;
    }

    @Override
    public String getFilename() {
        return new File(path).getName();
    }

    @Override
    public DataSource getDataSource() {
        return new FileDataSource(new File(path));
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
