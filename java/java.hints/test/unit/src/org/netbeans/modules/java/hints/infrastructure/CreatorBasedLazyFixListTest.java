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
package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class CreatorBasedLazyFixListTest extends HintsTestBase {
    
    public CreatorBasedLazyFixListTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCancel() throws Exception {
        prepareTest("Simple");
        
        final int[] calledCount = new int[1];
        final AtomicBoolean[] cancel = new AtomicBoolean[1];
        final CreatorBasedLazyFixList[] list = new CreatorBasedLazyFixList[1];
        
        list[0] = new CreatorBasedLazyFixList(null, "", "", 0, Collections.singleton((ErrorRule) new ErrorRule() {
            public Set getCodes() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public List<Fix> run(CompilationInfo compilationInfo,
                    String diagnosticKey, int offset, TreePath treePath,
                    Data data) {
                calledCount[0]++;
                if (cancel[0] != null) {
                    cancel[0].set(true);
                    list[0].cancel();
                }
                
                return Collections.<Fix>emptyList();
            }
            
            public void cancel() {
                //expected&ignored for now.
            }
            
            public String getId() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public String getDisplayName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public String getDescription() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }), new HashMap<Class, Data>());
        
        cancel[0] = new AtomicBoolean();
        
        list[0].compute(info, cancel[0]);
        
        assertEquals(1, calledCount[0]);
        
        cancel[0] = new AtomicBoolean();
        
        list[0].compute(info, cancel[0]);
        
        assertEquals(2, calledCount[0]);
        
        cancel[0] = null;
        
        list[0].compute(info, new AtomicBoolean());
        
        assertEquals(3, calledCount[0]);
        
        cancel[0] = null;
        
        list[0].compute(info, new AtomicBoolean());
        
        assertEquals(3, calledCount[0]);
    }

    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/";
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
    
}
