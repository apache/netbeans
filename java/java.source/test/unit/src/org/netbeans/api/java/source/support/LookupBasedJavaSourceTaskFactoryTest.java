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
package org.netbeans.api.java.source.support;

import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Lahoda
 */
public class LookupBasedJavaSourceTaskFactoryTest extends NbTestCase {
    
    public LookupBasedJavaSourceTaskFactoryTest(String testName) {
        super(testName);
    }

    private FileObject testDir;
    private FileObject testFile1;
    private FileObject testFile2;
    private DataObject testFile1DO;
    private DataObject testFile2DO;
    private Node testFile1Node;
    private Node testFile2Node;
    
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.setLookup(new Object[]{}, getClass().getClassLoader());
        testDir = SourceUtilsTestUtil.makeScratchDir(this);
        testFile1 = testDir.createData("test1.java");
        testFile2 = testDir.createData("test2.java");
        testFile1DO = DataObject.find(testFile1);
        testFile2DO = DataObject.find(testFile2);
        testFile1Node = testFile1DO.getNodeDelegate();
        testFile2Node = testFile2DO.getNodeDelegate();
    }
    
    public void testFactoryListensOnLookupChanges() throws Exception {
        // PENDING - correct ?
        int[] changeCount = new int[] { 1 };
        LookupBasedJavaSourceTaskFactory factory = new LookupBasedJavaSourceTaskFactoryImpl(changeCount);
        ChangeableLookup lookup = new ChangeableLookup();
        
        factory.setLookup(lookup);
        
        assertEquals(1, changeCount[0]);
        assertEquals(0, factory.getFileObjects().size());
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.singleton(testFile1)});
        
        assertEquals(2, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile1, factory.getFileObjects().get(0));
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.singleton(testFile2)});
        
        assertEquals(3, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile2, factory.getFileObjects().get(0));
        
        lookup.setLookupsImpl(new Lookup[] {});
        
        assertEquals(4, changeCount[0]);
        assertEquals(0, factory.getFileObjects().size());
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.fixed(new Object[] {testFile1, testFile2})});
        
        assertEquals(5, changeCount[0]);
        assertEquals(2, factory.getFileObjects().size());
        assertEquals(new HashSet<FileObject>(Arrays.asList(testFile1, testFile2)), new HashSet<FileObject>(factory.getFileObjects()));
        
        lookup.setLookupsImpl(new Lookup[] {});
        
        assertEquals(6, changeCount[0]);
        assertEquals(0, factory.getFileObjects().size());
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.singleton(testFile1DO)});
        
        assertEquals(7, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile1, factory.getFileObjects().get(0));
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.fixed(new Object[] {testFile1DO, testFile1Node})});
        
        assertEquals(7, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile1, factory.getFileObjects().get(0));
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.singleton(testFile2Node)});
        
        assertEquals(8, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile2, factory.getFileObjects().get(0));
    }

    private static class LookupBasedJavaSourceTaskFactoryImpl extends LookupBasedJavaSourceTaskFactory {
        
        private int[] changeCount;
        
        public LookupBasedJavaSourceTaskFactoryImpl(int[] changeCount) {
            super(Phase.PARSED, Priority.MIN);
            this.changeCount = changeCount;
        }
        
        public CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new CancellableTask<CompilationInfo>() {
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public void run(CompilationInfo parameter) throws Exception {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
        
        @Override
        protected void lookupContentChanged() {
            changeCount[0]++;
        }
        
    }
    
    private static class ChangeableLookup extends ProxyLookup {
        
        public void setLookupsImpl(Lookup[] lookups) {
            setLookups(lookups);
        }
    }
}
