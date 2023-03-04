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
package org.openide.filesystems.multifs;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.filesystems.localfs.LocalFSTest;
import org.openide.filesystems.xmlfs.XMLFSTest;
import org.openide.filesystems.xmlfs.XMLFSTest.ResourceComposer;

import org.netbeans.performance.DataManager;
import org.netbeans.performance.DataDescriptor;

/**
 * Base class for simulation of module layers. It creates several layers, each filled
 * with some number of .instance files. Each layer is zipped into one jar. 
 * The jars also contain class files.
 */
public class MultiXMLFSTest extends FSTest implements DataManager {
    
    public static final String XMLFS_NO_KEY = "XMLFS_NO";
    private FileWrapper[] wrappers;
    private static final String RES_EXT = ".instance";
    private MultiFileSystem mfs;
    
    protected List ddescs;
    
    // used for testCreateXMLFS
    private URL[] resources;
    
    private static final String getResource(int base) {
        return LocalFSTest.getPackage(base).replace('/', '-').concat(LocalFSTest.RES_NAME);
    }
    
    /** Creates new XMLFSGenerator */
    public MultiXMLFSTest(String name) {
        super(name);
        init();
    }

    /** Creates new XMLFSGenerator */
    public MultiXMLFSTest(String name, Object[] args) {
        super(name, args);
        init();
    }
    
    /** init */
    private void init() {
        ddescs = new ArrayList();
    }
    
    /** Set up given number of FileObjects */
    public FileObject[] setUpFileObjects(int foCount) throws Exception {
        
        int fsCount = getIntValue(XMLFS_NO_KEY);
        int foChunk = foCount / fsCount;
        int delta = foCount - (foCount / fsCount) * fsCount;
        
        int last = wrappers.length;
        FileSystem[] fss = new FileSystem[last];
        int[] bases = new int[last];
        resources = new URL[last];
        
        for (int i = 0; i < last; i++) {
            if (wrappers[i].isLocal()) {
                LocalFileSystem lfs = new LocalFileSystem();
                File mnt = wrappers[i].getMnt();
                if (mnt == null) {
                    wrappers[i] = createLocal(wrappers[i].getFoCount(), wrappers[i].getFoBase());
                }
                lfs.setRootDirectory(wrappers[i].getMnt());
                fss[i] = lfs;
            } else {
                URLClassLoader cloader = new URLClassLoader(new URL[] { wrappers[i].getMnt().toURL() });
                URL res = cloader.findResource(wrappers[i].getXResource());
                resources[i] = res;
                XMLFileSystem xmlfs = new XMLFileSystem();
                xmlfs.setXmlUrl(res, false);
                fss[i] = xmlfs;
            }
            
            if (i > 0) {
                bases[i] = bases[i - 1] + foChunk;
            }
        }
        
        FileObject[] ret = new FileObject[foCount];
        mfs = new MultiFileSystem(fss);
        for (int i = 0; i < last; i++) {
            FileObject res = mfs.findResource(LocalFSTest.getPackage(bases[i]));
            FileObject[] tmp = res.getChildren();
            int pos = i * foChunk + Math.min(i, 1) * delta;
            System.arraycopy(tmp, 0, ret, pos, tmp.length);
        }        
        
        return ret;
    }
    
    /** Empty */
    protected void postSetUp() {
    }
    
    /** Creates args for this instance of Benchmark */
    protected Map[] createArguments() {
        Map[] map = super.createArguments();
        Map[] newMap = new Map[map.length * 2];
        
        System.arraycopy(map, 0, newMap, 0, map.length);
        
        for (int i = map.length; i < newMap.length; i++) {
            newMap[i] = cloneMap(map[i - map.length]);
            newMap[i].put(XMLFS_NO_KEY, new Integer(50));
        }
        
        return newMap;
    }
    
    /** Creates a Map with default arguments values */
    protected Map createDefaultMap() {
        Map map = super.createDefaultMap();
        map.put(XMLFS_NO_KEY, new Integer(10));
        return map;
    }    
    
    /** Clones given Map by casting to a cloneable class - HashMap, Hashtable, or TreeMap */
    private static final Map cloneMap(Map toClone) {
        if (toClone instanceof HashMap) {
            return (Map) ((HashMap) toClone).clone();
        } else if (toClone instanceof Hashtable) {
            return (Map) ((Hashtable) toClone).clone();
        } else if (toClone instanceof TreeMap) {
            return (Map) ((TreeMap) toClone).clone();
        }
        
        return null;
    }
    
    /** @return this mfs */
    public MultiFileSystem getMultiFileSystem() {
        return mfs;
    }
    
    /** @return wrappers array */
    public FileWrapper[] getFileWrappers() {
        return wrappers;
    }

    /** Creates a FileWrapper suitable for mounting a LocalFileSystem */
    private static FileWrapper createLocal(int foCount, int foBase) throws Exception {
        File mnt = createTempFolder();
        LocalFSTest.createFiles(foCount, 0, mnt);
        return new FileWrapper(mnt, mnt, foCount, foBase, true, null);
    }
    
    /** Creates a FileWrapper suitable for mounting an XMLFileSystem */
    private static FileWrapper createXMLinJar(int foCount, int foBase) throws Exception {
        File tmp = createTempFolder();
        File destFolder = LocalFSTest.createFiles(foCount, foBase, tmp);
        compileFolder(tmp, destFolder);
        File xmlbase = XMLFSTest.generateXMLFile(destFolder, new ResourceComposer(getResource(foBase), RES_EXT, foCount, foBase));
        File jar = Utilities.createJar(tmp, "jarxmlfs.jar");
        String xres = LocalFSTest.getPackage(foBase) + xmlbase.getName();
        return new FileWrapper(tmp, jar, foCount, foBase, false, xres);
    }
    
