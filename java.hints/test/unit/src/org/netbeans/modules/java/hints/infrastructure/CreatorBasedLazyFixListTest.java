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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        
        list[0] = new CreatorBasedLazyFixList(null, "", 0, Collections.singleton((ErrorRule) new ErrorRule() {
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
