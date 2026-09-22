package com.nmmedit.apkprotect.util;


import com.android.zipflinger.ZipArchive;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

//import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
//import java.util.*;

/**
 * Utilities for working with apk files.
 */
public final class ApkUtils {

    private ApkUtils() {
    } // Prevent instantiation


    /**
     * Returns a file whose name matches {@code filename}, or null if no file was found.
     *
     * @param apkFile  The file containing the apk zip archive.
     * @param filename The full filename (e.g. res/raw/foo.bar).
     * @return A byte array containing the contents of the matching file, or null if not found.
     * @throws IOException Thrown if there's a matching file, but it cannot be read from the apk.
     */
    public static byte[] getFile(File apkFile, String filename) throws IOException {
        try (ZipArchive apkZip = new ZipArchive(apkFile.toPath())) {
            final InputStream input = apkZip.getInputStream(filename);
            if (input.available() == 0) {
                return null;
            }
            try (InputStream in = input) {
                return toByteArray(in);
            }
        }
    }

    private static byte[] toByteArray(InputStream in) throws IOException {
        byte[] buf = new byte[4 * 1024];
        int len;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((len = in.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return baos.toByteArray();
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4 * 1024];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }


    public static List<File> extractFiles(File apkFile, String regex, File outDir) throws IOException {
        return extractFiles(apkFile, Pattern.compile(regex), outDir);
    }

    public static List<File> extractFiles(File apkFile, Pattern regex, File outDir) throws IOException {
//        try (ZipFile apkZip = new ZipFile(apkFile)) {
//            List<File> result = new LinkedList<>();
//            final Enumeration<? extends ZipEntry> entries = apkZip.entries();
//            while (entries.hasMoreElements()) {
//                ZipEntry entry = entries.nextElement();
//                if (!entry.isDirectory() && regex.matcher(entry.getName()).matches()) {
//                    File file = new File(outDir, entry.getName());
//                    if (!file.getParentFile().exists()) {
//                        file.getParentFile().mkdirs();
//                    }
//                    try (InputStream in = apkZip.getInputStream(entry);
//                         FileOutputStream output = new FileOutputStream(file)) {
//                        copyStream(in, output);
//                        result.add(file);
//                    }
//                }
//            }
//            return result;
//        }

        try (ZipArchive apkZip = new ZipArchive(apkFile.toPath())) {
            List<File> result = new LinkedList<>();
            for (String entry : apkZip.listEntries()) {
                if (regex.matcher(entry).matches()) {
                    final InputStream input = apkZip.getInputStream(entry);

                    if (input.available() == 0) {//directory?
//                        System.out.println("dire: " + entry);
                        continue;
                    }
                    File file = new File(outDir, entry);

//                    System.out.println(file.getPath());

                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    try (InputStream in = input;
                         FileOutputStream output = new FileOutputStream(file)) {
                        copyStream(in, output);
                        result.add(file);
                    }
                }
            }
            return result;
        }
    }
}
