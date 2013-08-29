package org.jboss.as.domain.controller.modules;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class Module {

    private static final String LIB_PATH = "lib";
    private final File warArchive;
    private final File libFolder;
    private final String name;
    private boolean deployed;
    private boolean started;

    public Module(File extractedWar, File destinationLocation, String name) throws IOException {
        this.name = name;
        //warArchive = new File(destinationLocation, name);
        //FileUtil.copy(war, warArchive);
        libFolder = new File(extractedWar, LIB_PATH);
        if (!libFolder.isDirectory()) {
            libFolder.mkdir();
        }
        this.warArchive = new File(destinationLocation, name);

        List<File> fileList = new ArrayList<File>();
        getAllFiles(extractedWar, fileList);
        OutputStream os = null;
        try {
            os = new FileOutputStream(warArchive);
            writeZipFile(extractedWar, fileList, os );
        } finally {
            safeClose(os);
        }
    }

    public void addLibs(File[] files) throws IOException {
        for (File lib : files) {
            OutputStream out = null;
            ZipOutputStream zos = null;
            try {
                out = new FileOutputStream(warArchive);
                zos = new ZipOutputStream(out);
                addToZip("lib/" + lib.getName(), lib, zos);
            } finally {
                safeClose(zos);
                safeClose(out);
            }
        }
    }

    public void markDeployed() {
        deployed = true;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public String name() {
        return name;
    }

    public void markStarterd() {
        started = true;
    }

    public boolean isStarted() {
        return started;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(warArchive);
    }

    public File getWarArchive() {
        return warArchive;
    }

    public static void getAllFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        for (File file : files) {
            fileList.add(file);
            if (file.isDirectory()) {
                getAllFiles(file, fileList);
            }
        }
    }

    static void writeZipFile(File directoryToZip, List<File> fileList, OutputStream fos) throws IOException {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(fos);

            for (File file : fileList) {
                if (!file.isDirectory()) { // we only zip files, not directories
                    String zipFilePath = file.getCanonicalPath().substring(
                            directoryToZip.getCanonicalPath().length() + 1,
                            file.getCanonicalPath().length());
                    addToZip(zipFilePath, file, zos);
                }
            }
        } finally {
            safeClose(zos);
        }
    }

    static void addToZip(String zipFilePath, File file, ZipOutputStream zos) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);

            ZipEntry zipEntry = new ZipEntry(zipFilePath);
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

        } finally {
            try {
                zos.closeEntry();
            } catch(IOException e) {
                //TODO
                e.printStackTrace();
            }
            safeClose(fis);
        }
    }

    private static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
