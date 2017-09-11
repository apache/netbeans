/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.api.debugger;

import java.util.List;
import org.netbeans.api.debugger.test.TestLookupServiceFirst;
import org.netbeans.api.debugger.test.TestNodeModelContext;
import org.netbeans.spi.viewmodel.NodeModel;

/**
 *
 * @author Martin Entlicher
 */
public class LookupWithForPathTest  extends DebuggerApiTestBase {

    static {
        String[] layers = new String[] {"org/netbeans/api/debugger/test/mf-layer.xml"};//NOI18N
        Object[] instances = new Object[] { };
        IDEInitializer.setup(layers,instances);
    }

    public LookupWithForPathTest(String s) {
        super(s);
    }

    public void testForPath() throws Exception {
        Lookup.MetaInf l = new Lookup.MetaInf("unittest");
        List list = l.lookup(null, TestLookupServiceFirst.class);
        assertEquals("Wrong looked up object", 2, list.size());
        assertInstanceOf("Wrong looked up object", list.get(0), TestLookupServiceFirst.class);
        assertInstanceOf("Wrong looked up object", list.get(1), TestLookupServiceFirst.class);

        Lookup testContext = new Lookup.Instance(new Object[] {});
        l.setContext(testContext);
        List<? extends NodeModel> nodeModelList = l.lookup(null, NodeModel.class);
        assertEquals("Wrong looked up object", 2, nodeModelList.size());
        assertInstanceOf("Wrong looked up object", nodeModelList.get(0), NodeModel.class);
        assertInstanceOf("Wrong looked up object", nodeModelList.get(1), NodeModel.class);
        assertInstanceOf("Wrong looked up object", nodeModelList.get(1), TestNodeModelContext.class);
        TestNodeModelContext nmc = (TestNodeModelContext) nodeModelList.get(1);
        assertNotNull(nmc.getContext());
        assertEquals(testContext, nmc.getContext());
    }
}
