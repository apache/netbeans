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

package org.netbeans.lib.profiler.classfile;

import org.netbeans.lib.profiler.utils.MiscUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Class path, that can be set containing both directories and .jar files, and then used to read a .class (.java)
 * file with a specified fully qualified name.
 *
 * @author Misha Dmitirev
 * @author Tomas Hurka
 */
public class ClassPath {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private abstract static class PathEntry {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        protected HashSet entries;
        protected int hits; // This is done to avoid indexing of the JAR files too early and all at once
        protected int threshHits; // This is done to avoid indexing of the JAR files too early and all at once

        protected final Random r = new Random(System.currentTimeMillis());
        
        //~ Methods --------------------------------------------------------------------------------------------------------------

        abstract String getLocationForClassFile(String fileName);
    }

    private static class Dir extends PathEntry {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private File dir;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        Dir(File dirF) {
            dir = dirF;
            threshHits = 100 + r.nextInt(40);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        String getLocationForClassFile(String fileName) {
            if (entries != null) {
                if (entries.contains(fileName)) {
                    return dir.getAbsolutePath();
                } else {
                    return null;
                }
            } else {
                if (++hits >= threshHits) {
                    entries = new HashSet();
                    MiscUtils.getAllClassesInDir(dir.getAbsolutePath(), "", false, entries); // NOI18N

                    return getLocationForClassFile(fileName);
                } else {
                    File file = new File(dir, fileName);

                    //System.err.println("*** Trying file " + file.getAbsolutePath() + " in PathEntry = " + dir.getAbsolutePath());
                    if (file.exists()) {
                        return dir.getAbsolutePath();
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    private class Zip extends PathEntry {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private String zipFilePath;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        Zip(String path) {
            zipFilePath = path;
            threshHits = 50 + r.nextInt(20);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        String getLocationForClassFile(String fileName) {
            if (entries != null) {
                if (entries.contains(fileName)) {
                    return zipFilePath;
                } else {
                    return null;
                }
            } else {
                if (++hits >= threshHits) {
                    entries = new HashSet();
                    MiscUtils.getAllClassesInJar(zipFilePath, false, entries);
                    return getLocationForClassFile(fileName);
                } else {
                    ZipFile zip;
                    try {
                        zip = getZipFileForName(zipFilePath);
                    } catch (IOException ex) {
                        System.err.println("Warning: CLASSPATH component " + zipFilePath + ": " + ex); // NOI18N
                        return null;
                    }
                    ZipEntry entry = zip.getEntry(fileName);

                    if (entry != null) {
                        return zipFilePath;
                    } else {
                        return null;
                    }
                }
            }
        }
    }
    
    private static class JarLRUCache extends LinkedHashMap {
        private static final int MAX_CAPACITY = 100;
        
        private JarLRUCache() {  
            super(10, 0.75f, true); 
        }
        
        protected boolean removeEldestEntry(Map.Entry eldest) {
            if (size()>MAX_CAPACITY) {
                try {
                    ((ZipFile)eldest.getValue()).close();
                } catch (IOException ex) {
                    // ignore
                }
                return true;
            }
            return false;
        }

    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JarLRUCache zipFileNameToFile;
    private PathEntry[] paths;
    private boolean isCP; // True for a class path, false for a source path

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ClassPath(String classPath, boolean isCP) {
        this.isCP = isCP;
        List vec = new ArrayList();
        zipFileNameToFile = new JarLRUCache();

        for (StringTokenizer tok = new StringTokenizer(classPath, File.pathSeparator); tok.hasMoreTokens();) {
            String path = tok.nextToken();

            if (!path.equals("")) { // NOI18N
                File file = new File(path);

                if (file.exists()) {
                    if (file.isDirectory()) {
                        vec.add(new Dir(file));
                    } else {
                        vec.add(new Zip(file.getPath()));
                    }
                }
            }
        }

        paths = (PathEntry[])vec.toArray(new PathEntry[0]);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Searches for the class on this class path, reads it if found, and returns the DynamicClassInfo for it.
     * If class is not found, returns null. Exceptions are thrown if class file is found but something goes wrong when reading it.
     */
    public DynamicClassInfo getClassInfoForClass(String className, int classLoaderId)
                                          throws IOException, ClassFormatError {
        String slashedClassName = className.replace('.', '/'); // NOI18N
                                                               //System.err.println("*** Requested " + slashedClassName);

        String dirOrJar = getLocationForClass(slashedClassName);

        //if (dirOrJar == null) System.err.println("*** Unsuccessful for " + slashedClassName);
        if (dirOrJar == null) {
            return null;
        }

        return new DynamicClassInfo(slashedClassName, classLoaderId, dirOrJar);
    }

    /** Requires "slashed" class name. Returns the directory or .jar name where this class is located, or null if not found. */
    public String getLocationForClass(String slashedClassName) {
        String fileName = slashedClassName + (isCP ? ".class" : ".java"); // NOI18N

        for (int i = 0; i < paths.length; i++) {
            String location = paths[i].getLocationForClassFile(fileName);

            if (location != null) {
                return location;
            }
        }

        return null;
    }

    /** This is used to avoid repetitive creation of ZipFiles in the code that reads files from JARs given just the name of the latter */
    public ZipFile getZipFileForName(String zipFileName) throws IOException {
        ZipFile zip = (ZipFile) zipFileNameToFile.get(zipFileName);
        if (zip == null) {
            zip = new ZipFile(zipFileName);
            zipFileNameToFile.put(zipFileName,zip);
        }
        return zip;
    }

    public void close() {
        // close all ZipFiles in ClassPath, the files on disk would otherwise be locked
        // this is a bugfix for http://profiler.netbeans.org/issues/show_bug.cgi?id=61849
        for (Iterator it = zipFileNameToFile.values().iterator(); it.hasNext();) {
            try {
                ((ZipFile) it.next()).close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    //------------------------------------------ Debugging -----------------------------------------
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < paths.length; i++) {
            buf.append((paths[i] instanceof Dir) ? ((Dir) paths[i]).dir.getAbsolutePath() : ((Zip) paths[i]).zipFilePath);
            buf.append(File.pathSeparatorChar);
        }

        return buf.toString();
    }
}
