/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.installer.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.FileEntry;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.NativeUtils;
import org.netbeans.installer.utils.system.WindowsNativeUtils;

/**
 *
 * @author Kirill Sorokin
 */
public final class FileUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    
    // file/stream read/write ///////////////////////////////////////////////////////
    public static String readFile(
            final File file, String charset) throws IOException {
        FileInputStream fis   = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, charset);            
        final Reader reader = new BufferedReader(isr);
        try {
            final char[] buffer = new char[BUFFER_SIZE];
            final StringBuilder stringBuilder = new StringBuilder();
            int readLength;
            while ((readLength = reader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, readLength);
            }
            return stringBuilder.toString();
        } finally {
            try {
                reader.close();
                isr.close();
                fis.close();
            } catch(IOException ignord) {            
            }
        }
    }    
    public static String readFile(
            final File file) throws IOException {
        final Reader reader = new BufferedReader(new FileReader(file));
        try {
            final char[] buffer = new char[BUFFER_SIZE];
            final StringBuilder stringBuilder = new StringBuilder();
            int readLength;
            while ((readLength = reader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, readLength);
            }
            return stringBuilder.toString();
        } finally {
            try {
                reader.close();
            } catch(IOException ignord) {}
        }
    }
    
    public static FilesList writeFile(
            final File file,
            final CharSequence string) throws IOException {
        return writeFile(file, string, Charset.defaultCharset().name(), false);
    }
    
    public static FilesList writeFile(
            final File file,
            final CharSequence string,
            final String charset) throws IOException {
        return writeFile(file, string, charset, false);
    }
    
    public static FilesList appendFile(
            final File file,
            final CharSequence string) throws IOException {
        return writeFile(file, string, Charset.defaultCharset().name(), true);
    }
    
    public static FilesList appendFile(
            final File file,
            final CharSequence string,
            final String charset) throws IOException {
        return writeFile(file, string, charset, true);
    }
    
    public static FilesList writeFile(
            final File file,
            final CharSequence string,
            final boolean append) throws IOException {
        return writeFile(
                file,
                string,
                Charset.defaultCharset().name(),
                append);
    }
    
    public static FilesList writeFile(
            final File file,
            final CharSequence string,
            final String charset,
            final boolean append) throws IOException {
        return writeFile(
                file,
                new ByteArrayInputStream(string.toString().getBytes(charset)),
                append);
    }
    
    public static FilesList writeFile(
            final File file,
            final InputStream input) throws IOException {
        return writeFile(file, input, false);
    }
    
    public static FilesList appendFile(
            final File file,
            final InputStream input) throws IOException {
        return writeFile(file, input, true);
    }
    
    public static FilesList writeFile(
            final File file,
            final InputStream input,
            final boolean append) throws IOException {
        final FilesList list = new FilesList();
        
        if (!exists(file)) {
            if (!exists(file.getParentFile())) {
                list.add(mkdirs(file.getParentFile()));
            }
            
            file.createNewFile();
            list.add(file);
        }
        
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file, append);
            StreamUtils.transferData(input, output);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    ErrorManager.notifyDebug(ResourceUtils.getString(
                            FileUtils.class, ERROR_CLOSE_STREAM_KEY),
                            e);
                }
            }
        }
        
        return list;
    }
    
    public static String readFirstLine(
            final File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        try {
            return reader.readLine();
        } finally {
            try {
                reader.close();
            } catch (IOException ignord) {}
        }
    }
    public static List<String> readStringList(
            final File file, String charset) throws IOException {
        final List<String> list = new ArrayList<String>();
        for (String line: StringUtils.splitByLines((readFile(file,charset)))) {
            list.add(line);
        }
        return list;
    }
    public static List<String> readStringList(
            final File file) throws IOException {
        final List<String> list = new ArrayList<String>();
        for (String line: StringUtils.splitByLines((readFile(file)))) {
            list.add(line);
        }
        return list;
    }
    
    public static FilesList writeStringList(
            final File file,
            final List<String> list) throws IOException {
        return writeStringList(file, list, Charset.defaultCharset().name(), false);
    }
    
    public static FilesList writeStringList(
            final File file,
            final List<String> list,
            final String charset) throws IOException {
        return writeStringList(file, list, charset, false);
    }
    
    public static FilesList writeStringList(
            final File file,
            final List<String> list,
            final boolean append) throws IOException {
        return writeStringList(file, list, Charset.defaultCharset().name(), append);
    }
    
    public static FilesList writeStringList(
            final File file,
            final List<String> list,
            final String charset,
            final boolean append) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        for(int i=0;i<list.size();i++) {
            builder.append(list.get(i));
            if (i != list.size() - 1 ) {
                builder.append(SystemUtils.getLineSeparator());
            }
        }
        
        return writeFile(file, builder, charset, append);
    }
    
    // file metadata ////////////////////////////////////////////////////////////////
    public static Date getLastModified(
            final File file) {
        if (!exists(file)) {
            return null;
        }
        Date date = null;
        try {
            long modif = file.lastModified();
            date = new Date(modif);
        }  catch (SecurityException ex) {
            ex=null;
        }
        return date;
    }
    
    public static long getSize(
            final File file) {
        long size = 0;
        
        if(file != null && exists(file)) {            
            try {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if(files!=null) {
                        for(File f : files) {
                            size += getSize(f);
                        }
                    }
                } else {
                    size = file.length();
                }
            } catch (SecurityException e) {
                ErrorManager.notifyError(
                        ResourceUtils.getString(FileUtils.class,
                        ERROR_FILE_SECURITY_EXCEPTION_KEY, file),
                        e);
            }
        }
        
        return size;
    }
    
    public static Set<File> getRecursiveFileSet(
            final File file) throws IOException {
        Set<File> fileSet = new HashSet<>();

        if (file != null && exists(file)) {
            computeRecursiveFileSet(file,fileSet);
        }

        return fileSet;
    }
    
    static void computeRecursiveFileSet(final File file, Set<File> fileSet) throws IOException {
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    if (files.length > 0) {
                        fileSet.add(file);
                    }
                    for (File f : files) {
                        computeRecursiveFileSet(f,fileSet);
                    }
                }
            } else {
                fileSet.add(file);
            }
        } catch (SecurityException e) {
            ErrorManager.notifyError(
                    ResourceUtils.getString(FileUtils.class,
                    ERROR_FILE_SECURITY_EXCEPTION_KEY, file),
                    e);
        }
    }

    public static FilesList listFiles(
            final File file) throws IOException {
        final FilesList list = new FilesList();

        if (file != null && exists(file)) {
            try {
                list.add(file);
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            list.add(listFiles(f));
                        }
                    }
                } 
            } catch (SecurityException e) {
                ErrorManager.notifyError(
                        ResourceUtils.getString(FileUtils.class,
                        ERROR_FILE_SECURITY_EXCEPTION_KEY, file),
                        e);
            }
        }

        return list;
    }
    
    public static long getFreeSpace(
            final File file) {
        long freeSpace = 0;
        
        try {
            freeSpace = SystemUtils.getNativeUtils().getFreeSpace(file);
        } catch (NativeException e) {
            ErrorManager.notifyError(ResourceUtils.getString(
                    FileUtils.class, ERROR_CANT_GET_FREE_SPACE_KEY, file),
                    e);
        }
        
        return freeSpace;
    }
    
    public static long getCrc32(final File file) throws IOException {
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            return getCrc32(input);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignord) {}
            }
        }
    }
    public static long getCrc32(final InputStream input) throws IOException {
        CRC32 crc = new CRC32();
        final byte[] buffer = new byte[BUFFER_SIZE];
        int readLength;
        while ((readLength = input.read(buffer)) != -1) {
            crc.update(buffer, 0, readLength);
        }
        return crc.getValue();
    }
    
    public static String getMd5(
            final File file) throws IOException {
        return StringUtils.asHexString(getMd5Bytes(file));
    }
    public static String getMd5(
            final InputStream input) throws IOException {
        return StringUtils.asHexString(getMd5Bytes(input));
    }
    
    public static byte[] getMd5Bytes(
            final File file) throws IOException {
        try {
            return getDigestBytes(file, MD5_DIGEST_NAME);
        } catch (NoSuchAlgorithmException e) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    FileUtils.class, ERROR_MD5_NOT_SUPPORTED_KEY), e);
        }
        
        return null;
    }
    public static byte[] getMd5Bytes(
            final InputStream input) throws IOException {
        try {
            return getDigestBytes(input, MD5_DIGEST_NAME);
        } catch (NoSuchAlgorithmException e) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    FileUtils.class, ERROR_MD5_NOT_SUPPORTED_KEY), e);
        }
        
        return null;
    }
    public static String getSha1(
            final File file) throws IOException {
        return StringUtils.asHexString(getSha1Bytes(file));
    }
    
    public static byte[] getSha1Bytes(
            final File file) throws IOException {
        try {
            return getDigestBytes(file, SHA1_DIGEST_NAME);
        } catch (NoSuchAlgorithmException e) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    FileUtils.class, ERROR_SHA1_NOT_SUPPORTED_KEY), e);
        }
        
        return null;
    }
    
    public static byte[] getDigestBytes(
            final File file,
            final String algorithm) throws IOException, NoSuchAlgorithmException {        
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            return getDigestBytes(input, algorithm);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    LogManager.log(ex);
                }
            }
        }
    }
    
    public static byte[] getDigestBytes(
            final InputStream input,
            final String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();

        final byte[] buffer = new byte[BUFFER_SIZE];//todo: here was 10240?? discus
        int readLength;
        while ((readLength = input.read(buffer)) != -1) {
            md.update(buffer, 0, readLength);
        }

        return md.digest();
    }

    public static boolean isEmpty(
            final File file) {
        if (!exists(file)) {
            return true;
        }
        
        if (file.isDirectory()) {
            File [] list = file.listFiles();
            if (list != null) {
                for(File child : list) {
                    if (!isEmpty(child)) {
                        return false;
                    }
                }
            }
            return true;
        }  else {
            return false;
        }
    }
    
    public static boolean canRead(
            final File file) {
        return canAccessFile(file,true);
    }
    
    public static boolean canWrite(
            final File file) {
        return canAccessFile(file,false);
    }
    
    public static boolean isJarFile(
            final File file) {
        if (file.getName().endsWith(JAR_EXTENSION)) {
            JarFile jar = null;
            try {
                jar = new JarFile(file);
                return true;
            } catch (IOException e) {
                ErrorManager.notifyDebug(
                        ResourceUtils.getString(FileUtils.class,
                        ERROR_NOT_JAR_FILE_KEY, file),
                        e);
                return false;
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException e) {
                        ErrorManager.notifyDebug(
                                ResourceUtils.getString(
                                FileUtils.class, ERROR_CANT_CLOSE_JAR_KEY,
                                jar.getName()),
                                e);
                    }
                }
            }
        } else {
            return false;
        }
    }
    
    public static boolean isSigned(
            final File file) throws IOException {
        JarFile jar = new JarFile(file);

        try {
            Enumeration<JarEntry> entries = jar.entries();
            boolean signatureInfoPresent = false;
            boolean signatureFilePresent = false;
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName();
                if (entryName.startsWith("META-INF/")) {
                    if (entryName.endsWith(".RSA") || entryName.endsWith(".DSA")) {
                        signatureFilePresent = true;
                        if(signatureInfoPresent) {
                            break;
                        }
                    } else if (entryName.endsWith(".SF")) {
                        signatureInfoPresent = true;
                        if(signatureFilePresent) {
                            break;
                        }
                    }
                }
            }
            return signatureFilePresent && signatureInfoPresent;
        } finally {
            jar.close();
        }
    }
    
    public static boolean exists(
            final File file) {
        if (file.exists()) {
            return true;
        } else if (!file.isFile() && !file.isDirectory()) {
            final File parent = file.getParentFile();
            if ((parent == null) || !parent.exists()) {
                return false;
            }
            
            final File[] children = parent.listFiles();
            if (children == null) {
                return false;
            }
            
            for (File child: children) {
                if (child.equals(file)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static boolean isParent(
            final File candidate,
            final File file) {
        File parent = file.getParentFile();
        
        while ((parent != null) && !candidate.equals(parent)) {
            parent = parent.getParentFile();
        }
        
        return (parent != null) && candidate.equals(parent);
    }
    
    public static File getRoot(
            final File fileRequested,
            final List<File> roots) {
        File result = null;
        File file = fileRequested;
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            LogManager.log("... cannot get canonical file for " + file);
        }
        for (File root: roots) {
            if (isParent(root, file) || root.equals(file)) {
                if(result == null ||
                        (result.getAbsolutePath().length() <
                        root.getAbsolutePath().length())) {
                    result = root;
                }
            }
        }
        if(result == null) {
            if(SystemUtils.isWindows() && FileUtils.isUNCPath(file.getPath())) {
                return getRoot(file);
            }
        }
        return result;
    }
    
    public static long countChildren(
            final File file) {
        long count = 0;
        
        if (!file.exists()) {
            return 0;
        } else {
            count++;
        }
        
        final File[] children = file.listFiles();
        if (children != null) {
            for (File child: children) {
                count += countChildren(child);
            }
        }
        
        return count;
    }
    
    // in-file string replacement ///////////////////////////////////////////////////
    public static void modifyFile(
            final File file,
            final String token,
            final Object replacement) throws IOException {
        modifyFile(file, token, replacement, false, Charset.defaultCharset().name());
    }
    public static void modifyFile(
            final File file,
            final String token,
            final Object replacement,
            final String charset) throws IOException {
        modifyFile(file, token, replacement, false, charset);
    }

    public static void modifyFile(
            final File file,
            final String token,
            final Object replacement,
            final boolean regexp) throws IOException {
        modifyFile(file, token, replacement, regexp, Charset.defaultCharset().name());
    }
    
    public static void modifyFile(
            final File file,
            final String token,
            final Object replacement,
            final boolean regexp,
            final String charset) throws IOException {
        final Map<String, Object> replacementMap = new HashMap<String, Object>();
        
        replacementMap.put(token, replacement);
        
        modifyFile(file, replacementMap, regexp, charset);
    }
    
    public static void modifyFile(
            final File file,
            final Map<String, Object> map) throws IOException {
        modifyFile(file, map, false);
    }

    public static void modifyFile(
            final File file,
            final Map<String, Object> map,
            final boolean regexp) throws IOException {
        modifyFile(file, map,regexp, Charset.defaultCharset().name());
    }
    public static void modifyFile(
            final File file,
            final Map<String, Object> map,
            final boolean regexp,
            final String charset) throws IOException {
        if (!exists(file)) {
            return;
        }
        
        if (file.isDirectory()) {
            for (File child: file.listFiles()) {
                modifyFile(child, map, regexp, charset);
            }
        } else {
            // if the file is larger than 100 Kb - skip it
            if (file.length() > 1024*100) {
                return;
            }
            
            final String original = readFile(file, charset);

            String modified = new String(original);
            for(Map.Entry<String, Object> entry : map.entrySet()) {
                final Object object = entry.getValue();
                String token = entry.getKey();

                final String replacement;
                if (object instanceof File) {
                    replacement = ((File) object).getAbsolutePath();
                }  else {
                    replacement = object.toString();
                }
                
                if (regexp) {
                    modified = Pattern.
                            compile(token, Pattern.MULTILINE).
                            matcher(modified).
                            replaceAll(replacement);
                }  else {
                    modified = modified.replace(token, replacement);
                }
            }
            
            if (!modified.equals(original)) {
                LogManager.log("modifying file: " + file.getAbsolutePath());
                
                writeFile(file, modified, charset);
            }
        }
    }
    
    public static void modifyFiles(
            final List<File> files,
            final Map<String, Object> map,
            final boolean regexp) throws IOException {
        modifyFiles(files, map, regexp, new Progress());
    }
    
    public static void modifyFiles(
            final List<File> files,
            final Map<String, Object> map,
            final boolean regexp,
            final Progress progress) throws IOException {
        progress.setPercentage(Progress.START);
        
        for (int i = 0; i < files.size(); i++) {
            modifyFile(files.get(i), map, regexp);
            
            progress.setPercentage(Progress.COMPLETE * i / files.size());
        }
        
        progress.setPercentage(Progress.COMPLETE);
    }
    
    // file operations //////////////////////////////////////////////////////////////
    public static void deleteFile(
            final File file) throws IOException {
        deleteFile(file, false);
    }
    
    public static void deleteFile(
            final File file,
            final Progress progress) throws IOException {
        deleteFile(file, false, progress);
    }
    
    public static void deleteFile(
            final File file,
            final boolean recurse) throws IOException {
        deleteFile(file, recurse, new Progress());
    }
    
    public static void deleteFile(
            final File file,
            final boolean recurse,
            final Progress progress) throws IOException {
        final long childrenCount;
        if (recurse) {
            childrenCount = countChildren(file);
        } else {
            childrenCount = 1;
        }
        
        deleteFile(
                file,
                recurse,
                progress,
                0,
                childrenCount == 0 ? 1 : childrenCount);
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public static void deleteFiles(
            final List<File> files) throws IOException {
        deleteFiles(files, new Progress());
    }
    
    public static void deleteFiles(
            final List<File> files,
            final Progress progress) throws IOException {
        long count = 0;
        
        for (File file: files) {
            count = deleteFile(file, false, progress, count, files.size());
        }
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public static void deleteFiles(
            final File... files) throws IOException {
        deleteFiles(new Progress(), files);
    }
    
    public static void deleteFiles(
            final Progress progress,
            final File... files) throws IOException {
        deleteFiles(Arrays.asList(files), progress);
    }
    
    public static void deleteFiles(
            final FilesList files) throws IOException {
        deleteFiles(files, new Progress());
    }
    
    public static void deleteFiles(
            final FilesList files,
            final Progress progress) throws IOException {
        long count = 0;
        
        for (FileEntry entry: files) {
            count = deleteFile(
                    entry.getFile(), false, progress, count, files.getSize());
        }
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public static void deleteEmptyParents(
            final File file) throws IOException {
        if (!exists(file)) {
            final File parent = file.getParentFile();
            
            if (isEmpty(parent)) {
                deleteWithEmptyParents(parent);
            }
        }
    }
    
    public static void deleteWithEmptyParents(
            final File file) throws IOException {
        if (file == null) {
            return;
        }
        
        File probe = file;
        do {
            deleteFile(probe);
            probe = probe.getParentFile();
        } while ((probe != null) && isEmpty(probe));
    }
    
    public static void deleteOnExit(
            final File file) {
        SystemUtils.getNativeUtils().addDeleteOnExitFile(file);
    }
    
    public static File createTempFile(
            ) throws IOException {
        return createTempFile(SystemUtils.getTempDirectory());
    }
    
    public static File createTempFile(
            final File parent) throws IOException {
        return createTempFile(parent, true);
    }
    
    public static File createTempFile(
            final File parent,
            final boolean create) throws IOException {
        return createTempFile(parent, create, false);
    }
    
    public static File createTempFile(
            final File parent,
            final boolean create,
            final boolean directory) throws IOException {
        final File file = File.createTempFile("nbi-", ".tmp", parent);
        
        if (!create || directory) {
            file.delete();
        }
        if (create && directory) {
            mkdirs(file);
        }
        
        file.deleteOnExit();
        
        return file;
    }
    
    public static FilesList copyFile(
            final File source,
            final File target) throws IOException {
        return copyFile(source, target, false);
    }
    
    public static FilesList copyFile(
            final File source,
            final File target,
            final Progress progress) throws IOException {
        return copyFile(source, target, false, progress);
    }
    
    public static FilesList copyFile(
            final File source,
            final File target,
            final boolean recurse) throws IOException {
        return copyFile(source, target, recurse, new Progress());
    }
    
    public static FilesList copyFile(
            final File source,
            final File target,
            final boolean recurse,
            final Progress progress) throws IOException {
        final FilesList list = new FilesList();
        
        final long childrenCount;
        if (recurse) {
            childrenCount = countChildren(source);
        } else {
            childrenCount = 1;
        }
        
        copyFile(
                source,
                target,
                recurse,
                list,
                progress,
                0,
                childrenCount == 0 ? 1 : childrenCount,
                false);
        progress.setPercentage(Progress.COMPLETE);
        
        return list;
    }
    
    /**
     * Special method for copying nested JRE - #256122 - 8.1 RC2 IDE will not start unless run as admin
     * 
     * @param source Source JRE folder
     * @param target Targer folder (in <installation folder>\bin\jre\)
     * @param progress
     * @return
     * @throws IOException 
     */
    public static FilesList copyNestedJRE(final File source, final File target, final Progress progress) throws IOException {
        final FilesList list = new FilesList();
        
        copyFile(source, target, true, list, progress, 0, countChildren(source), true);
        progress.setPercentage(Progress.COMPLETE);
        
        return list;
    }
    
    public static FilesList moveFile(
            final File source,
            final File target) throws IOException {
        return moveFile(source, target, new Progress());
    }
    
    public static FilesList moveFile(
            final File source,
            final File target,
            final Progress progress) throws IOException {
        final FilesList list = new FilesList();
        
        progress.setDetail(StringUtils.format(
                MESSAGE_MOVING, source, target));
        if (!source.renameTo(target)) {
            final CompositeProgress composite = new CompositeProgress();
            final Progress copyProgress = new Progress();
            final Progress deleteProgress = new Progress();
            
            composite.synchronizeTo(progress);
            composite.addChild(copyProgress, 80);
            composite.addChild(deleteProgress, 20);
            
            list.add(copyFile(
                    source,
                    target,
                    true,
                    copyProgress));
            
            deleteFile(
                    source,
                    true,
                    deleteProgress);
        } else {
            list.add(target);
        }
        progress.setPercentage(Progress.COMPLETE);
        
        return list;
    }
    
    // archive operations ///////////////////////////////////////////////////////////
    public static void zip(
            final File file,
            final ZipOutputStream output,
            final File root,
            final List<File> excludes) throws IOException {
        if (excludes.contains(file)) {
            return;
        }
        
        final String entryName = file.getAbsolutePath().substring(
                root.getAbsolutePath().length() + 1);
        
        if (file.isDirectory()) {
            output.putNextEntry(new ZipEntry(entryName + SLASH));
            
            final File[] children = file.listFiles();
            if (children != null) {
                for (File child: children) {
                    zip(child, output, root, excludes);
                }
            }
        } else {
            output.putNextEntry(new ZipEntry(entryName));
            StreamUtils.transferFile(file, output);
        }
    }
    
    public static FilesList unzip(
            final File source,
            final File target) throws IOException {
        return extractAll(source, target, null, new Progress());
    }
    
    public static FilesList unzip(
            final File source,
            final File target,
            final Progress progress) throws IOException {
        return extractAll(source, target, null, progress);
    }
    
    public static FilesList unjar(
            final File source,
            final File target) throws IOException, XMLException {
        return unjar(source, target, new Progress());
    }
    
    public static FilesList unjar(
            final File source,
            final File target,
            final Progress progress) throws IOException, XMLException {
        return extractAll(source, target, METAINF_MASK, progress);
    }
    
    public static boolean zipEntryExists(
            final File file,
            final String entry) throws IOException {
        ZipFile zip = new ZipFile(file);
        
        try {
            return zip.getEntry(entry) != null;
        } finally {
            zip.close();
        }
    }
    
    public static boolean jarEntryExists(
            final File file,
            final String entry) throws IOException {
        JarFile jar = new JarFile(file);
        
        try {
            return jar.getEntry(entry) != null;
        } finally {
            jar.close();
        }
    }
    
    public static File extractJarEntry(
            final String entry,
            final File source) throws IOException {
        return extractJarEntry(entry, source, FileUtils.createTempFile());
    }
    
    public static File extractJarEntry(
            final String entry,
            final File source,
            final File target) throws IOException {
        JarFile jar = new JarFile(source);
        FileOutputStream out = new FileOutputStream(target);
        
        try {
            StreamUtils.transferData(jar.getInputStream(jar.getEntry(entry)), out);
            
            return target;
        } finally {
            jar.close();
            out.close();
        }
    }
    
    public static String getJarAttribute(
            final File file,
            final String name) throws IOException {
        JarFile jar = new JarFile(file);
        
        try {
            return jar.getManifest().getMainAttributes().getValue(name);
        } finally {
            try {
                jar.close();
            } catch (IOException e) {
                ErrorManager.notifyDebug(ResourceUtils.getString(
                        FileUtils.class, ERROR_CANT_CLOSE_JAR_KEY, jar.getName()), e);
            }
        }
    }
    
    // miscellaneous ////////////////////////////////////////////////////////////////
    public static FilesList mkdirs(
            final File file) throws IOException {
        FilesList list = new FilesList();

        if (exists(file)) {
            if (file.isFile()) {
                throw new IOException(ResourceUtils.getString(FileUtils.class,
                        ERROR_CANT_CREATE_DIR_EXIST_FILE_KEY, file));
            }
        } else {
            final File parent = file.getParentFile();
            if (parent != null && !exists(parent)) {
                list.add(mkdirs(parent));
            }

            if (file.mkdir()) {
                list.add(file);
            } else {
                throw new IOException(ResourceUtils.getString(FileUtils.class,
                        ERROR_CANT_CREATE_DIR_KEY, file));
            }
        }

        return list;
    }
    
    public static String getRelativePath(
            final File source,
            final File target) {
        String path;
        
        if (source.equals(target)) { // simplest - source equals target
            path = source.isDirectory() ? CURRENT : target.getName();
        } else if (isParent(source, target)) { // simple - source is target's parent
            final String sourcePath =
                    source.getAbsolutePath().replace(BACKSLASH, SLASH);
            final String targetPath =
                    target.getAbsolutePath().replace(BACKSLASH, SLASH);
            
            if (sourcePath.endsWith(SLASH)) {
                path = targetPath.substring(sourcePath.length());
            } else {
                path = targetPath.substring(sourcePath.length() + 1);
            }
        } else if (isParent(target, source)) { // simple - target is source's parent
            path = source.isDirectory() ? PARENT : CURRENT;
            
            File parent = source.getParentFile();
            while (!parent.equals(target)) {
                path  += SLASH + PARENT;
                parent = parent.getParentFile();
            }
        } else { // tricky - the files are unrelated
            // first we need to find a common parent for the files
            File parent = source.getParentFile();
            while ((parent != null) && !isParent(parent, target)) {
                parent = parent.getParentFile();
            }
            
            // if there is no common parent, we cannot deduct a relative path
            if (parent == null) {
                return null;
            }
            
            path =  getRelativePath(source, parent) +
                    SLASH +
                    getRelativePath(parent, target);
        }
        
        // some final beautification
        if (path.startsWith(CURRENT + SLASH)) {
            if (path.length() > 2) {
                path = path.substring(2);
            } else {
                path = path.substring(0, 1);
            }
        }
        path = path.replace(SLASH + CURRENT + SLASH, SLASH);
        
        return path;
    }
    
    public static File getNormalizedPathFile(File file) {
        if (file != null && !file.getPath().isEmpty()) {
            try {
                Path path = FileSystems.getDefault().getPath(file.getPath());
                
                if (path != null) {
                    return path.normalize().toFile();
                }
            } catch(InvalidPathException ex) {
                LogManager.log("Trying to normalize invalid path", ex);
            }                            
        }
        
        return file;
    }
    
    public static boolean isUNCPath(String path) {
        return SystemUtils.getNativeUtils().isUNCPath(path);
    }
    
    public static File eliminateRelativity(
            final String path) {
        String corrected = path;
        
        if(SystemUtils.isWindows() && isUNCPath(corrected)) {
            // don`t correct UNC paths that starts with \\<servername>
            corrected = corrected.substring(0,2) +
                    corrected.substring(2).replace(BACKSLASH, SLASH);
        } else {
            corrected = corrected.replace(BACKSLASH, SLASH);
        }
        
        while (corrected.indexOf(SLASH + SLASH) != -1) {
            corrected = corrected.replace(SLASH + SLASH, SLASH);
        }
        
        while (corrected.indexOf(SLASH + CURRENT + SLASH) != -1) {
            corrected = corrected.replace(SLASH + CURRENT + SLASH, SLASH);
        }
        
        final Pattern pattern = Pattern.compile("(\\/([^\\/]+)\\/\\.\\.\\/)");
        
        Matcher matcher = pattern.matcher(corrected);
        while (matcher.find()) {
            if (matcher.group(2).equals(PARENT)) {
                continue;
            } else {
                corrected = corrected.replace(matcher.group(), SLASH);
                matcher = pattern.matcher(corrected);
            }
        }
        
        if (corrected.endsWith(SLASH + CURRENT)) {
            corrected = corrected.substring(
                    0,
                    corrected.length() - SLASH.length() - CURRENT.length());
        }
        
        if (corrected.endsWith(SLASH + PARENT)) {
            int index = corrected.lastIndexOf(
                    SLASH,
                    corrected.length() - SLASH.length() - PARENT.length() - 1);
            if (index != -1) {
                corrected = corrected.substring(0, index);
            }
        }
        
        return new File(corrected);
    }
    
    public static File getRoot(
            final File file) {
        return SystemUtils.getNativeUtils().getRoot(file);
    }
    
    public static File findFile(
            final File directory,
            final String filename) {
        if (directory.getName().equals(filename)) {
            return directory;
        }
        
        final File[] children = directory.listFiles();
        if (children != null) {
            for (File child: children) {
                final File match = findFile(child, filename);
                
                if (match != null) {
                    return match;
                }
            }
        }
        
        return null;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static long deleteFile(
            final File file,
            final boolean recurse,
            final Progress progress,
            final long start,
            final long total) throws IOException {
        long count = start;
        
        if (SystemUtils.isDeletingAllowed(file)) {
            final boolean isDir = file.isDirectory();
            final String type = (isDir) ? "directory" : "file";
            if (isDir && recurse) {
                final File[] children = file.listFiles();
                if (children != null) {
                    for (File child: children) {
                        count = deleteFile(child, true, progress, count, total);
                    }
                }
            }
            
            LogManager.log("deleting " + type + ": " + file);
            
            progress.setDetail(StringUtils.format(
                    isDir ? MESSAGE_DELETE_DIR : MESSAGE_DELETE_FILE, file));
            
            if (!exists(file)) {
                LogManager.log("    ... " + type + " does not exist");
                SystemUtils.getNativeUtils().removeDeleteOnExitFile(file);
            } else {
                if (!file.delete()) {
                    deleteOnExit(file);
                }
            }
            
            count++;
            progress.setPercentage(Progress.COMPLETE * count / total);
        }
        
        return count;
    }
    
    private static long copyFile(
            final File source,
            final File target,
            final boolean recurse,
            final FilesList list,
            final Progress progress,
            final long start,
            final long total,
            final boolean copyNesteJre) throws IOException {
        long count = start;
        
        if (!exists(source)) {
            LogManager.log("    ... " + source + " does not exist");
            return count;
        }
        
        if (source.isFile()) {
            LogManager.log("copying file: " + source + " to: " + target);
            progress.setDetail(StringUtils.format(MESSAGE_COPY_FILE, source,target));
            
            if (!source.canRead()) {
                throw new IOException(ResourceUtils.getString(
                        FileUtils.class,  ERROR_SOURCE_NOT_READABLE_KEY, source));
            }
            
            if (exists(target) && !target.isFile()) {
                throw new IOException(ResourceUtils.getString(FileUtils.class,
                        ERROR_DEST_NOT_FILE_KEY, target));
            }
            
            File parent = target.getParentFile();
            if (!exists(parent)) {
                list.add(mkdirs(parent));
            }
            
            if (!exists(target) && !target.createNewFile()) {
                throw new IOException(ResourceUtils.getString(FileUtils.class,
                        ERROR_DEST_CREATION_KEY, target));
            }
            
            if (!target.canWrite()) {
                throw new IOException(ResourceUtils.getString(FileUtils.class,
                        ERROR_DEST_NOT_WRITABLE_KEY, target));
            }            
            
            if (copyNesteJre && SystemUtils.isWindows()) {
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new FileInputStream(source);
                    os = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                } finally {                    
                    is.close();
                    os.close();
                }
            } else {
                Files.copy(source.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS);
            }
            list.add(target);
        } else {
            LogManager.log("copying directory: " + source + " to: " + target + (recurse ? " with recursion" : ""));
            progress.setDetail(StringUtils.format(MESSAGE_COPY_DIRECTORY, source, target));

            list.add(mkdirs(target));
            if (recurse) {
                for (File file : source.listFiles()) {
                    count = copyFile(file, new File(target, file.getName()), recurse, list, progress, count, total, copyNesteJre);
                }
            }
        }
        
        count++;
        progress.setPercentage(Progress.COMPLETE * count / total);
        
        return count;
    }
    
    private static boolean canAccessDirectoryReal(
            final File file,
            final boolean isReadNotWrite) {
        if (isReadNotWrite) {
            boolean result = (file.listFiles()!=null);
            //            LogManager.indent();
            //            LogManager.log(ErrorLevel.DEBUG, "READ: Real Level Access DIR: " + ((result) ? "TRUE" : "FALSE"));
            //            LogManager.unindent();
            return result;
        } else {
            try {
                FileUtils.createTempFile(file).delete();
                //                LogManager.indent();
                //                LogManager.log(ErrorLevel.DEBUG, "WRITE: Real Level Access DIR: TRUE");
                //                LogManager.unindent();
                return true;
            } catch (IOException e) {
                //                LogManager.indent();
                //                LogManager.log(ErrorLevel.DEBUG, "WRITE: Real Level Access DIR: FALSE");
                //                LogManager.unindent();
                return false;
            }
        }
    }
    
    private static boolean canAccessFileReal(
            final File file,
            final boolean isReadNotWrite) {
        Closeable stream = null;
        LogManager.indent();
        try {
            stream = (isReadNotWrite) ? new FileInputStream(file) :
                new FileOutputStream(file) ;
            //LogManager.log(ErrorLevel.DEBUG,
            //        ((isReadNotWrite) ? "READ:" : "WRITE:") + "Real Level Access File: TRUE");
            return true;
        } catch (IOException ex) {
            //LogManager.log(ErrorLevel.DEBUG,
            //        ((isReadNotWrite) ? "READ:" : "WRITE:") + "Real Level Access File: FALSE");
            return false;
        } finally {
            LogManager.unindent();
            if (stream!=null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    LogManager.log(ex);
                }
            }
        }
    }
    
    private static boolean canAccessFile(
            final File fileToCheck,
            final boolean isReadNotWrite) {
        File file = fileToCheck;
        
        // if file doesn`t exist then get it existing parent
        if (!exists(file)) {
            File parent = file;
            do {
                parent = parent.getParentFile();
            } while ((parent != null) && !exists(parent));
            
            if ((parent == null) || !parent.isDirectory()) {
                return false;
            } else {
                file = parent;
            }
        }
        
        //first of all check java implementation
        boolean javaAccessCheck = (isReadNotWrite) ? file.canRead() : file.canWrite();
        
        // don`t treat read-only attributes for directories as "can`t write" on windows
        if (SystemUtils.isWindows() && !isReadNotWrite && file.isDirectory()) {
            javaAccessCheck = true;
        }
        
        if (javaAccessCheck) {
            boolean result = true;
            boolean needCheckDirectory = true;
            
            try {
                // Native checking
                result = SystemUtils.getNativeUtils().checkFileAccess(file, isReadNotWrite);
                
                if (!isReadNotWrite) {
                    // we don`t want to check for writing if OS says smth specific
                    needCheckDirectory = false;
                }
            } catch (NativeException ex) {
                // most probably there is smth wrong with OS
                LogManager.log(ex);
            }
            
            if (!result) { // some limitations by OS
                return false;
            }
            
            if (file.isFile()) {
                return canAccessFileReal(file, isReadNotWrite);
            } else if (file.isDirectory() && (needCheckDirectory)) {
                return canAccessDirectoryReal(file, isReadNotWrite);
            } else { // file is directory, access==read || (access==write & OSCheck==true)
                return true;
            }
        } else {
            return false;
        }
    }
    
    private static FilesList extractAll(
            final File file,
            final File target,
            final String excludes,
            final Progress progress) throws IOException {
        final FilesList list = new FilesList();
        
        // first some basic validation of the destination directory
        if (exists(target) && target.isFile()) {
            throw new IOException(ResourceUtils.getString(FileUtils.class,
                    ERROR_UNJAR_TODIR_KEY, target));
        } else if (!exists(target)) {
            list.add(mkdirs(target));
        }
        
        final ZipFile zip = new ZipFile(file);
        
        try {
            FilesList extracted = null;
            boolean extractedWithList = false;
            
            // first we try to extract with the given list
            if (zipEntryExists(file, FILES_LIST_ENTRY)) {
                try {
                    final File initialList =
                            extractJarEntry(FILES_LIST_ENTRY, file);
                    final FilesList toExtract =
                            new FilesList().loadXml(initialList, target);
                    
                    deleteFile(initialList);
                    extracted = extractByList(zip, target, toExtract, progress);
                    toExtract.clear();
                    
                    extractedWithList = true;
                } catch (XMLException e) {
                    ErrorManager.notifyDebug(
                            ResourceUtils.getString(FileUtils.class,
                            ERROR_LOAD_XML_FILE_LIST_KEY),
                            e);
                }
            }
            
            if (!extractedWithList) {
                extracted = extractNormal(zip, target, excludes, progress);
            }
            
            list.add(extracted);
            extracted.clear();
        } finally {
            zip.close();
        }
        
        return list;
    }
    
    private static FilesList extractByList(
            final ZipFile zip,
            final File target,
            final FilesList list,
            final Progress progress) throws IOException {
        final FilesList newList = new FilesList();
        final String targetPath = target.getAbsolutePath();
        
        final int total = list.getSize();
        
        int extracted = 0;
        for (FileEntry listEntry: list) {
            // check for cancel status
            if (progress.isCanceled()) return newList;
            
            final String listEntryName = listEntry.getName();
            final File listEntryFile = listEntry.getFile();
            
            final String zipEntryName =
                    listEntryName.substring(targetPath.length() + 1);
            
            // increase the extracted files count and update the progress percentage
            extracted++;
            progress.setPercentage(Progress.COMPLETE * extracted / total);
            
            // set the progress detail and add a log entry
            progress.setDetail(StringUtils.format(MESSAGE_EXTRACTING, listEntryFile));
            LogManager.log("extracting " + listEntryFile);
            
            if (listEntry.isDirectory()) {
                newList.add(mkdirs(listEntryFile));
            } else {
                final ZipEntry zipEntry = zip.getEntry(zipEntryName);
                
                newList.add(mkdirs(listEntryFile.getParentFile()));
                
                // actual data transfer
                InputStream  in  = null;
                OutputStream out = null;
                try {
                    in  = zip.getInputStream(zipEntry);
                    out = new FileOutputStream(listEntryFile);
                    
                    StreamUtils.transferData(in, out);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
                
                listEntryFile.setLastModified(listEntry.getLastModified());
                
                SystemUtils.setPermissions(
                        listEntry.getFile(),
                        listEntry.getPermissions(),
                        NativeUtils.FA_MODE_SET);
            }
            
            newList.add(listEntry);
        }
        
        return newList;
    }
    
    private static FilesList extractNormal(
            final ZipFile zip,
            final File target,
            final String excludes,
            final Progress progress) throws IOException {
        final FilesList list = new FilesList();
        
        Enumeration<? extends ZipEntry> entries;
        
        int total     = 0;
        int extracted = 0;
        
        // then we count the entries, to correctly display progress
        entries = (Enumeration<? extends ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            total++;
            entries.nextElement();
        }
        
        // and only after that we actually extract them
        entries = (Enumeration<? extends ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            // check for cancel status
            if (progress.isCanceled()) return list;
            
            final ZipEntry entry = entries.nextElement();
            
            // increase the extracted files count and update the progress percentage
            extracted++;
            progress.setPercentage(Progress.COMPLETE * extracted / total);
            
            // if the entry name matches the excludes pattern, we skip it
            if ((excludes != null) && entry.getName().matches(excludes)) {
                continue;
            }
            
            // create the target file for this entry
            final File file = new File(target, entry.getName()).getAbsoluteFile();
            
            // set the progress detail and add a log entry
            progress.setDetail(StringUtils.format(MESSAGE_EXTRACTING, file));
            LogManager.log("extracting " + file);
            
            if (entry.getName().endsWith(SLASH)) {
                // some validation (this is a directory entry and thus an existing
                // file will definitely break things)
                if (exists(file) && !file.isDirectory()) {
                    throw new IOException(ResourceUtils.getString(
                            FileUtils.class, ERROR_OUTPUT_DIR_ENTRY_KEY, file));
                }
                
                // if the directory does not exist, it will be created and added to
                // the extracted files list (if it exists already, it will not
                // appear in the list)
                if (!exists(file)) {
                    list.add(mkdirs(file));
                }
            } else {
                // some validation of the file's parent directory
                final File parent = file.getParentFile();
                if (!exists(parent)) {
                    list.add(mkdirs(parent));
                }
                
                // some validation of the file itself
                if (exists(file) && !file.isFile()) {
                    throw new IOException(ResourceUtils.getString(
                            FileUtils.class, ERROR_OUTPUT_FILE_ENTRY_KEY, file));
                }
                
                // actual data transfer
                InputStream  in  = null;
                OutputStream out = null;
                try {
                    in  = zip.getInputStream(entry);
                    out = new FileOutputStream(file);
                    
                    StreamUtils.transferData(in, out);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
                
                // as opposed to directories, we always add files to the list, as
                // even if they exist, they will be overwritten
                list.add(file);
            }
            
            // correct the entry's modification time, so it corresponds to the real
            // time of the file in archive
            file.setLastModified(entry.getTime());
        }
        
        return list;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private FileUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int BUFFER_SIZE =
            65536; // NOMAGI
    
    public static final String SLASH =
            "/"; // NOI18N
    public static final String BACKSLASH =
            "\\"; // NOI18N
    public static final String METAINF_MASK =
            "META-INF.*"; // NOI18N
    
    public static final String JAR_EXTENSION =
            ".jar"; // NOI18N
    public static final String PROPERTIES_EXTENSION =
            ".properties"; // NOI18N
    public static final String PACK_GZ_SUFFIX =
            ".pack.gz"; // NOI18N
    public static final String SUN_MICR_RSA =
            "META-INF/SUN_MICR.RSA"; // NOI18N
    public static final String SUN_MICR_SF =
            "META-INF/SUN_MICR.SF"; // NOI18N
    public static final String FILES_LIST_ENTRY =
            "META-INF/files.list";//NOI18N
    public static final String CURRENT =
            "."; // NOI18N
    public static final String PARENT =
            ".."; // NOI18N
    public static final String SHA1_DIGEST_NAME =
            "SHA1";//NOI18N
    public static final String MD5_DIGEST_NAME =
            "MD5";//NOI18N
    public static final String INFO_PLIST_STUB =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE plist SYSTEM \"file://localhost/System/Library/DTDs/PropertyList.dtd\">\n" +
            "<plist version=\"0.9\">\n" +
            "  <dict>\n" +
            "    \n" +
            "    <key>CFBundleName</key>\n" +
            "    <string>{0}</string>\n" +
            "    \n" +
            "    <key>CFBundleVersion</key>\n" +
            "    <string>{1}</string>\n" +
            "    \n" +
            "    <key>CFBundleExecutable</key>\n" +
            "    <string>{3}</string>\n" +
            "    \n" +
            "    <key>CFBundlePackageType</key>\n" +
            "    <string>APPL</string>\n" +
            "    \n" +
            "    <key>CFBundleShortVersionString</key>\n" +
            "    <string>{2}</string>\n" +
            "    \n" +
            "    <key>CFBundleSignature</key>\n" +
            "    <string>????</string>\n" +
            "    \n" +
            "    <key>CFBundleInfoDictionaryVersion</key>\n" +
            "    <string>6.0</string>\n" +
            "    \n" +
            "    <key>CFBundleIconFile</key>\n" +
            "    <string>{4}</string>\n" +
            "  </dict>\n" +
            "</plist>\n";
    
    
    
    public static final String ERROR_OUTPUT_FILE_ENTRY_KEY =
            "FU.error.output.file.entry";// NOI18N
    
    public static final String ERROR_OUTPUT_DIR_ENTRY_KEY =
            "FU.error.output.dir.entry";// NOI18N
    public static final String MESSAGE_MOVING =
            ResourceUtils.getString(FileUtils.class,
            "FU.message.moving");//NOI18N
    public static final String MESSAGE_EXTRACTING =
            ResourceUtils.getString(FileUtils.class,
            "FU.message.extracting");//NOI18N
    public static final String ERROR_LOAD_XML_FILE_LIST_KEY =
            "FU.error.load.xml.file.list";//NOI18N
    public static final String ERROR_UNJAR_TODIR_KEY =
            "FU.error.unjar.todir";//NOI18N
    public static final String MESSAGE_COPY_DIRECTORY =
            ResourceUtils.getString(FileUtils.class,
            "FU.message.copy.dir");//NOI18N
    public static final String MESSAGE_COPY_FILE =
            ResourceUtils.getString(FileUtils.class,
            "FU.message.copy.file");//NOI18N
    public static final String ERROR_CLOSE_STREAM_KEY =
            "FU.error.close.stream";//NOI18N
    public static final String ERROR_SOURCE_NOT_READABLE_KEY =
            "FU.error.source.not.readable";//NOI18N
    public static final String ERROR_DEST_NOT_FILE_KEY =
            "FU.error.dest.not.file";//NOI18N
    public static final String ERROR_DEST_CREATION_KEY =
            "FU.error.dest.creation";//NOI18N
    public static final String ERROR_DEST_NOT_WRITABLE_KEY =
            "FU.error.dest.not.writable";//NOI18N
    public static final String ERROR_CANT_GET_FREE_SPACE_KEY =
            "FU.error.freespace";//NOI18N
    public static final String ERROR_CANT_CLOSE_JAR_KEY =
            "FU.error.cannot.close.jar";//NOI18N
    public static final String ERROR_CANT_CREATE_DIR_EXIST_FILE_KEY=
            "FU.error.cannot.create.dir.exist.file";//NOI18N
    public static final String ERROR_CANT_CREATE_DIR_KEY=
            "FU.error.cannot.create.dir";//NOI18N
    public static final String ERROR_NOT_JAR_FILE_KEY=
            "FU.error.not.jar.file";//NOI18N
    public static final String ERROR_SHA1_NOT_SUPPORTED_KEY =
            "FU.error.sha1.not.supported";//NOI18N
    public static final String ERROR_MD5_NOT_SUPPORTED_KEY =
            "FU.error.md5.not.supported";//NOI18N
    public static final String ERROR_FILE_SECURITY_EXCEPTION_KEY =
            "FU.error.file.security.exception";//NOI18N
    public static final String MESSAGE_DELETE_FILE =
            ResourceUtils.getString(FileUtils.class,
            "FU.message.delete.file");//NOI18N
    public static final String MESSAGE_DELETE_DIR =
            ResourceUtils.getString(FileUtils.class,
            "FU.message.delete.dir");//NOI18N
}
