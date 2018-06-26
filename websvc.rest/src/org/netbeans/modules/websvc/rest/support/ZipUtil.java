/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.websvc.rest.support;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Ayub Khan
 */
public class ZipUtil {
    
    private static final int READ_BUF_SIZE = 65536;
    private static final int WRITE_BUF_SIZE = 65536;
    
    private List<UnZipFilter> filters = new ArrayList<UnZipFilter>();
    
    public ZipUtil() {
    }
    
    public void addFilter(UnZipFilter filter) {
        filters.add(filter);
    }
    
    private boolean allow(ZipEntry entry) {
        for(UnZipFilter f:filters) {
            if(!f.allow(entry))
                return false;
        }
        return true;
    }
    
    public void zip(File zipFile, String[] sources, String[] paths) {
        try {

            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            for (int i = 0; i < sources.length; i++) {
                File f = new File(sources[i]);
                addEntry(f, paths[i], out);
            }
            out.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    private void addEntry(File file, String path, ZipOutputStream out) throws FileNotFoundException, IOException {
        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                File f = new File(file + File.separator + files[i]);
                addEntry(f, path + File.separator + file.getName(), out);
            }
        } else {
            byte[] data = new byte[WRITE_BUF_SIZE];
            BufferedInputStream origin = null;
            //System.out.println("Adding: " + file);
            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, READ_BUF_SIZE);
            ZipEntry entry = new ZipEntry(path + File.separator + file.getName());
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, WRITE_BUF_SIZE)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
    }

    public boolean unzip(final InputStream source,
            final FileObject targetFolderFO, boolean overwrite) throws IOException {
        boolean result = true;
        FileSystem targetFS = targetFolderFO.getFileSystem();
        File targetFolder = FileUtil.toFile(targetFolderFO);
        ZipInputStream zip = null;
        try {
            final byte [] buffer = new byte [WRITE_BUF_SIZE];
            zip = new ZipInputStream(new BufferedInputStream(source, READ_BUF_SIZE));
            final InputStream in = zip;
            ZipEntry entry;
            while((entry = zip.getNextEntry()) != null) {
                if(!allow(entry)) {
                    continue;
                }
                final File entryFile = new File(targetFolder, entry.getName());
                if(entry.isDirectory()) {
                    if(!entryFile.exists()) {
                        try {
                            FileObject fObj = FileUtil.createFolder(entryFile);
                        } catch(IOException iox) {
                            throw new RuntimeException("Failed to create folder: " +
                                    entryFile.getName() + ".  Terminating archive installation.");
                        }
                    }
                } else {
                    if(entryFile.exists() && overwrite) {
                        if (!entryFile.delete()) {
                            throw new RuntimeException("Failed to delete file: " +
                                    entryFile.getName() + ".  Terminating archive installation.");
                        }
                    }
                    File parentFile = entryFile.getParentFile();
                    if(!parentFile.exists()) {
                        try {
                            FileObject fObj = FileUtil.createFolder(parentFile);
                        } catch(IOException iox) {
                            throw new RuntimeException("Failed to create folder: " +
                                parentFile.getName() + ".  Terminating archive installation.");
                        }
                    }
                    targetFS.runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            FileOutputStream os = null;
                            try {
                                os = new FileOutputStream(entryFile);
                                int len;
                                while((len = in.read(buffer)) >= 0) {
                                    os.write(buffer, 0, len);
                                }
                            } finally {
                                if(os != null) {
                                    try {
                                        os.close();
                                    } catch(IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        } finally {
            if(zip != null) {
                try {
                    zip.close();
                } catch(IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        return result;
    }
    
    public interface UnZipFilter {
        public boolean allow(ZipEntry entry);
    }

}

