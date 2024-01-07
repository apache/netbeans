/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.classfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.utils.FileOrZipEntry;
import org.netbeans.lib.profiler.utils.MiscUtils;


/**
 * Fixed-size cache of binary classes (.class files). Used to avoid flooding memory with class files when performing intensive
 * method scanning, that may touch thousands of classes. Currently uses LRU eviction policy.
 * A separate, currently no-eviction cache, is maintained for classes supplied by the VM.
 *
 * @author Misha Dmitirev
 */
public class ClassFileCache {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static ClassFileCache defaultClassFileCache;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ClassPath classPath; // Used to quickly obtain an open JAR file for a given name
    private Hashtable<String, byte[]> vmSuppliedClassCache;
    private byte[][] classFileBytes;
    private String[] classNameAndLocation;
    private long[] lastTimeUsed;
    private int capacity;
    private int size;
    private int sizeLimit;
    private long timeCounter;
    private List<String> preloadNames;
    private List<Integer> preloadLoaderIds;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    //------------ We don't expect the below API to be used outside of this package, hence it's package-private ------------
    ClassFileCache() {
        capacity = 877; // FIXME: may be worth setting size flexibly, or adjusting inside cache if too many evictions happen
        size = 0;
        sizeLimit = (capacity * 3) / 4;
        classNameAndLocation = new String[capacity];
        classFileBytes = new byte[capacity][];
        lastTimeUsed = new long[capacity];

        vmSuppliedClassCache = new Hashtable<>();
        preloadNames = new ArrayList<>();
        preloadLoaderIds = new ArrayList<>();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    static ClassFileCache getDefault() {
        if (defaultClassFileCache == null) {
            defaultClassFileCache = new ClassFileCache();
        }

        return defaultClassFileCache;
    }

    static void resetDefaultCache() {
        defaultClassFileCache = null;
    }

    byte[] getClassFile(String name, String location) throws IOException {
        String nameAndLocation = (name + "#" + location).intern(); // NOI18N
        byte[] res;

        if (location.startsWith(ClassRepository.LOCATION_VMSUPPLIED)) {
            res = vmSuppliedClassCache.get(nameAndLocation);
            if (res != null && res.length == 0) {
                try {
                    // known class without bytecode; get it from TA
                    ProfilerClient client = TargetAppRunner.getDefault().getProfilerClient();
                    if (!preloadNames.contains(name)) {
                        preloadBytecode(name, location);
                    }
                    String names[] = preloadNames.toArray(new String[0]);
                    int loadersId[] = new int[preloadLoaderIds.size()];
                    for (int i=0; i<loadersId.length; i++) {
                        loadersId[i] = preloadLoaderIds.get(i);
                    }
                    //System.out.println("Caching "+names.length+" classes");
                    byte[][] bytes = client.getCachedClassFileBytes(names, loadersId);
                    for (int i=0; i<bytes.length; i++) {
                        res = bytes[i];
                        if (res == null) res = new byte[0];
                        //System.out.println("Get class file for " + names[i] + " " + res.length + " bytes");
                        if (res.length != 0) {
                            vmSuppliedClassCache.put(getNameAndLocation(names[i],loadersId[i]), res);
                        }
                    }
                    preloadNames = new ArrayList<>();
                    preloadLoaderIds = new ArrayList<>();
                    res = vmSuppliedClassCache.get(nameAndLocation);
                    if (res.length == 0) {
                        throw new IOException("Get class file for " + name + " not found in TA");
                    }
                } catch (ClientUtils.TargetAppOrVMTerminated ex) {
                    throw new IOException(ex);
                }
            }
        } else {
            res = get(nameAndLocation);

            if (res == null) {
                if (size > sizeLimit) {
                    removeLRUEntry();
                }

                res = readAndPut(name, location, nameAndLocation);
            }
        }

        return res;
    }

    void preloadBytecode(String name, String location) {
        String nameAndLocation = (name + "#" + location).intern(); // NOI18N
        if (location.startsWith(ClassRepository.LOCATION_VMSUPPLIED)) {
            byte[] res = (byte[]) vmSuppliedClassCache.get(nameAndLocation);
            if (res != null && res.length == 0) {
                // known class without bytecode; get it from TA
                preloadNames.add(name);
                String loaderIdStr = location.substring(ClassRepository.LOCATION_VMSUPPLIED.length());
                preloadLoaderIds.add(Integer.valueOf(loaderIdStr));
            }
        }
    }

    void addVMSuppliedClassFile(String name, int classLoaderId, byte[] buf) {
        String nameAndLocation = getNameAndLocation(name, classLoaderId); 
        vmSuppliedClassCache.put(nameAndLocation, buf);
    }

    private String getNameAndLocation(String name, int classLoaderId) {
        return (name + "#" + ClassRepository.getClassFileLoc(classLoaderId)).intern(); // NOI18N
    }

    /**
     * Returns the actual class loader id for the given class/loader pair, or -1 if class is not loaded.
     * The real loader may be the same as classLoaderId or its parent loader.
     */
    int hasVMSuppliedClassFile(String name, int classLoaderId) {
        do {
            // we are trying the whole classloader hierarchy up to the root system classloader with id=0
            String nameAndLocation = getNameAndLocation(name, classLoaderId);  
            boolean res = vmSuppliedClassCache.containsKey(nameAndLocation);

            if (res) {
                return classLoaderId;
            } else if (classLoaderId != 0) {
                classLoaderId = ClassLoaderTable.getParentLoader(classLoaderId);
            }

            if (classLoaderId == -1) {
                MiscUtils.printWarningMessage("Failed to lookup classloader for: " + name); // NOI18N

                return -1;
            }
        } while (classLoaderId != 0);

        return -1;
    }

    //---------------------------------------- Private implementation -------------------------------------------
    private byte[] get(String nameAndLocation) {
        int pos = (nameAndLocation.hashCode() & 0x7FFFFFFF) % capacity;

        while ((classNameAndLocation[pos] != null) && (classNameAndLocation[pos] != nameAndLocation)) {
            pos = (pos + 1) % capacity;
        }

        if (classNameAndLocation[pos] != null) {
            lastTimeUsed[pos] = ++timeCounter;

            return classFileBytes[pos];
        } else {
            return null;
        }
    }

    private byte[] readAndPut(String name, String classFileLocation, String nameAndLocation)
                       throws IOException {
        byte[] classFile = readClassFile(name, classFileLocation);
        int pos = (nameAndLocation.hashCode() & 0x7FFFFFFF) % capacity;

        while (classNameAndLocation[pos] != null) {
            pos = (pos + 1) % capacity;
        }

        classNameAndLocation[pos] = nameAndLocation;
        classFileBytes[pos] = classFile;
        lastTimeUsed[pos] = ++timeCounter;
        size++;

        return classFile;
    }

    private byte[] readClassFile(String name, String classFileLocation)
                          throws IOException {
        String classFileName = name + ".class"; // NOI18N
        File location = new File(classFileLocation);

        if (location.isDirectory()) {
            return MiscUtils.readFileIntoBuffer(new FileOrZipEntry(classFileLocation, classFileName));
        } else { // Should be .jar file
                 // The following code may be used at different stages of JFluid work, with different initialization states, so
                 // it's coded defensively. If it can use an available open ZipFile, it will use it, otherwise it will open its own.

            ZipFile zip = null;

            if (classPath == null) {
                classPath = ClassRepository.getClassPath();
            }

            if (classPath != null) {
                try {
                    zip = classPath.getZipFileForName(classFileLocation);
                } catch (ZipException e2) {
                    throw new IOException("Could not open archive " + classFileLocation); // NOI18N
                }
            } else {
                throw new IOException("Could not get classpath for " + classFileName + " in " + classFileLocation); // NOI18N
            }

            ZipEntry entry = zip.getEntry(classFileName);

            if (entry == null) {
                throw new IOException("Could not find entry for " + classFileName + " in " + classFileLocation); // NOI18N
            }

            int len = (int) entry.getSize();
            byte[] buf = new byte[len];
            InputStream in = zip.getInputStream(entry);
            int readBytes;
            int ofs = 0;
            int remBytes = len;

            do {
                readBytes = in.read(buf, ofs, remBytes);
                ofs += readBytes;
                remBytes -= readBytes;
            } while (ofs < len);

            in.close();

            return buf;
        }
    }

    private void removeLRUEntry() {
        long leastTime = 0x7FFFFFFFFFFFFFFFL;
        int pos = 0;

        for (int i = 0; i < capacity; i++) {
            if ((lastTimeUsed[i] > 0) && (lastTimeUsed[i] < leastTime)) {
                pos = i;
            }
        }

        classNameAndLocation[pos] = null;
        classFileBytes[pos] = null;
        lastTimeUsed[pos] = 0;
        size--;

        return;
    }
}
