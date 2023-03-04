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
package org.netbeans.api.java.source.test.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class MemoryValidator extends NbTestCase {
    
    private static final boolean ENABLED = Boolean.getBoolean("org.netbeans.api.java.source.test.support.MemoryValidator.enable");
    
    private TestCase delegate;
    
    /** Creates a new instance of MemoryValidator */
    public MemoryValidator(TestCase delegate) {
        super(delegate.getName());
        
        this.delegate = delegate;
    }
    
    public static Test wrap(Test t) {
        if (t instanceof TestCase) {
            return wrap((TestCase) t);
        }
        if (t instanceof TestSuite) {
            return wrap((TestSuite) t);
        }
        
        throw new IllegalArgumentException("Unknown type to wrap");
    }
    
    public static TestCase wrap(TestCase t) {
        return new MemoryValidator(t);
    }
    
    public static TestSuite wrap(TestSuite t) {
        TestSuite result = new TestSuite();
        
        for (int cntr = 0; cntr < t.testCount(); cntr++) {
            result.addTest(wrap(t.testAt(cntr)));
        }
        
        return result;
    }

    private static Logger log = Logger.getLogger(MemoryValidator.class.getName());
    protected @Override void runTest() throws Throwable {
        delegate.runBare();
        
        if (ENABLED) {
            //if the tests passes, check if all the DataObjects created during the test are reclaimable.
            //the same for all corresponding JavaSources.
            long start = System.currentTimeMillis();
            long end = -1;
            
            try {
                Collection<FileObject> allFileObjects = null;
                
                try {
                    Class poolClass = Class.forName("org.openide.loaders.DataObjectPool");
                    Method getPOOL = poolClass.getDeclaredMethod("getPOOL", new Class[0]);
                    getPOOL.setAccessible(true);
                    Object pool = getPOOL.invoke(null, new Object[0]);
                    Field m = poolClass.getDeclaredField("map");
                    m.setAccessible(true);
                    
                    Map<FileObject, Object> map = (Map) m.get(pool);
                    
                    allFileObjects = new HashSet(map.keySet());
                } catch  (ThreadDeath t) {
                    throw t;
                } catch (Throwable t) {
                    if (log.isLoggable(Level.SEVERE))
                        log.log(Level.SEVERE, t.getMessage(), t);
                }
                
                if (allFileObjects != null) {
                    for (Iterator<FileObject> i = allFileObjects.iterator(); i.hasNext(); ){
                        FileObject file = i.next();
                        
                        i.remove();
                        
                        String name = FileUtil.getFileDisplayName(file);
                        DataObject d = DataObject.find(file);
                        JavaSource s = JavaSource.forFileObject(d.getPrimaryFile());
                        
                        if (s != null) {
                            Reference rD = new WeakReference(d);
                            Reference sD = new WeakReference(s);
                            
                            file = null;
                            d = null;
                            s = null;
                            
                            NbTestCase.assertGC(name, rD);
                            NbTestCase.assertGC(name, sD);
                        }
                    }
                }
                
                end = System.currentTimeMillis();
            } finally {
                if (end != (-1)) {
                    log(getName() + ": reference check took: " + (end - start));
                } else {
                    log(getName() + ": reference check failed");
                }
            }
        }
    }

}
