package org.raspi.media;

/**
 *
 * @author vignesh
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;

@Named
@ApplicationScoped
public class FileUploadBean {

    @Inject
    private MediaBean mediaBean;

    public void handleFileUpload(FileUploadEvent event) throws IOException {
        FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
        // Do what you want with the file 
        //try (InputStream inputStream = event.getFile().getInputstream()) {
        System.out.println("1 --------------");
        copyFile(event.getFile().getFileName(), event.getFile().getInputstream());
        System.out.println("2 --------------");
        //}
        mediaBean.loadMediaFiles();
//        RequestContext.getCurrentInstance().update("mediaForm");
//        RequestContext.getCurrentInstance().scrollTo("mediaForm");
    }

    public void copyFile(String fileName, InputStream in) throws IOException {
        try (OutputStream out = new FileOutputStream(new File(mediaBean.getParentMediaDir(), fileName))) {
            byte[] bytes = new byte[1024];
            int read;
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        }
        System.out.println("New file created!");
    }
}
