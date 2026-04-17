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
package org.netbeans.modules.maven.debug;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import static junit.framework.TestCase.fail;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class DebuggerCheckerTest extends NbTestCase {
    private final DebuggerChecker ch = new DebuggerChecker();
    
    public DebuggerCheckerTest(String n) {
        super(n);
    }
    
    public void testDebugAttachTrue() {
        MockConfig mc = new MockConfig();
        mc.setProperty("jpda.attach", "true");
        boolean res = ch.checkRunConfig(mc);
        assertTrue(res);
        int port = Integer.parseInt(mc.getProperties().get("jpda.attach.port"));
        assertTrue(port > 0);
        String localhost = mc.getProperties().get("jpda.attach.address");
        assertEquals("localhost", localhost);
        String both = mc.getProperties().get("jpda.attach");
        assertEquals(localhost + ":" + port, both);
    }
    
    private static final class MockConfig implements RunConfig, Project {
        private Map<String,String> props = new HashMap<>();
        private Map<String,String> options = new HashMap<>();

        @Override
        public File getExecutionDirectory() {
            fail();
            return null;
        }

        @Override
        public void setExecutionDirectory(File directory) {
            fail();
        }

        @Override
        public RunConfig getPreExecution() {
            fail();
            return null;
        }

        @Override
        public void setPreExecution(RunConfig config) {
            fail();
        }

        @Override
        public ReactorStyle getReactorStyle() {
            fail();
            return null;
        }

        @Override
        public Project getProject() {
            return this;
        }

        @Override
        public MavenProject getMavenProject() {
            fail();
            return null;
        }

        @Override
        public List<String> getGoals() {
            fail();
            return null;
        }

        @Override
        public String getExecutionName() {
            fail();
            return null;
        }

        @Override
        public String getTaskDisplayName() {
            fail();
            return null;
        }

        @Override
        public String getActionName() {
            return "debug";
        }

        @Override
        public Map<? extends String, ? extends String> getProperties() {
            return Collections.unmodifiableMap(props);
        }

        @Override
        public void setProperty(String key, String value) {
            props.put(key, value);
        }

        @Override
        public void addProperties(Map<String, String> properties) {
            fail();
        }

        @Override
        public void setInternalProperty(String key, Object value) {
            fail();
        }

        @Override
        public Map<? extends String, ? extends Object> getInternalProperties() {
            fail();
            return null;
        }

        @Override
        public boolean isShowDebug() {
            fail();
            return false;
        }

        @Override
        public boolean isShowError() {
            fail();
            return false;
        }

        @Override
        public Boolean isOffline() {
            fail();
            return null;
        }

        @Override
        public void setOffline(Boolean bool) {
            fail();
        }

        @Override
        public boolean isRecursive() {
            fail();
            return false;
        }

        @Override
        public boolean isUpdateSnapshots() {
            fail();
            return false;
        }

        @Override
        public List<String> getActivatedProfiles() {
            fail();
            return null;
        }

        @Override
        public void setActivatedProfiles(List<String> profiles) {
            fail();
        }

        @Override
        public boolean isInteractive() {
            fail();
            return false;
        }

        @Override
        public FileObject getSelectedFileObject() {
            fail();
            return null;
        }

        @Override
        public FileObject getProjectDirectory() {
            fail();
            return null;
        }

        @Override
        public Lookup getLookup() {
            fail();
            return null;
        }

        @Override
        public Map<? extends String, ? extends String> getOptions() {
            return Collections.unmodifiableMap(options);
        }

        @Override
        public void setOption(String key, String value) {
            options.put(key, value);
        }

        @Override
        public void addOptions(Map<String, String> properties) {
            fail();
        }
        
    }
}
