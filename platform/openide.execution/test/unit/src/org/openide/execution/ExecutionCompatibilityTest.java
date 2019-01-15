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

package org.openide.execution;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.execution.ExecutionEngine;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Entry point to the whole execution compatibility suite.
 *
 * @author Jaroslav Tulach
 */
public class ExecutionCompatibilityTest {

    /** Creates a new instance of ExecutionCompatibilityTest */
    private ExecutionCompatibilityTest() {
    }
    
    /** Executes the execution compatibility kit on the default implementation of the
     * ExecutionEngine.
     */
    public static Test suite() {
        return suite(null);
    }
    
    /** Executes the execution compatibility kit tests on the provided instance
     * of execution engine.
     */
    public static Test suite(ExecutionEngine engine) {
        System.setProperty("org.openide.util.Lookup", ExecutionCompatibilityTest.class.getName() + "$Lkp");
        Object o = Lookup.getDefault();
        if (!(o instanceof Lkp)) {
            Assert.fail("Wrong lookup object: " + o);
        }
        
        Lkp l = (Lkp)o;
        l.assignExecutionEngine(engine);
        
        if (engine != null) {
            Assert.assertEquals("Same engine found", engine, ExecutionEngine.getDefault());
        } else {
            o = ExecutionEngine.getDefault();
            Assert.assertNotNull("Engine found", o);
            Assert.assertEquals(ExecutionEngine.Trivial.class, o.getClass());
        }
        
        TestSuite ts = new TestSuite();
        ts.addTestSuite(ExecutionEngineHid.class);
        
        return ts;
    }
    
    /** Default lookup used in the suite.
     */
    public static final class Lkp extends AbstractLookup {
        private InstanceContent ic;
        
        public Lkp() {
            this(new InstanceContent());
        }
        private Lkp(InstanceContent ic) {
            super(ic);
            this.ic = ic;
        }
        
        final void assignExecutionEngine(ExecutionEngine executionEngine) {
//          ic.setPairs(java.util.Collections.EMPTY_LIST);
            if (executionEngine != null) {
                ic.add(executionEngine);
            }
        }
        
        
    }
}
