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
