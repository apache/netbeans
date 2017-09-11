/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.util;

import java.io.*;
import java.util.logging.Level;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.openide.filesystems.FileUtil;

/**
 * Copy 'n paste from jira
 * XXX move to bugtracking.util?
 * @author Ondra Vrabec
 */
public class FileUtils {

    /**
     * Copies the specified sourceFile to the specified targetFile.
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        if (sourceFile == null || targetFile == null) {
            throw new NullPointerException("sourceFile and targetFile must not be null"); // NOI18N
        }

        InputStream inputStream = null;
        try {
            inputStream = createInputStream(sourceFile);            
            copyStreamToFile(inputStream, targetFile);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    public static void copyDirFiles(File sourceDir, File targetDir) {
        copyDirFiles(sourceDir, targetDir, false);
    }      
    
    public static void copyDirFiles(File sourceDir, File targetDir, boolean preserveTimestamp) {
        File[] files = sourceDir.listFiles();

        if(files==null || files.length == 0) {
            targetDir.mkdirs();
            if(preserveTimestamp) targetDir.setLastModified(sourceDir.lastModified());
            return;
        }
        if(preserveTimestamp) targetDir.setLastModified(sourceDir.lastModified());
        for (int i = 0; i < files.length; i++) {
            try {
                File target = FileUtil.normalizeFile(new File(targetDir.getAbsolutePath() + "/" + files[i].getName())); // NOI18N
                if(files[i].isDirectory()) {
                    copyDirFiles(files[i], target, preserveTimestamp);
                } else {
                    FileUtils.copyFile (files[i], target);
                    if(preserveTimestamp) target.setLastModified(files[i].lastModified());
                }
            } catch (IOException ex) {
                Bugzilla.LOG.log(Level.INFO, null, ex); // should not happen
            }
        }
    }
    
    /**
     * Copies the specified sourceFile to the specified targetFile.
     * It <b>closes</b> the input stream.
     */
    public static void copyStreamToFile(InputStream inputStream, File targetFile) throws IOException {
        if (inputStream == null || targetFile == null) {
            throw new NullPointerException("sourcStream and targetFile must not be null"); // NOI18N
        }

        // ensure existing parent directories
        File directory = targetFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory '" + directory + "'"); // NOI18N
        }
        copyStream(inputStream, createOutputStream(targetFile));
    }

    public static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        if (inputStream == null || outputStream == null) {
            throw new NullPointerException("sourcStream and targetFile must not be null"); // NOI18N
        }
        try {            
            try {
                byte[] buffer = new byte[32768];
                for (int readBytes = inputStream.read(buffer); readBytes > 0; readBytes = inputStream.read(buffer)) {
                    outputStream.write(buffer, 0, readBytes);
                }
            }
            catch (IOException ex) {
                outputStream.close();
                throw ex;
            }
        }
        finally {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
                try {
                    outputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }

    /**
     * Reads the data from the <code>file</code> and returns it as an array of bytes.
     * @param file file to be read
     * @return file contents as a byte array
     * @throws java.io.IOException
     */
    public static byte[] getFileContentsAsByteArray (File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 5);
        BufferedInputStream bis = null;
        try {
        bis = createInputStream(file);
        byte[] buffer = new byte[1024];
        for (int byteRead = bis.read(buffer); byteRead > 0; byteRead = bis.read(buffer)) {
            baos.write(buffer, 0, byteRead);
        }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
        return baos.toByteArray();
    }

    /**
     * Recursively deletes all files and directories under a given file/directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }
    
    /**
     * Do the best to rename the file.
     * @param orig regular file
     * @param dest regular file (if exists it's rewritten)
     */
    public static void renameFile(File orig, File dest) throws IOException {
        boolean destExists = dest.exists();
        if (destExists) {
            for (int i = 0; i<3; i++) {
                if (dest.delete()) {
                    destExists = false;
                    break;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        if (destExists == false) {
            for (int i = 0; i<3; i++) {
                if (orig.renameTo(dest)) {
                    return;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        // requires less permisions than renameTo
        FileUtils.copyFile(orig, dest);

        for (int i = 0; i<3; i++) {
            if (orig.delete()) {
                return;
            }
            try {
                Thread.sleep(71);
            } catch (InterruptedException e) {
            }
        }
        throw new IOException("Can not delete: " + orig.getAbsolutePath());  // NOI18N
    }

    /**
     * This utility class needs not to be instantiated anywhere.
     */
    private FileUtils() {
    }
    
    public static BufferedInputStream createInputStream(File file) throws IOException {
        int retry = 0;
        while (true) {   
            try {
                return new BufferedInputStream(new FileInputStream(file));                
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }       
    }
    
    public static BufferedOutputStream createOutputStream(File file) throws IOException {
        int retry = 0;
        while (true) {            
            try {
                return new BufferedOutputStream(new FileOutputStream(file));                
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }       
    }

    /** Creates new tmp dir in java.io.tmpdir */
    public static File createTmpFolder(String prefix) {
        String tmpDir = System.getProperty("java.io.tmpdir");  // NOI18N
        File tmpFolder = new File(tmpDir);
        File checkoutFolder = null;
        try {
            // generate unique name for tmp folder
            File tmp = File.createTempFile(prefix, "", tmpFolder);  // NOI18N
            if (tmp.delete() == false) {
                return checkoutFolder;
            }
            if (tmp.mkdirs() == false) {
                return checkoutFolder;
            }
            checkoutFolder = FileUtil.normalizeFile(tmp);
        } catch (IOException e) {
            Bugzilla.LOG.log(Level.SEVERE, null, e);
        }
        return checkoutFolder;
    }

    /**
     * Returns the first found file whose filename is the same (in a case insensitive way) as given <code>file</code>'s.
     * @param file
     * @return the first found file with the same name, but ignoring case, or <code>null</code> if no such file is found.
     */
    public static String getExistingFilenameInParent(File file) {
        String filename = null;
        if (file == null) {
            return filename;
        }
        File parent = file.getParentFile();
        if (parent == null) {
            return filename;
        }
        File[] children = parent.listFiles();
        for (File child : children) {
            if (file.getName().equalsIgnoreCase(child.getName())) {
                filename = child.getName();
                break;
            }
        }
        return filename;
    }
}
