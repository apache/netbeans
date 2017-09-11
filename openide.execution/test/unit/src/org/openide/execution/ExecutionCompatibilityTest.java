/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
