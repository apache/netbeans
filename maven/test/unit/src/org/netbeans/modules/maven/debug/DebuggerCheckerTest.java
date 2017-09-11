/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.debug;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
        
    }
}
