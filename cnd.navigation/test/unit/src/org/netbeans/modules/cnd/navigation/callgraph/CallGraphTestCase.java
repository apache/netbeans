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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.navigation.callgraph;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;

/**
 *
 */
public class CallGraphTestCase extends ProjectBasedTestCase {

    public CallGraphTestCase(String testName) {
        super(testName, true);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }

    @Override
    protected File getUserDir() {
        File userDir = super.getUserDir();
        String path =userDir.getAbsolutePath().replace("cnd.modelimpl", "cnd.navigation/build");
        return new File(path);
    }

    @Override
    public File getDataDir() {
        File dataDir = super.getDataDir();
        String path =dataDir.getAbsolutePath().replace("cnd.navigation/build", "cnd.modelimpl");
        File goldDataDir = new File(path);
        assertTrue(goldDataDir.exists());
        return goldDataDir;
    }

    protected final File getQuoteDataDir() {
        return Manager.normalizeFile(new File(getDataDir(), "common/quote_nosyshdr"));
    }

    public void testCallGraph() throws Exception {
        CsmProject project = getProject();
        CsmFile quote = null;
        for(CsmFile file : project.getAllFiles()){
            if (file.getAbsolutePath().toString().endsWith("quote.cc")) {
                quote = file;
                break;
            }
        }
        assertNotNull(quote);
        CsmDeclaration main = null;
        for(CsmDeclaration decl : quote.getDeclarations()) {
            if (decl.getName().toString().startsWith("main")) {
                main = decl;
                break;
            }
        }
        assertNotNull(main);
        CallModelImpl graph = new CallModelImpl(project, (CsmFunction) main);
        List<Call> callees_main = graph.getCallees(graph.getRoot());
        assertNotNull(callees_main);
        assertEquals(callees_main.size(), 7);
        Map<String,Function> map = new TreeMap<String,Function>();
        for(Call call : callees_main) {
            map.put(call.getCallee().getName(), call.getCallee());
        }
        assertEquals(7,map.size());
        assertNotNull(map.get("readNumberOf"));
        assertNotNull(map.get("getDiscountFor"));
        assertNotNull(map.get("outCustomersList"));
        assertNotNull(map.get("fetchCustomersList"));
        assertNotNull(map.get("readChar"));
        assertNotNull(map.get("GetSupportMetric"));
        assertNotNull(map.get("AddModule"));

        Function addModule = map.get("AddModule");
        List<Call> callees_addModule = graph.getCallees(addModule);
        assertEquals(1, callees_addModule.size());

        Function getSupportMetric = callees_addModule.get(0).getCallee();
        assertNotNull(getSupportMetric);
        assertEquals(getSupportMetric.getName(), "GetSupportMetric");

        List<Call> callers_getSupportMetric = graph.getCallers(getSupportMetric);
        assertEquals(4, callers_getSupportMetric.size());
        map = new TreeMap<String,Function>();
        for(Call call : callers_getSupportMetric) {
            map.put(call.getCaller().getName(), call.getCallee());
        }
        assertEquals(4, map.size());
        assertNotNull(map.get("operator ="));
        assertNotNull(map.get("Module"));
        assertNotNull(map.get("operator <<"));
        assertNotNull(map.get("AddModule"));
    }
}
