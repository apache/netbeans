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

import org.netbeans.performance.Benchmark;
import org.openide.filesystems.*;

/**
 * FSTest is a base class for FileSystem tests. It defines a lot of methods that
 * exploit interface of the FileSystem class. It tests operations that change
 * state of a FileSystem.
 */
public abstract class FSTest extends ReadOnlyFSTest {

    public static final String ATTRIBUTES_NO_KEY = "ATTRIBUTES_NO";
    
    /** number of attributes (taken into account) for given run */
    protected int attrsCount;
    
    /** Creates new Tests */
    public FSTest(String name) {
        super(name);
    }
    
    /** Creates new Tests */
    public FSTest(String name, Object[] args) {
        super(name, args);
    }
    
    /** inherited; sets up env */
    protected void setUp() throws Exception {
        super.setUp();
        if (shouldDefAttrNo()) {
            attrsCount = getIntValue(ATTRIBUTES_NO_KEY);
        }
        postSetup();
    }
    
    /** Hook for operations right after the setup */
    protected void postSetup() throws Exception {
        // setup some attributes
        if (getName().startsWith("testGetAttributes")) {
            testSetOneAttributeSeq(1);
        }
    }
    
    /** Disposes given FileObjects  */
    protected void tearDownFileObjects(FileObject[] fos) throws Exception {
        // setup some attributes
        if (getName().startsWith("testGetAttributes")) {
            unsetOneAttributeSeq();
        }
    }
    
    /** Creates a Map with default arguments values */
    protected Map createDefaultMap() {
        Map map = super.createDefaultMap();
        if (shouldDefAttrNo()) {
            map.put(ATTRIBUTES_NO_KEY, new Integer(2));
        }
        if (getName().startsWith("testSet")) {
            narrow(map);
        }
        return map;
    }
    
    /** Decides whether attributes number should be defined */
    private boolean shouldDefAttrNo() {
        return getName().startsWith("testSetMany");
    }
    
    /** Creates arguments for this instance of Benchmark (not for given configuration) */
    protected Map[] createArguments() {
        if (shouldDefAttrNo()) {
            Map[] ret = new Map[2];
            ret[0] = createDefaultMap();

            ret[1] = createDefaultMap();
            ret[1].put(ATTRIBUTES_NO_KEY, new Integer(5));
            return ret;
        } else {
            return super.createArguments();
        }
    }
    
    /** Sets FILE_NO_KEY to one tenth of its original value */
    private static final void narrow(Map map) {
        Integer in = (Integer) map.get(FILE_NO_KEY);
        int ival = Math.max(in.intValue() / 10, 10); 
        map.put(FILE_NO_KEY, new Integer(ival));
    }
    
    //--------------------------------------------------------------------------
    //------------------------- attributes section -----------------------------
    
    /** Sets one random attribute for all FileObjects (their no. given by the
     * parameter). Attributes are added sequentially. Only one iteration
     */
    private void testSetOneAttributeSeq(int xiterations) throws IOException {
        FileObject[] files = this.files;
        String[][] pairs = this.pairs;
        
        for (int it = 0; it < xiterations; it++) {
            for (int i = 0; i < files.length; i++) {
                files[i].setAttribute(pairs[i][0], pairs[i][1]);
            }
        }
    }
    
    /** Unsets some attributes */
    private void unsetOneAttributeSeq() throws IOException {
        FileObject[] files = this.files;
        String[][] pairs = this.pairs;
        
        for (int i = 0; i < files.length; i++) {
            files[i].setAttribute(pairs[i][0], null);
        }
    }
    
    /** Sets one random attribute for all FileObjects (their no. given by the
     * parameter). Attributes are added sequentially.
     */
    public void testSetOneAttributeSeq() throws IOException {
        testSetOneAttributeSeq(iterations);
    }
    
    /** Sets many random attributes for all FileObjects (their no. given by the 
     * parameter). Attributes are added sequentially.
     */
    public void testSetManyAttributesSeq() throws IOException {
        FileObject[] files = this.files;
        String[][] pairs = this.pairs;
        int iterations = this.iterations;
        
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < files.length; i++) {
                for (int j = 0; (j < pairs.length) && (j < attrsCount); j++) {
                    files[i].setAttribute(pairs[j][0], pairs[j][1]);
                }
            }
        }
    }
    
    /** Sets one random attribute for all FileObjects (their no. given by the
     * parameter). Attributes are added randomly.
     */
    public void testSetOneAttributeRnd() throws IOException {
        FileObject[] files = this.files;
        String[][] pairs = this.pairs;
        int iterations = this.iterations;
        int perm[] = this.perm;
        
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < files.length; i++) {
                files[perm[i]].setAttribute(pairs[i][0], pairs[i][1]);
            }
        }
    }    
    
    /** Sets many random attributes for all FileObjects (their no. given by the 
     * parameter). Attributes are added randomly.
     */
    public void testSetManyAttributesRnd() throws IOException {
        FileObject[] files = this.files;
        String[][] pairs = this.pairs;
        int iterations = this.iterations;
        int perm[] = this.perm;
        
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < files.length; i++) {
                for (int j = 0; (j < pairs.length) && (j < attrsCount); j++) {
                    files[perm[i]].setAttribute(pairs[j][0], pairs[j][1]);
                }
            }
        }
    }
}
