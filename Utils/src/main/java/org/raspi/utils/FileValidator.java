/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.raspi.utils;

import java.io.File;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author vignesh
 */
public class FileValidator {
    public static boolean validateFile(File selectedFile) {
        if (selectedFile == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No Selection", "Please select a file"));
            return false;
        }
        if (!selectedFile.exists()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "File does not exist", "Please refresh content"));
            return false;
        }
        return true;
    }
}
