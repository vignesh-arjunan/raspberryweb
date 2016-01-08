package org.raspi.utils;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.*;

public class ZipFSPUser {

    public static File fileZipper(File toZip) throws Throwable {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        File zipFile = new File(toZip.getAbsolutePath() + ".zip");
        zipFile.createNewFile();
        System.out.println("jar:" + zipFile.toURI());
        URI uri = URI.create("jar:" + zipFile.toURI());
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            Path externalTxtFile = toZip.toPath();
            Path pathInZipfile = zipfs.getPath("/" + toZip.getName());
            // copy a file into the zip file
            Files.copy(externalTxtFile, pathInZipfile,
                    StandardCopyOption.REPLACE_EXISTING);
        }
        return zipFile;
    }
}
