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
package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleInstaller;
import org.netbeans.ModuleManager;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.Dependency;

/**
 *
 * @author Jaroslav Tulach
 */
public class NbProblemDisplayerTest extends NbTestCase {
    
    public NbProblemDisplayerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testSimpleDepOnJava() throws Exception {
        StringBuilder writeTo = new StringBuilder();
        Set<ProblemModule> modules = new HashSet<ProblemModule>();

        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "root.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_JAVA, "Java > 1.30"));
            pm.addAttr("OpenIDE-Module-Name", "RootModule");
            modules.add(pm);
        }
        
        NbProblemDisplayer.problemMessagesForModules(writeTo, modules, true);

        String msg = writeTo.toString();
        if (msg.indexOf("RootModule") == -1) {
            fail("There should be noted the root module: " + msg);
        }
    }
    public void testFindTheRootCause() throws Exception {
        StringBuilder writeTo = new StringBuilder();
        Set<ProblemModule> modules = new HashSet<ProblemModule>();

        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "root.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_JAVA, "Java > 1.30"));
            pm.addAttr("OpenIDE-Module-Name", "RootModule");
            modules.add(pm);
        }

        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "dep.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_MODULE, "root.module"));
            pm.addAttr("OpenIDE-Module-Name", "DepModule");
            modules.add(pm);
        }
        
        NbProblemDisplayer.problemMessagesForModules(writeTo, modules, true);

        String msg = writeTo.toString();
        if (msg.indexOf("DepModule") >= 0) {
            fail("There should not be be name of dep.module: " + msg);
        }

        Locale.setDefault(Locale.US);

        if (msg.toUpperCase().indexOf("ANOTHER MODULE") == -1) {
            fail("There should be note about one missing module: " + msg);
        }
    }
    public void testFindTheRootCauseForMoreCausesAtOnce() throws Exception {
        StringBuilder writeTo = new StringBuilder();
        Set<ProblemModule> modules = new HashSet<ProblemModule>();

        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "root.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_JAVA, "Java > 1.30"));
            pm.addAttr("OpenIDE-Module-Name", "RootModule");
            modules.add(pm);
        }
        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "root2.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_JAVA, "Java > 1.35"));
            pm.addAttr("OpenIDE-Module-Name", "Root2Module");
            modules.add(pm);
        }
        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "root3.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_JAVA, "Java > 1.40"));
            pm.addAttr("OpenIDE-Module-Name", "Root3Module");
            modules.add(pm);
        }

        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "dep.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_MODULE, "root.module"));
            pm.addAttr("OpenIDE-Module-Name", "DepModule");
            modules.add(pm);
        }
        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "dep2.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_MODULE, "root2.module"));
            pm.addAttr("OpenIDE-Module-Name", "Dep2Module");
            modules.add(pm);
        }
        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "dep3.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_MODULE, "root3.module"));
            pm.addAttr("OpenIDE-Module-Name", "Dep3Module");
            modules.add(pm);
        }
        
        NbProblemDisplayer.problemMessagesForModules(writeTo, modules, true);

        String msg = writeTo.toString();
        if (msg.indexOf("DepModule") >= 0) {
            fail("There should not be be name of dep.module: " + msg);
        }

        Locale.setDefault(Locale.US);

        if (msg.toUpperCase().indexOf("3 FURTHER") == -1) {
            fail("There should be note about 3 missing modules: " + msg);
        }
        if (msg.toUpperCase().indexOf("DEP3") >= 0) {
            fail("Nothing about Dep3: " + msg);
        }
        if (msg.toUpperCase().indexOf("DEP2") >= 0) {
            fail("Nothing about Dep2: " + msg);
        }
        if (msg.toUpperCase().indexOf("1.35") == -1) {
            fail("Something about Root2: " + msg);
        }
        if (msg.toUpperCase().indexOf("1.40") == -1) {
            fail("Something about Root3: " + msg);
        }
    }

    private static class ProblemModule extends Module {
        private static final Inst INST = new Inst();
        private static final ModuleManager MGR = new ModuleManager(INST, new NbEvents());

        private Map<String,String> attrs = new HashMap<String,String>();
        private Set<Dependency> problems = new HashSet<Dependency>();
        private final Manifest manifest;

        public ProblemModule(Manifest m) throws IOException {
            super(MGR, null, null, ProblemModule.class.getClassLoader());
            manifest = m;
            parseManifest();
        }

        public void addProblem(Set<Dependency> d) {
            problems.addAll(d);
        }

        public void addAttr(String key, String value) {
            attrs.put(key, value);
        }


        public List<File> getAllJars() {
            return Collections.emptyList();
        }

        public void setReloadable(boolean r) {
        }

        public void reload() throws IOException {
        }

        protected void classLoaderUp(Set<Module> parents) throws IOException {
        }

        protected void classLoaderDown() {
        }

        protected void cleanup() {
        }

        protected void destroy() {
        }

        public boolean isValid() {
            return true;
        }

        public boolean isFixed() {
            return false;
        }

        public Object getLocalizedAttribute(String attr) {
            return attrs.get(attr);
        }

        public Set<Object> getProblems() {
            return new HashSet<Object>(problems);
        }

        public @Override Manifest getManifest() {
            return manifest;
        }

    } // end of ProblemModule

    private static final class Inst extends ModuleInstaller {
        public void prepare(Module m) throws InvalidException {
        }

        public void dispose(Module m) {
        }

        public void load(List<Module> modules) {
        }

        public void unload(List<Module> modules) {
        }

        public boolean closing(List<Module> modules) {
            return true;
        }

        public void close(List<Module> modules) {
        }
    } // end of ModuleInstaller
}