    /** Compiles folder */
    private static void compileFolder(File root, File destFolder) throws Exception {
        File[] files = destFolder.listFiles();
        //StringBuffer sb = new StringBuffer(3000);
        String[] args = new String[files.length + 3];
        args[0] = "javac";
        args[1] = "-classpath";
        args[2] = System.getProperty("java.class.path");
        
        for (int i = 3; i < args.length; i++) {
            args[i] = files[i - 3].getCanonicalPath();
        }
        
        File stdlog = new File(root, "stdcompilerlog.txt");
        File errlog = new File(root, "errcompilerlog.txt");
        
        PrintStream stdps = new PrintStream(new FileOutputStream(stdlog));
        PrintStream errps = new PrintStream(new FileOutputStream(errlog));
        
        Process p = Runtime.getRuntime().exec(args);
        CopyMaker cma, cmb;
        Thread tha = new Thread(cma = new CopyMaker(p.getInputStream(), stdps));
        tha.start();
        Thread thb = new Thread(cmb = new CopyMaker(p.getErrorStream(), errps));
        thb.start();
        
        p.waitFor();
        tha.join();
        thb.join();
        
        stdps.close();
        errps.close();
        
        if (cma.e != null) {
            throw cma.e;
        }
        if (cmb.e != null) {
            throw cmb.e;
        }
    }
    
    /** Called after tearDown()  */
    public void tearDownData() throws Exception {
        for (Iterator it = ddescs.iterator(); it.hasNext(); ) {
            MFSDataDescriptor dd = (MFSDataDescriptor) it.next();
            FileWrapper[] wrappers = dd.getFileWrappers();
            if (wrappers != null) {
                for (int i = 0; i < wrappers.length; i++) {
                    delete(wrappers[i].getRootDir());
                }
            }
        }
    }
    
    /** Called before setUp()  */
    public DataDescriptor createDataDescriptor() {
        return new MFSDataDescriptor(getIntValue(FILE_NO_KEY), getIntValue(XMLFS_NO_KEY));
    }
    
    /** Called before setUp()  */
    public void setUpData(DataDescriptor ddesc) throws Exception {
        MFSDataDescriptor dd = (MFSDataDescriptor) ddesc;
        ddescs.add(dd);
        FileWrapper fwrappers[] = dd.getFileWrappers();
        
        if (fwrappers == null) {
            int foCount = dd.getFoCount();
            int fsCount = dd.getFsCount();
            int foChunk = foCount / fsCount;
            int delta = foCount - (foCount / fsCount) * fsCount;
            wrappers = new FileWrapper[fsCount];
            int[] bases = new int[fsCount];
            for (int i = 1; i < fsCount; i++) {
                int ibase = i * foChunk;
                wrappers[i] = createXMLinJar(foChunk, ibase);
                bases[i] = ibase;
            }

            wrappers[0] = createLocal(foChunk + delta, 0);
            dd.setFileWrappers(wrappers);
        } else {
            wrappers = fwrappers;
        }
    }
    
    // test method
    public void testCreateXMLFS() throws Exception {
        int iters = iterations;
        FileWrapper[] wrappers = this.wrappers;
        int len = wrappers.length;
        while (iters-- > 0) {
            // first is LocalFS
            for (int i = 1; i < len; i++) {
                XMLFileSystem xmlfs = new XMLFileSystem();
                xmlfs.setXmlUrl(resources[i], false);
            }
        }
    }
    
    static final class CopyMaker implements Runnable {
        InputStream is;
        PrintStream os;
        Exception e;
        
        CopyMaker(InputStream is, PrintStream os) {
            this.is = is;
            this.os = os;
        }
        
        public void run() {
            try {
                Utilities.copyIS(is, os);
            } catch (Exception ee) {
                e = ee;
            }
        }
    }
    
    /** Wraps Files */
    public static final class FileWrapper implements Serializable {
        private transient File rootDir;
        private transient File mnt;
        private int foCount;
        private int foBase;
        
        private boolean isLocal;
        
        // xml specific
        private String xresource;
        
        /** New FileWrapper */
        public FileWrapper(File rootDir, File mnt, int foCount, int foBase, boolean isLocal, String xresource) {
            this.rootDir = rootDir;
            this.mnt = mnt;
            this.foCount = foCount;
            this.foBase = foBase;
            this.isLocal = isLocal;
            this.xresource = xresource;
        }
        
        public File getRootDir() {
            return rootDir;
        }
        
        public File getMnt() {
            return mnt;
        }
        
        public int getFoCount() {
            return foCount;
        }
        
        public int getFoBase() {
            return foBase;
        }
        
        public boolean isLocal() {
            return isLocal;
        }
        
        public String getXResource() {
            return xresource;
        }
        
        private void writeObject(ObjectOutputStream obtos) throws IOException {
            obtos.defaultWriteObject();
            if (! isLocal()) {
                Utilities.writeFile(getMnt(), obtos);
            }
        }
        
        private void readObject(ObjectInputStream obtis) throws IOException, ClassNotFoundException {
            obtis.defaultReadObject();
            if (! isLocal()) {
                rootDir = createTempFolder();
                mnt = Utilities.readFile(rootDir, obtis);
            }
        }
    }
    
    /*
    public static void main(String[] args) throws Exception {
        MultiXMLFSTest mtest = new MultiXMLFSTest("first test");
        mtest.setUpFileObjects(500);
        System.out.println("done");
        
        System.out.println(mtest.wrappers[1].getClassLoader().loadClass("org.openide.filesystems.data50.JavaSrc55"));
    }
     */
}
