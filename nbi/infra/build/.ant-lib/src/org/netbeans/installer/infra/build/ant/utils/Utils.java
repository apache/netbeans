/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.build.ant.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;
import java.util.jar.Pack200.Unpacker;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.Project;
import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * A collection of utility methods used throughout the custom tasks classes.
 *
 * @author Kirill Sorokin
 */
public final class Utils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private final static Packer packer = Pack200.newPacker();
    private final static Unpacker unpacker = Pack200.newUnpacker();
    /**
     * The current ant project. Some of its methods will get called in process of
     * the executions of some of the utility procedures. Thus the ant tasks using the
     * class are strongly encouraged to call the {@link #setProject(Project)}
     * method prior to suing any other functionality.
     */
    private static byte[] buffer = new byte[102400];
    
    private static Project project = null;
    private static boolean useInternalPacker = false;
    private static boolean useInternalUnpacker = false;

    private static boolean tarInitialized = false;

    private static String tarExecutable = null;

    private static boolean lsInitialized = false;
    private static String lsExecutable = null;
    private static String xmx;
    private static String permSize;
    private static String maxPermSize;
    /**
     * Setter for the 'project' property.
     *
     * @param project New value for the 'project' property.
     */
    public static void setProject(final Project project) {
        Utils.project = project;
        useInternalPacker = "true".equals(project.getProperty("use.internal.packer"));
        useInternalUnpacker = "true".equals(project.getProperty("use.internal.unpacker"));
        xmx = ARG_PREFIX + XMX_ARG + project.getProperty("pack200.xmx");
        permSize = ARG_PREFIX + PERM_SIZE_ARG + project.getProperty("pack200.perm.size");
        maxPermSize = ARG_PREFIX + MAX_PERM_SIZE_ARG + project.getProperty("pack200.max.perm.size");
        String output = "use.internal.packer? " + useInternalPacker;
        if (project != null) {
            project.log("            " + output);
        } else {
            System.out.println(output);
        }
    }
    
    /**
     * Calculates the MD5 checksum for the given file.
     *
     * @param file File for which the checksum should be calculated.
     * @return The MD5 checksum of the file.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static String getMd5(final File file) throws IOException {
        return getDigest(file, MD5);
    }
    
    /**
     * Checks whether the given file is a directory.
     *
     * @param file File to check for being a directory.
     * @return <code>true</code> if the file is a directory, <code>false</code>
     *      otherwise.
     */
    public static boolean isEmpty(final File file) {
        if (file.listFiles().length == 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Checks whether the given file is a jar archive.
     *
     * @param file File to check for being a jar archive.
     * @return <code>true</code> if the file is a jar archive, <code>false</code>
     *      otherwise.
     */
    @SuppressWarnings("CallToThreadDumpStack")
    public static boolean isJarFile(final File file) {
        if (file.getName().endsWith(JAR_EXTENSION)) {
            JarFile jar = null;
            try {
                jar = new JarFile(file);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return false;
        }
    }
    
    /**
     * Checks whether the given file is a signed jar archive.
     *
     * @param file File to check for being a signed jar archive.
     * @return <code>true</code> if the file is a signed jar archive,
     *      <code>false</code> otherwise.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static boolean isSigned(final File file) throws IOException {
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

    /**
     * Packs the given jar archive using the pack200 utility.
     *
     * @param source Jar archive to pack.
     * @param target File which should become the packed jar archive.
     * @return The target file, i.e. the packed jar archive.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static boolean pack(
            final File source,
            final File target) throws IOException {
        return useInternalPacker ? packInternally(source,target) : packExternally(source,target);        
    }
    
    private static boolean packExternally(
            final File source,
            final File target) throws IOException {
        boolean result;
        
        
            String output = "Calling pack200(" + getPackerExecutable() + ") on " + source + " to " + target;
            if (project != null) {
                project.log("            " + output);
            } else {
                System.out.println(output);
            }

        Results results = run(
                getPackerExecutable(),
                xmx,
                permSize,
                maxPermSize,
                target.getAbsolutePath(),
                source.getAbsolutePath());
        
        if (results.getExitcode() == 0) {
            result = true;
        } else {
            System.out.println(results.getStdout());
            System.out.println(results.getStderr());
            System.out.println(results.getExitcode());
            result = false;
        }
        
        if (result == true) {
            target.setLastModified(source.lastModified());
        }
        
        return result;
    }
    @SuppressWarnings("CallToThreadDumpStack")
    public static boolean packInternally(final File source,
            final File target) throws IOException {
        try {
            JarFile jarFile = new JarFile(source);
            FileOutputStream outputStream = new FileOutputStream(target);

            
            String output = "Packing jarFile: " + jarFile + " to " + target;
            if (project != null) {
                project.log("            " + output);
            } else {
                System.out.println(output);
            }

            packer.pack(jarFile, outputStream);

            jarFile.close();
            outputStream.close();
            target.setLastModified(source.lastModified());
        } catch (IOException exc) {
            exc.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * Unpacks the given packed jar archive using the unpack200 utility.
     *
     * @param source Jar archive to unpack.
     * @param target File to which the unpacked archive should be saved.
     * @return The target file, i.e. the unpacked jar archive.
     * @throws java.io.IOException if an I/O errors occurs.
     */
    
     public static boolean unpack(
            final File source,
            final File target) throws IOException {
        return useInternalUnpacker ? unpackInternally(source,target) : unpackExternally(source,target);        
    }
     
    private static boolean unpackExternally(
            final File source,
            final File target) throws IOException {
        boolean result;
        
        
        Results results = run(
                getUnPackerExecutable(),
                xmx,
                permSize,
                maxPermSize,
                source.getAbsolutePath(),
                target.getAbsolutePath());
        
        if (results.getExitcode() == 0) {
            result = true;
        } else {
            System.out.println(results.getStdout());
            System.out.println(results.getStderr());
            System.out.println(results.getExitcode());
            result = false;
        }
        
        if (result == true) {
            target.setLastModified(source.lastModified());
        }
        
        return result;
    }
    @SuppressWarnings("CallToThreadDumpStack")
    public static boolean unpackInternally(final File source,
            final File target) throws IOException {
        try {
            JarOutputStream os = new JarOutputStream(new FileOutputStream(target));
            unpacker.unpack(source, os);
            os.close();
            target.setLastModified(source.lastModified());
        } catch (IOException exc) {
            exc.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * Verifies that the jar archive is correct. This method tries to access all
     * jar archive entries and to load all the classes.
     *
     * @param file Jar archive to check.
     * @return <code>true</code> is the archive is correct, <code>false</code>
     *      otherwise.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static boolean verify(final File file) throws IOException {        
        Results results = runClass(VERIFIER_CLASSNAME, file.getAbsolutePath());
        
        if (results.getExitcode() == 0) {
            return true;
        } else {
            System.out.println(results.getStdout());
            System.out.println(results.getStderr());
            System.out.println(results.getExitcode());
            return false;
        }
    }
    
    /**
     * Verifies that the if jar archive is placed next to jad, the jad file 
     * contains correct jar size
     *
     * @param file Jar archive to check.
     * @return <code>true</code> is the archive is correct, <code>false</code>
     *      otherwise.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static boolean verifyJad(final File file) throws IOException {
        final String name = file.getName();
        if(name.endsWith(".jar")) {
            File jad = new File(file.getParent(),
                    name.substring(0, name.length()-4) + ".jad");
            if(jad.exists()) {
                FileInputStream fis = new FileInputStream(jad);
                String string = read(fis).toString();  
                fis.close();
                final Matcher matcher = Pattern.compile(
                       "MIDlet-Jar-Size: ([0-9]+).*").
                        matcher(string);
                if (matcher.find()) {
                    final long size = new Long(matcher.group(1)).longValue();
                    final long realSize = file.length();
                    if(realSize!=size) {
                        System.out.println("... java descriptor file exist : " + jad);
                        System.out.println("... expected jar size : " + size);
                        System.out.println("... real jar size : " + realSize);
                        return false;
                    }
                }
                
            }
        }
        return true;
    }
    
    /**
     * Fully reads an input stream into a character sequence using the system's
     * default encoding.
     *
     * @param in Input sream to read.
     * @return The read data as a character sequence.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static CharSequence read(final InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        byte[] buf = new byte[1024];
        while (in.available() > 0) {
            int read = in.read(buf);
            
            String readString = new String(buf, 0, read);
            for(String string : readString.split(NEWLINE_REGEXP)) {
                builder.append(string).append(LINE_SEPARATOR);
            }
        }
        
        return builder;
    }
    
    /**
     * Fully transfers the given input stream to the given output stream.
     * @param in Input stream to read and transfer.
     * @param out Output stream to transfer data to.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void copy(
            final InputStream in,
            final OutputStream out) throws IOException {
        int read;
        while (in.available() > 0) {
            read = in.read(buffer);
            if (read > 0) {
                out.write(buffer, 0, read);
            }
        }
    }
    
    /**
     * Unzips a zip archive to the specified directory.
     *
     * @param file Zip archive to extract.
     * @param directory Directory which will be the target for the extraction.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void unzip(
            final File file,
            final File directory) throws IOException {
        ZipFile zip = new ZipFile(file);
        
        if (directory.exists() && directory.isFile()) {
            throw new IOException("Directory is an existing file, cannot unzip.");
        }
        
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Cannot create directory");
        }
        
        Enumeration<? extends ZipEntry> entries =
                (Enumeration<? extends ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            
            File entryFile = new File(directory, entry.getName());
            
            InputStream  in;
            OutputStream out;
            if (entry.getName().endsWith(SLASH)) {
                entryFile.mkdirs();
            } else {
                in = zip.getInputStream(entry);
                out = new FileOutputStream(entryFile);
                
                copy(in, out);
                
                in.close();
                out.close();
            }
            
            entryFile.setLastModified(entry.getTime());
        }
        
        zip.close();
    }
    
    public static void nativeUnzip(
            final File file,
            final File directory) throws IOException {
        final String[] command = new String[] {
                getUnzipExecutable(),
                file.getAbsolutePath(),
                "-d",
                directory.getAbsolutePath()};
        
        if (project != null) {
            project.log("            running command: " + Arrays.asList(command));
        }
        
        final Results results = run(command);
        
        if (results.getExitcode() != 0) {
            System.out.println(results.getStdout());
            System.out.println(results.getStderr());
            throw new IOException();
        }
    }

    private static String findTarExecutable() {
        if (!tarInitialized) {
            for (String s : new String[]{NATIVE_GNUTAR_EXECUTABLE, NATIVE_GTAR_EXECUTABLE, NATIVE_TAR_EXECUTABLE}) {
                try {                    
                    run(s);
                    tarExecutable = s;                    
                    break;                    
                } catch (IOException ex) {                    
                }
            }
        }
        tarInitialized = true;
        return tarExecutable;
    }
    
    private static String findLsExecutable() {
        if (!lsInitialized) {
            try {
                run(LS_EXECUTABLE);
                lsExecutable = LS_EXECUTABLE;
            } catch (IOException ex) {
            }
            lsInitialized = true;
        }        
        return lsExecutable;
    }
    /**
     * Untars a tar(.tar.gz|.tgz|.tar.bz2|.tar.bzip2) archive to the specified directory.
     *
     * @param file Tar archive to extract.
     * @param directory Directory which will be the target for the extraction.
     * @throws java.io.IOException if an I/O error occurs.
     */
    
    public static void nativeUntar(
            final File file,
            final File directory) throws IOException {
        boolean gzipCompression  = 
                file.getName().endsWith(".tar.gz")  || 
                file.getName().endsWith(".tgz");
        boolean bzip2Compression = 
                file.getName().endsWith(".tar.bz2") || 
                file.getName().endsWith(".tar.bzip2");
        
        File tempSource = null;
        if (gzipCompression || bzip2Compression) {            
            tempSource = File.createTempFile("temp-tar-file", ".tar", directory);
            if (project != null) {
                project.log("... extract compressed tar archive to temporary file " + tempSource);
            }
            final FileInputStream fis = new FileInputStream(file);
            InputStream is = (gzipCompression) ? new GZIPInputStream(fis) : new CBZip2InputStream(fis);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(tempSource);
                copy(is, fos);
            } catch (IOException e) {
                if(fos != null) {
                    fos.close();
                    fos = null;
                }
                tempSource.delete();
                throw e;   
            } finally {          
                if(fos != null) {
                    fos.close();
                }
                is.close();
            }
        }
        String tar = getTarExecutable();
        if(tar==null) {
            throw new IOException("... native tar executable not available");
        }
            
        
        final String[] command = new String[]{
            tar,
            "xvf",
            ((tempSource != null) ? tempSource : file).getName(),
            "-C",
            directory.getAbsolutePath().replace("\\","/")
        };

        if (project != null) {
            project.log("            running command: " + Arrays.asList(command));
        }
        try {
            final Results results = run(
                    ((tempSource != null) ? tempSource : file).getAbsoluteFile().getParentFile(),
                    command);

            if (results.getExitcode() != 0) {
                System.out.println(results.getStdout());
                System.out.println(results.getStderr());
                throw new IOException();
            }
        } finally {
            if (tempSource != null) {
                tempSource.delete();
            }
        }
    }
    /**
     * Deletes a file. If the file is a directory its contents are recursively
     * deleted.
     *
     * @param file File to be deleted.
     */
    public static void delete(final File file) {
        if (file.isDirectory()) {
            for (File child: file.listFiles()) {
                delete(child);
            }
        }
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }
    
    /**
     * Measures the size of a file. If the file is a directory, its size would be
     * equal to the sum of sizes of all its files and subdirectories.
     *
     * @param file File whose size should be measured.
     * @return The size of file.
     */
    public static long size(final File file) {
        long size = 0;
        
        if (file.isDirectory()) {
            for (File child: file.listFiles()) {
                size += size(child);
            }
        }
        
        return size + file.length();
    }
    
    /**
     * Converts the given string to its java-style ASCII equivalent, escaping
     * non ASCII characters with their \\uXXXX sequences.
     *
     * @param string String to escape.
     * @return The escaped string.
     */
    @SuppressWarnings("CallToThreadDumpStack")
    public static String toAscii(final String string) {
        Properties properties = new Properties();
        
        properties.put(UBERKEY, string);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            properties.store(outputStream, "");
        } catch (IOException e) {
            e.printStackTrace();
            return string;
        }
        
        Matcher matcher = Pattern.compile(UBERKEY_REGEXP, Pattern.MULTILINE).matcher(outputStream.toString());
        
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return string;
        }
    }
    
    /**
     * Writes the given character sequence to the given file.
     *
     * @param file File to which the character sequence should be written.
     * @param chars Character sequence which should be written to the file.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void write(
            final File file,
            final CharSequence chars) throws IOException {
        OutputStreamWriter writer =
                new OutputStreamWriter(new FileOutputStream(file), UTF8);
        writer.write(chars.toString());
        writer.close();
    }
    
    /**
     * Copies the contents of a file to the given output stream.
     *
     * @param source File whose contents should be copied.
     * @param target Output stream to which the file's contents should be
     *      transferred.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void copy(
            final File source,
            final OutputStream target) throws IOException {
        FileInputStream input = new FileInputStream(source);
        copy(input, target);
        input.close();
    }
    
    /**
     * Copies one file to another.
     *
     * @param source File to be copied.
     * @param target File which should be come the copy of the source one.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static void copy(
            final File source,
            final File target) throws IOException {
        FileOutputStream out = new FileOutputStream(target);
        copy(source, out);
        out.close();
    }
    
    /**
     * Sends an HTTP POST request to the given URL. The supplied parameters will be
     * passed as part of the request body according to
     * {@link http://www.faqs.org/rfcs/rfc1945.html}.
     *
     * @param url URL to which the POST request should be sent.
     * @param args Request parameters.
     * @return The first line of the server response, e.g. "HTTP/1.x 200 OK".
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static String post(
            final String url,
            final Map<String, Object> args) throws IOException {
        final String boundary     = "---------------" + Math.random();
        final byte[] realBoundary = ("--" + boundary).getBytes("UTF-8");
        final byte[] endBoundary  = ("--" + boundary + "--").getBytes("UTF-8");
        final byte[] crlf         = new byte[]{13, 10};
        
        final HttpURLConnection connection =
                (HttpURLConnection) new URL(url).openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        
        connection.connect();
        final OutputStream out = connection.getOutputStream();
        
        final Iterator<String> iterator = args.keySet().iterator();
        while (iterator.hasNext()) {
            String key   = iterator.next();
            Object value = args.get(key);
            
            out.write(realBoundary);
            out.write(crlf);
            
            if (value instanceof File) {
                File file = (File) value;
                
                out.write(("Content-Disposition: form-data; name=\"" +
                        key + "\"; filename=\"" +
                        file.getName() + "\"").getBytes("UTF-8"));
                out.write(crlf);
                
                out.write(("Content-Type: " +
                        "application/octet-stream").getBytes("UTF-8"));
                out.write(crlf);
                out.write(crlf);
                
                copy(file, out);
            }
            
            if (value instanceof String) {
                String string = (String) value;
                
                out.write(("Content-Disposition: form-data; " +
                        "name=\"" + key + "\"").getBytes("UTF-8"));
                out.write(crlf);
                out.write(crlf);
                
                out.write(string.getBytes("UTF-8"));
            }
            
            out.write(crlf);
        }
        
        out.write(endBoundary);
        out.close();
        
        return "" + connection.getResponseCode() + " " + connection.getResponseMessage();
    }
    
    /**
     * Signs the given jar file using the data provided in the given keystore.
     *
     * @param file Jar archive which will be signed.
     * @param keystore Path to the keystore file.
     * @param alias Keystore alias.
     * @param password Keystore password.
     * @return The results of executing the <code>jarsigner</code> utility.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public static Results sign(
            final File file,
            final String keystore,
            final String alias,
            final String password) throws IOException {
        List<String> command = new ArrayList<String>();
        
        command.add(getJarSignerExecutable());
        command.add("-keystore");
        command.add(keystore);
        command.add(file.getAbsolutePath());
        command.add(alias);
        
        Process process = new ProcessBuilder(command).start();        
        process.getOutputStream().write(password.getBytes());
        
        return handleProcess(process);
    }

    private static int getPermissionsAnalized(final File file) {
        if (file.isDirectory()) {
            return getIntegerValue(DEFAULT_PERMISSION_DIR_PROP, DEFAULT_PERMISSION_DIR);
                        
        } else {
            return isExecutableAnalized(file) ? 
                getIntegerValue(DEFAULT_EXECUTABLE_PERMISSION_FILE_PROP, DEFAULT_EXECUTABLE_PERMISSION_FILE) :
                getIntegerValue(DEFAULT_NOT_EXECUTABLE_PERMISSION_FILE_PROP, DEFAULT_NOT_EXECUTABLE_PERMISSION_FILE);
        }
    }
    private static int getIntegerValue(String prop, int defaultValue) {
        int result = defaultValue;
        String value = project.getProperty(prop);
        if (value != null && !value.equals("")) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                if (project != null) {
                    project.log("Error while parsing property " + prop
                            + " which value is [" + value + "]"
                            + " but should be integer", e, Project.MSG_WARN);
                }
            }
        }
        return result;
    }

    private static boolean isExecutableAnalized(File file) {
        int index = file.getName().lastIndexOf(".");
        String ext = (index != -1) ? file.getName().substring(index + 1) : "";
    
        for (String e : EXECUTABLE_EXTENSIONS) {
            if (ext.equals(e)) {
                return true;
            }
        }
        for (String e : NOT_EXECUTABLE_EXTENSIONS) {
            if (ext.equals(e)) {
                return false;
            }
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] bytes = new byte[64];

            int c;
            c = fis.read(bytes);
            if (c >= 4) { // length of ELF header and min length of "#!/X" string
                if (bytes[0] == '\177'
                        && bytes[1] == 'E'
                        && bytes[2] == 'L'
                        && bytes[3] == 'F') {
                    return true;
                } else if (bytes[0] == '#' && bytes[1] == '!') {
                    String s = new String(bytes, 0, c);
                    String[] array = s.split("(?:\r\n|\n|\r)");

                    if (array.length > 0) {
                        //read the first line only
                        //allow lines like "#! /bin/sh"
                        if (array[0].replaceAll("#!(\\s)+/", "#!/").startsWith("#!/")) {
                            return true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                }
            }
        }

        return false;
    }
    
    public static int getPermissions(final File file) {
        try {
            final String lsExec = getLsExecutable();
            if (lsExec == null) {
                //no ls found
                return getPermissionsAnalized(file);
            }
            final Results results = run(file.getParentFile(), lsExec, "-ld", file.getName());
            
            final String output = results.getStdout().toString().trim();
            
            if (project != null) {
                project.log("            " + output);
            } else {
                System.out.println(output);
            }
            
            int permissions = 0;

            if(output.length() < 9) { 
                return 777;
            }

            for (int i = 0; i < 9; i++) {                
                char character = output.charAt(i + 1);
                
                if (i % 3 == 0) {
                    permissions *= 10;
                }
                
                if (character == '-') {
                    continue;
                } else if ((i % 3 == 0) && (character == 'r')) {
                    permissions += 4;
                } else if ((i % 3 == 1) && (character == 'w')) {
                    permissions += 2;
                } else if ((i % 3 == 2) && (character == 'x')) {
                    permissions += 1;
                } else {
                    return 777;
                }
            }
            
            return permissions;
        } catch (IOException e) {
            return -1;
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Calculates the digital digest of the given file's contents using the
     * supplied algorithm.
     *
     * @param file File for which the digest should be calculated.
     * @param algorithm Algorithm which should be used for calculating the digest.
     * @return The calculated digest as a string.
     * @throws java.io.IOException if an I/O error occurs.
     */
    private static String getDigest(
            final File file,
            final String algorithm) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.reset();
            
            InputStream input = null;
            try {
                input = new FileInputStream(file);
                
                while (input.available() > 0) {
                    md.update(buffer, 0, input.read(buffer));
                }
            }  finally {
                if (input != null) {
                    input.close();
                }
            }
            
            byte[] bytes = md.digest();
            
            StringBuilder builder = new StringBuilder();
            
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                
                String byteHex = Integer.toHexString(b);
                if (byteHex.length() == 1) {
                    byteHex = "0" + byteHex;
                }
                if (byteHex.length() > 2) {
                    byteHex = byteHex.substring(byteHex.length() - 2);
                }
                
                builder.append(byteHex);
            }
            
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Could not find the aglorithm");
        }
    }
    
    /**
     * Runs the given class in a separate JVM.
     *
     * @param clazz Classname of the class which should be run.
     * @param args Command-line arguments for the class.
     * @return Results of executing the command (exitcode, stdout and stderr
     *      contents).
     * @throws java.io.IOException if an I/O error occurs.
     */
    private static Results runClass(
            final String clazz,
            final String... args) throws IOException {
        final String classPath = project.getProperty(CLASSPATH_VALUE_PROPERTY);
        
        final List<String> command = new ArrayList<String>();
        
        command.add(getVerificationJavaExecutable());
        command.add(CLASSPATH_ARG);
        command.add(classPath);
        command.add(clazz);
        command.addAll(Arrays.asList(args));
        
        return run(command.toArray(new String[command.size()]));
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    private static Results handleProcess(Process process) throws IOException {
        StringBuilder processStdOut = new StringBuilder();
        StringBuilder processStdErr = new StringBuilder();
        
        int errorCode = 0;        
        boolean doRun = true;
        long delay = INITIAL_DELAY;
        
        final String projectMaxTime = project.getProperty(MAX_EXECUTION_TIME_PROPERTY);
        long maxExecutionTime = (projectMaxTime!=null) ? 
            Long.parseLong(projectMaxTime) :
            MAX_EXECUTION_TIME;
        long start = System.currentTimeMillis();
        long end   = start + maxExecutionTime;
    
        while(doRun && (maxExecutionTime==0 || System.currentTimeMillis() < end)) {
            try {                
                Thread.sleep(delay);
                if(delay < MAX_DELAY) {
                    delay += DELTA_DELAY;
                }
            }  catch (InterruptedException e) {
                // do nothing - this may happen every now and then
            }
            
            try {
                errorCode = process.exitValue();
                doRun = false;
            } catch (IllegalThreadStateException e) {
                // do nothing - the process is still running
            }
            
            CharSequence string = read(process.getInputStream());
            if (string.length() > 0) {
                processStdOut.append(string);
            }
            
            string = read(process.getErrorStream());
            if (string.length() > 0) {
                processStdErr.append(string);
            }
        }
        process.destroy();
        return new Results(processStdOut, processStdErr, errorCode);
    }
    /**
     * Runs the specified command using the <code>ProcessBuilder</code> class.
     *
     * @param command Path to the executable and its arguments.
     * @return Results of executing the command (exitcode, stdout and stderr
     *      contents).
     * @throws java.io.IOException if an I/O error occurs.
     */
    private static Results run(final String... command) throws IOException {
        
        Process process = new ProcessBuilder(command).start();
        
        return handleProcess(process);
    }

    private static Results run(File directory, final String... command) throws IOException {
        
        Process process = new ProcessBuilder(command).directory(directory).start();
        
        return handleProcess(process);
    }
    
    public static String getPackerExecutable() {
        return getExecutable(PACKER_EXECUTABLE_PROPERTY, PACKER_EXECUTABLE);
    }
    public static String getUnPackerExecutable() {
        return getExecutable(UNPACKER_EXECUTABLE_PROPERTY, UNPACKER_EXECUTABLE);
    }
    public static String getLsExecutable() {
        final String value = project.getProperty(LS_EXECUTABLE_PROPERTY);
        return (value == null || value.equals("")) ? findLsExecutable() : value;
    }
    public static String getUnzipExecutable() {
        return getExecutable(UNZIP_EXECUTABLE_PROPERTY, NATIVE_UNZIP_EXECUTABLE);
    }
    public static String getTarExecutable() {
        final String value = project.getProperty(TAR_EXECUTABLE_PROPERTY);
        return (value == null || value.equals("")) ? findTarExecutable() : value;
    }
    public static String getJarSignerExecutable() {
        return getExecutable(JARSIGNER_EXECUTABLE_PROPERTY, JARSIGNER_EXECUTABLE);
    }
    public static String getVerificationJavaExecutable() {
        return getExecutable(VERIFICATION_JAVA_EXECUTABLE_PROPERTY, VERIFICATION_JAVA_EXECUTABLE);
    }
    private static String getExecutable(String propName, String defaultValue) {
        final String value = project.getProperty(propName);
        return (value == null || value.equals("")) ? defaultValue : value;
    }
    /**
     * Resolving the project property
     *
     * @param string Property to resolve
     * @return Results of resolving the property
     */
    public static String resolveProperty(String string) {        
        return resolveProperty(string,project);
    }
    
    /**
     * Resolving the project property
     *
     * @param string Property to resolve
     * @param prj Project to resolve the property for
     * @return Results of resolving the property
     */    
    public static String resolveProperty(String string, Project prj) {        
        final List <String> resolving = new ArrayList <String> ();
        return resolveProperty(string, prj, resolving);
    }
    
    private static String resolveProperty(String string, Project prj, List <String> resolving) {
        if(string==null || string.isEmpty()) {
            return string;
        }        
        StringBuilder result = new StringBuilder(string.length());
        StringBuilder buf = null;
        Stack <StringBuilder> started = new Stack <StringBuilder> ();
        boolean inside = false;
        
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '$':
                    if (i + 1 < string.length() && string.charAt(i + 1) == '{') {
                        if (inside) {
                            started.push(buf);
                        }
                        i++;
                        inside = true;
                        buf = new StringBuilder();
                    } else {
                        (inside ? buf : result).append("$");
                    }
                    break;
                case '}':
                    if (inside) {
                        final String propName = buf.toString();
                        String propValue;
                        
                        if(resolving.contains(propName)) {                            
                            propValue = null;
                        } else {
                            resolving.add(propName);
                            propValue = resolveProperty(prj.getProperty(propName),prj,resolving);
                            resolving.remove(propName);
                        }
                        final String resolved = propValue != null ? propValue : "${" + propName + "}";
                        if (!started.empty()) {
                            buf = started.pop().append(resolved);
                        } else {
                            result.append(resolved);
                            inside = false;
                        }
                    } else {
                        result.append(c);
                    }
                    break;
                default:
                    (inside ? buf : result).append(c);
            }
        }
        
        if (inside) {
            do {
                if (!started.empty()) {
                    buf = started.pop().append("${").append(buf);
                } else {
                    result.append("${").append(buf);
                    buf = null;
                }
            } while (buf != null);
        }
        return result.toString();    
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The private default constructor which prevents the class from being
     * instantiated.
     */
    private Utils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    /**
     * This class is a container for the results of executing a process. It keeps
     * the values of <code>&lt;stdout&gt;</code>, <code>&lt;stderr&gt;</code> and
     * the exitcode.
     *
     * @author Kirill Sorokin
     */
    public static class Results {
        /**
         * Value of <code>&lt;stdout&gt;</code>.
         */
        private CharSequence stdout;
        
        /**
         * Value of <code>&lt;stdout&gt;</code>.
         */
        private CharSequence stderr;
        
        /**
         * Value of the exitcode.
         */
        private int exitcode;
        
        /**
         * Creates a new instance of <code>Results</code>. The constructor simply
         * initializes the class properties with the passed-in values.
         *
         * @param stdout Contents of the process's <code>&lt;stdout&gt;</code>.
         * @param stderr Contents of the process's <code>&lt;stderr&gt;</code>.
         * @param exitcode The process's exitcode.
         */
        public Results(
                final CharSequence stdout,
                final CharSequence stderr,
                final int exitcode) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitcode = exitcode;
        }
        
        /**
         * Getter for the 'stdout' property.
         *
         * @return Value of the 'stdout' property.
         */
        public CharSequence getStdout() {
            return stdout;
        }
        
        /**
         * Getter for the 'stderr' property.
         *
         * @return Value of the 'stderr' property.
         */
        public CharSequence getStderr() {
            return stderr;
        }
        
        /**
         * Getter for the 'exitcode' property.
         *
         * @return Value of the 'exitcode' property.
         */
        public int getExitcode() {
            return exitcode;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Maximum allowed execution time for a process.
     */
    public static final int MAX_EXECUTION_TIME =
            300000; // NOMAGI
    
    /**
     * Property for setting maximum allowed execution time for a process.
     */
    public static final String MAX_EXECUTION_TIME_PROPERTY =
            "process.max.execution.time"; // NOI18N
    
    /**
     * Max delay (in milliseconds) which to wait between checking the process state.
     */
    public static final int MAX_DELAY =
            50; // NOMAGI
    
    /**
     * Delta delay (in milliseconds) which to increase the delay between checking the process state.
     */
    public static final int DELTA_DELAY =
            1; // NOMAGI
    /**
     * Initial delay (in milliseconds) between checking the process state.
     */
    public static final int INITIAL_DELAY =
            1; // NOMAGI
    
    /**
     * Prefix for JVM command-line arguments.
     */
    public static final String ARG_PREFIX =
            "-J"; // NOI18N
    
    /**
     * Maximum heap size command-line argument prefix.
     */
    public static final String XMX_ARG =
            "-Xmx"; // NOI18N
    
    /**
     * PermSize command-line argument prefix.
     */
    public static final String PERM_SIZE_ARG =
            "-XX:PermSize="; // NOI18N
    
    /**
     * MacPermSize command-line argument prefix.
     */
    public static final String MAX_PERM_SIZE_ARG =
            "-XX:MaxPermSize="; // NOI18N
    
    /**
     * Classpath command-line argument prefix.
     */
    public static final String CLASSPATH_ARG =
            "-cp"; // NOI18N
    
    /**
     * Classname of the class which should be called to verify the unpacked jar
     * file.
     */
    public static final String VERIFIER_CLASSNAME =
            "org.netbeans.installer.infra.build.ant.utils.VerifyFile"; // NOI18N
    
    /**
     * Name of the ant project's property which contains the classpath which should
     * be used for running classes.
     */
    public static final String CLASSPATH_VALUE_PROPERTY =
            "custom.tasks.cls"; // NOI18N
    
    /**
     * MD5 digital digest algorithm code name.
     */
    public static final String MD5 =
            "MD5"; // NOI18N
    
    /**
     * Extension of jar files.
     */
    public static final String JAR_EXTENSION =
            ".jar"; // NOI18N
    
    /**
     * Marker file which indicated that the jar file is signed.
     */
    public static final String SUN_MICR_RSA =
            "META-INF/SUN_MICR.RSA"; // NOI18N
    
    /**
     * Marker file which indicated that the jar file is signed.
     */
    public static final String SUN_MICR_SF =
            "META-INF/SUN_MICR.SF"; // NOI18N
    
    /**
     * A regular expression which matches any line separator.
     */
    public static final String NEWLINE_REGEXP =
            "(?:\n\r|\r\n|\n|\r)"; // NOI18N
    
    /**
     * Forward slash.
     */
    public static final String SLASH =
            "/"; // NOI18N
    
    /**
     * An artificial key name used in converting a string to ASCII.
     */
    public static final String UBERKEY =
            "uberkey"; // NOI18N
    
    /**
     * An artificial regular expression used in converting a string to ASCII.
     */
    public static final String UBERKEY_REGEXP =
            "uberkey=(.*)$"; // NOI18N
    
    /**
     * Name of the UTF-8 encoding.
     */
    public static final String UTF8 =
            "UTF-8"; // NOI18N
    
    
    /**
     * Name of the system property which contains the operating system name.
     */
    public static final String OS_NAME =
            "os.name"; // NOI18N
    
    /**
     * Name of the windows operating system.
     */
    public static final String WINDOWS =
            "Windows"; // NOI18N
    
    /**
     * Name of the windows operating system.
     */
    public static final boolean IS_WINDOWS =
            System.getProperty(OS_NAME).contains(WINDOWS); // NOI18N
    /**
     * Name of the system property which contains the current java home.
     */
    public static final String JAVA_HOME =
            "java.home"; // NOI18N
    /**
     * Value of the system property which contains the current java home.
     */
    public static final String JAVA_HOME_VALUE =
            System.getProperty(JAVA_HOME); // NOI18N
    
    /**
     * Path to the java executable on non-windows platforms. Relative to the java
     * home.
     */
    public static final String JAVA =
            "bin/java"; // NOI18N
    
    /**
     * Path to the java executable on windows platforms. Relative to the java
     * home.
     */
    public static final String JAVA_EXE =
            "bin\\java.exe"; // NOI18N
    
    private static final String VERIFICATION_JAVA_EXECUTABLE = 
            JAVA_HOME_VALUE + File.separator + ((IS_WINDOWS) ? JAVA_EXE : JAVA);
    
    private static final String PACKER_EXECUTABLE = JAVA_HOME_VALUE + 
            ((IS_WINDOWS) ? "\\bin\\pack200.exe" : "/bin/pack200");//NOI18N
    private static final String UNPACKER_EXECUTABLE = JAVA_HOME_VALUE + 
            ((IS_WINDOWS) ? "\\bin\\unpack200.exe" : "/bin/unpack200");//NOI18N
    private static final String NATIVE_UNZIP_EXECUTABLE =
            (IS_WINDOWS) ? "unzip.exe" : "unzip"; //NOI18N
    private static final String NATIVE_TAR_EXECUTABLE =
            (IS_WINDOWS) ? "tar.exe" : "tar"; //NOI18N
    private static final String NATIVE_GTAR_EXECUTABLE =
            (IS_WINDOWS) ? "gtar.exe" : "gtar"; //NOI18N
    private static final String NATIVE_GNUTAR_EXECUTABLE =
            (IS_WINDOWS) ? "gnutar.exe" : "gnutar"; //NOI18N

    public static final String PACKER_EXECUTABLE_PROPERTY = 
            "pack200.executable";
    public static final String UNPACKER_EXECUTABLE_PROPERTY = 
            "unpack200.executable";
    public static final String TAR_EXECUTABLE_PROPERTY = 
            "tar.executable";
    public static final String UNZIP_EXECUTABLE_PROPERTY = 
            "unzip.executable";
    public static final String LS_EXECUTABLE_PROPERTY = 
            "ls.executable";
    public static final String JARSIGNER_EXECUTABLE_PROPERTY = 
            "jarsigner.executable";
    public static final String VERIFICATION_JAVA_EXECUTABLE_PROPERTY =
            "verification.java.executable";

    public static final String DEFAULT_EXECUTABLE_PERMISSION_FILE_PROP =
            "default.file.executable.permissions";
    public static final String DEFAULT_NOT_EXECUTABLE_PERMISSION_FILE_PROP =
            "default.file.not.executable.permissions";
    public static final String DEFAULT_PERMISSION_DIR_PROP =
            "default.dir.permissions";
    
    private static final String JARSIGNER_EXECUTABLE = JAVA_HOME_VALUE +
        ((IS_WINDOWS) ? "\\..\\bin\\jarsigner.exe" : "/../bin/jarsigner");//NOI18N
    private static final String LS_EXECUTABLE = 
        (IS_WINDOWS) ? "ls.exe" : "ls";//NOI18N
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String [] EXECUTABLE_EXTENSIONS =
            {"so", "a", "jnilib", "dylib", "sl",
             "sh", "pl", "rb", "py", "command"};
    public static final String [] NOT_EXECUTABLE_EXTENSIONS =
            {"jar", "zip", "gz", "bzip2", "tgz", "txt", "xml", 
             "html", "htm", "pdf", "conf", "css", "java"};
    public static final int DEFAULT_EXECUTABLE_PERMISSION_FILE = 755;//rwx r-x r-x
    public static final int DEFAULT_NOT_EXECUTABLE_PERMISSION_FILE = 644;//rwx r-- r--
    public static final int DEFAULT_PERMISSION_DIR = 755; //rwx r-x r-x
}
