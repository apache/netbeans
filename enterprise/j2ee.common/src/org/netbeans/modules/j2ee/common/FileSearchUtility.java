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

package org.netbeans.modules.j2ee.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;

public final class FileSearchUtility {
    
    /** Creates a new instance of FileSearchUtility. */
    private FileSearchUtility() {
    }
        
   /** Recursively enumerate all children of this folder to some specified depth.
    *  All direct children are listed; then children of direct subfolders; and so on.
    *
    * @param root the starting directory
    * @param depth the search limit
    * @param onlyWritables only recurse into wriable directories
    * @return enumeration of type <code>FileObject</code>
    */
    public static Enumeration<FileObject> getChildrenToDepth(final FileObject root, final int depth, final boolean onlyWritables) {
        class WithChildren implements Enumerations.Processor<FileObject, FileObject> {
            private int rootDepth;
            public WithChildren(final int rootDepth) {
                this.rootDepth = rootDepth;
            }
            public FileObject process(FileObject fo, Collection<FileObject> toAdd) {
                if (!onlyWritables || (onlyWritables && fo.canWrite())) {
                    if (fo.isFolder() && (getDepth(fo) - rootDepth) < depth) {
                        toAdd.addAll(Arrays.asList(fo.getChildren()));
                    }
                }
                return fo;
            }
        }

        return Enumerations.queue(
            Enumerations.array(root.getChildren()),
            new WithChildren(getDepth(root))
        );
    }

    public static FileObject guessWebInf(FileObject dir) {        
        Enumeration<FileObject> ch = getChildrenToDepth(dir, 3, true);
        while (ch.hasMoreElements ()) {
            FileObject f = ch.nextElement ();
            if (f.isFolder()) {
                final FileObject webXmlFO = f.getFileObject("web.xml"); //NOI18N
                if (webXmlFO != null && webXmlFO.isData()) {
                    return f;
                }
            }
        }
        
        return null;
    }
    
    public static FileObject guessDocBase(FileObject dir) {
        FileObject potentialDocBase = null;
        Enumeration<FileObject> ch = getChildrenToDepth(dir, 3, true);
        while (ch.hasMoreElements ()) {
            FileObject f = ch.nextElement ();
            if (f.isData() && f.getExt().equals("jsp")) { //NOI18N
                return f.getParent();
            } else if (f.isFolder() && (f.getName().equalsIgnoreCase("web") || f.getName().equalsIgnoreCase("webroot"))) { //NOI18N
                potentialDocBase = f;
            }
        }
        
        return potentialDocBase;
    }
       
    public static FileObject guessLibrariesFolder (FileObject dir) {
        FileObject webInf = guessWebInf(dir);
        if (webInf != null) {
            FileObject lib = webInf.getFileObject("lib"); //NOI18N
            if (lib != null) {
                return lib;
            }
        }
        Enumeration<FileObject> ch = getChildrenToDepth(dir, 3, true);
        while (ch.hasMoreElements ()) {
            FileObject f = ch.nextElement ();
            if (f.getExt ().equals ("jar")) { //NOI18N
                return f.getParent ();
            }
        }
        return null;
    }
    
    public static FileObject[] guessJavaRoots(final FileObject dir) {
        List<FileObject> foundRoots = new ArrayList<FileObject>();
        if (null == dir)
            return null;
        Enumeration<FileObject> ch = FileSearchUtility.getChildrenToDepth(dir, 10, true); // .getChildren(true);
        try {
            // digging through 10 levels exhaustively is WAY TOO EXPENSIVE
            while (ch.hasMoreElements () && foundRoots.isEmpty()) {
                FileObject f = ch.nextElement ();
                if (f.getExt().equals("java") && !f.isFolder()) { //NOI18N
                    String pckg = guessPackageName(f);
                    String pkgPath = f.getParent().getPath(); 
                    if (pckg != null && pkgPath.endsWith(pckg.replace('.', '/'))) { // NOI18N
                        String rootName = pkgPath.substring(0, pkgPath.length() - pckg.length());
                        FileObject fr = f.getFileSystem().findResource(rootName);
                        if (!fr.getNameExt().equals("test") && !foundRoots.contains(fr)) { // NOI18N
                            foundRoots.add(fr);
                        }
                    }
                }
            }
        } catch (FileStateInvalidException fsie) {
            Logger.getLogger("global").log(Level.INFO, null, fsie); // NOI18N
        }
        if (foundRoots.size() == 0) {
            FileObject webInf = guessWebInf(dir);
            if (webInf != null) {
                FileObject classes = webInf.getFileObject("classes"); //NOI18N
                if (classes != null) {
                    foundRoots.add(classes);
                }
            }
        }

        if (foundRoots.size() == 0) {
            if (dir.getFileObject("src/java") != null) { // NOI18N
                foundRoots.add(dir.getFileObject("src/java")); // NOI18N
            }
        }

        if (foundRoots.size() == 0) {
            return null;
        } else {
            FileObject[] resultArr = new FileObject[foundRoots.size()];
            for (int i = 0; i < foundRoots.size(); i++) {
                resultArr[i] = foundRoots.get(i);
            }
            return resultArr;
        }
    }
    
    public static  File[] guessJavaRootsAsFiles(final FileObject dir) {
        FileObject[] rootsFOs = guessJavaRoots(dir);
        if (rootsFOs == null) {
            return new File[0];
        }
        File[] resultArr = new File[rootsFOs.length];
        for (int i = 0; i < resultArr.length; i++) {
            resultArr[i] = FileUtil.toFile(rootsFOs[i]);
        }
        return resultArr;
    }

    private static String guessPackageName(final FileObject f) {
        java.io.Reader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(f.getInputStream(), "utf-8")); // NOI18N
            StringBuffer sb = new StringBuffer();
            final char[] buffer = new char[4096];
            int len;

            for (;;) {
                len = r.read(buffer);
                if (len == -1) { break; }
                sb.append(buffer, 0, len);
            }
            int idx = sb.indexOf("package"); // NOI18N
            if (idx >= 0) {
                int idx2 = sb.indexOf(";", idx);  // NOI18N
                if (idx2 >= 0) {
                    return sb.substring(idx + "package".length(), idx2).trim();
                }
            }
        } catch (java.io.IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        } finally {
            try { 
                if (r != null) {
                    r.close();
                }
            } catch (java.io.IOException ioe) {
                // ignore this
            }
        }
        // AB: fix for #56160: assume the class is in the default package
        return ""; // NOI18N
    }
    
    private static int getDepth(final FileObject fo) {
        String path = FileUtil.toFile(fo).getAbsolutePath();
        StringTokenizer toker = new StringTokenizer(path, File.separator);
        return toker.countTokens();
    }

    public static FileObject guessConfigFilesPath(final FileObject dir, final String configFileName) {
        if (null == dir) {
            return null;
        }
        Enumeration<FileObject> ch = FileSearchUtility.getChildrenToDepth(dir, 3, true); //getChildren(true);
        try {
            while (ch.hasMoreElements()) {
                FileObject f = ch.nextElement();
                if (f.getNameExt().equals(configFileName)) {
                    String rootName = f.getParent().getPath();
                    return f.getFileSystem().findResource(rootName);
                }
            }
        } catch (FileStateInvalidException fsie) {
            Logger.getLogger("global").log(Level.INFO, null, fsie);
        }
        return null;
    }

}
