package org.jboss.as.domain.controller.modules;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.vfs.VirtualFile;

class FileUtil {

    private FileUtil() {}

    static void copy(final VirtualFile src, final File dest) throws IOException {
        for (VirtualFile vf : src.getChildren()) {
            if (vf.isFile()) {
                if (!dest.exists()) {
                    if (!dest.mkdirs()) {
                        throw new IOException("Cannot create dir " + dest);
                    }
                }
                File destFile = new File(dest, vf.getName());
                copyFile(vf.getPhysicalFile(), destFile);
            } else {
                copy(vf,new File(dest, vf.getName()));
            }
        }
    }

    static void copyFile(File src, File dest) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(src).getChannel();
            destination = new FileOutputStream(dest).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    static void extractArchive(File zipFile, File destinationFolder) throws IOException {
        int BUFFER = 2048;

        ZipFile zip = new ZipFile(zipFile);

        Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(destinationFolder, currentEntry);
            //destFile = new File(newPath, destFile.getName());
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte[] data = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                // TODO use channel instead of buffer
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }

            if (currentEntry.endsWith(".war")) {
                // found a war file, try to open
                extractArchive(destFile, destinationFolder);
            }
        }
    }

    static void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                recursiveDelete(c);
            }
        }
        f.delete();
    }
}
