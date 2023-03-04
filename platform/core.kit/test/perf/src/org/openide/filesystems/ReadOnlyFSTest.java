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
package org.openide.filesystems;

import java.io.IOException;
import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import org.netbeans.performance.Benchmark;
import org.netbeans.performance.MapArgBenchmark;
import org.openide.filesystems.*;

/**
 * ReadOnlyFSTest is a base class for FileSystem tests. It defines a lot of methods that
 * exploit interface of the FileSystem class, note, however, that it uses only operations
 * that do not change state of the FileSystem.
 */
public abstract class ReadOnlyFSTest extends MapArgBenchmark {
    
    public static final String FILE_NO_KEY = "FILE_NO";
    
    /** number of files for given run */
    protected int foCount;
    /** iterations for given run */
    protected int iterations;
    /** FileObjects for given run */
    protected FileObject[] files;
    /** String attrs for given run */
    protected String[][] pairs;
    /** Gives permutation */
    protected int[] perm;

    /** Creates new Tests */
    public ReadOnlyFSTest(String name) {
        super(name);
        setArgumentArray(createArguments());
    }
    
    /** Creates new Tests */
    public ReadOnlyFSTest(String name, Object[] args) {
        super(name, args);
    }
    
    /** inherited; sets up env */
    protected void setUp() throws Exception {
        Map param = (Map) getArgument();
        foCount = ((Integer) param.get(FILE_NO_KEY)).intValue();
        iterations = getIterationCount();
        files = setUpFileObjects(foCount);
        pairs = generateRandomStrings(new String[files.length][2]);
        perm = shuffleIntArray(fillIntArray(new int[files.length]));
    }
    
    /** Set up given number of FileObjects */
    protected abstract FileObject[] setUpFileObjects(int foCount) throws Exception;
    
    /** Disposes given FileObjects */
    protected abstract void tearDownFileObjects(FileObject[] fos) throws Exception;
    
    /** Shuts down this test */
    protected void tearDown() throws Exception {
        tearDownFileObjects(files);
    }
    
    /** Creates arguments for this instance of Benchmark (not for given configuration) */
    protected Map[] createArguments() {
        Map[] ret = new Map[1];
        ret[0] = createDefaultMap();
        return ret;
    }
    
    /** Creates a Map with default arguments values */
    protected Map createDefaultMap() {
        Map map = super.createDefaultMap();
        map.put(FILE_NO_KEY, new Integer(1000));
        return map;
    }    
    
    //--------------------------------------------------------------------------
    //------------------------- attributes section -----------------------------
    
    /** Gets all attributes for all FileObjects (their no. given by the 
     * parameter). Attributes are acquired sequentially.
     */
    public void testGetAttributesSeq() throws IOException {
        int iterations = this.iterations;
        
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < files.length; i++) {
                Enumeration enum = files[i].getAttributes();
                while (enum.hasMoreElements()) {
                    String attr = (String) enum.nextElement();
                    Object val = files[i].getAttribute(attr);
                }
            }
        }
    }
     
    /** Gets all attributes for all FileObjects (their no. given by the 
     * parameter). Attributes are acquired randomly.
     */
    public void testGetAttributesRnd() throws IOException {
        List list = new ArrayList(files.length + 3);
        int iterations = this.iterations;
        
        for (int it = 0; it < iterations; it++) {
            list.clear();
            for (int i = 0; i < files.length; i++) {
                list.add(files[i].getAttributes());
            }

            for (int i = 0; i < files.length; i++) {
                Enumeration enum = (Enumeration) list.get(i);
                if (enum.hasMoreElements()) {
                    String key = (String) enum.nextElement();
                    files[i].getAttribute(key);
                }
            }
        }
    }
    
    //--------------------------------------------------------------------------
    //------------------------- utility methods --------------------------------
    
    /** Remove all attributes for given files */
    public void cleanUpAttributes(FileObject[] files) throws Exception {
        for (int i = 0; i < files.length; i++) {
            Enumeration enum = files[i].getAttributes();
            while (enum.hasMoreElements()) {
                String attr = (String) enum.nextElement();
                files[i].setAttribute(attr, null);
            }
        }        
    }

    /** Fills in  an array of ints so that arr[i] == i */
    public static final int[] fillIntArray(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        
        return arr;
    }
    
    /** Shuffles an int array so that arr[i] == i is not very likely */
    public static final int[] shuffleIntArray(int[] arr) {
        Random rnd = new Random(97943);
        
        for (int i = 0; i < arr.length; i++) {
            int next = rnd.nextInt(arr.length);
            swap(arr, i, next);
        }
        
        return arr;
    }
    
    /** Swaps integers from idxa and idxb in the arr array */
    private static void swap(int[] arr, int idxa, int idxb) {
        if (idxa == idxb) {
            return;
        }
        
        int tmp = arr[idxa];
        arr[idxa] = arr[idxb];
        arr[idxb] = tmp;
    }
    
    /** Generates random String pairs */
    public static final String[][] generateRandomStrings(String[][] arr) {
        Random rnd = new Random(97943);
        
        for (int i = 0; i < arr.length; i++) {
            arr[i][0] = String.valueOf(rnd.nextInt());
            arr[i][1] = String.valueOf(rnd.nextInt());
        }
        
        return arr;
    }
    
    /** Creates temporary folder with a random name */
    public static File createTempFolder() throws IOException {
        File tmp = File.createTempFile("local", "lacol");
        String name = tmp.getName();
        File folder = tmp.getParentFile();
        tmp.delete();

        folder = new File(folder, name);
        folder.mkdir();

        return folder;
    }
    
    /** Deletes (recursively) a folder */
    public static void delete(File folder) throws Exception {
        if (folder == null) {
            return;
        }
        
        File[] files = folder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    delete(files[i]);
                }
                files[i].delete();
            }
        }
        
        folder.delete();
    }    
}
