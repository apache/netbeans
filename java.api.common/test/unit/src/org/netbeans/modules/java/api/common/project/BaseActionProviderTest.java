/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.project;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.junit.Test;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class BaseActionProviderTest extends NbTestCase {

    private APImpl ap;

    public BaseActionProviderTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final Project prj = TestProject.createMultiModuleProject(wd);
        final TestProject tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        final BaseActionProvider.Callback cb = new BaseActionProvider.Callback() {
            @Override
            public ClassPath getProjectSourcesClassPath(String type) {
                return ClassPath.EMPTY;
            }
            @Override
            public ClassPath findClassPath(FileObject file, String type) {
                return ClassPath.getClassPath(file, type);
            }
        };
        createBuildScript(tp);
        ap = new APImpl(
                tp,
                tp.getUpdateHelper(),
                tp.getEvaluator(),
                tp.getSourceRoots(),
                tp.getTestRoots(),
                tp.getUpdateHelper().getAntProjectHelper(),
                cb);
        JavaActionProviderTestSupport.setUserPropertiesPolicy(
                JavaActionProviderTestSupport.getDelegate(ap),
                ActionProviderSupport.UserPropertiesPolicy.RUN_ANYWAY);
    }

    @Test
    public void testOverridenGetTargetNames() throws Exception {
        assertNotNull(ap);
        final Logger log = Logger.getLogger(ActionProviderSupport.class.getName());
        final Level origLevel = log.getLevel();
        final MockHandler handler = new MockHandler();
        log.setLevel(Level.FINE);
        log.addHandler(handler);
        try {
            SwingUtilities.invokeAndWait(() -> {
                ap.invokeAction(ActionProvider.COMMAND_CLEAN, Lookup.EMPTY);
            });
            assertEquals("1", handler.props.get("a"));      //NOI18N
            assertEquals("2", handler.props.get("b"));      //NOI18N
        } finally {
            log.setLevel(origLevel);
            log.removeHandler(handler);
        }
    }

    private static FileObject createBuildScript(@NonNull final Project p) throws IOException {
        final FileObject bs = FileUtil.createData(p.getProjectDirectory(), "build.xml");    //NOI18N
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(bs.getOutputStream(), FileEncodingQuery.getEncoding(bs)))) {
            out.println("<project name='test' default='clean'>");   //NOI18N
            out.println("<target name='clean'/>");  //NOI18N
            out.println("</project>");  //NOI18N
        }
        return bs;
    }

    private static final class MockHandler extends Handler {

        private final Map<Object,Object> props = new Properties();

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            if (msg != null && msg.startsWith("runTargets:")) { //NOI18N
                props.putAll((Properties)record.getParameters()[1]);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    private static class APImpl extends BaseActionProvider {

        public APImpl(Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testRoots, AntProjectHelper antProjectHelper, Callback callback) {
            super(project, updateHelper, evaluator, sourceRoots, testRoots, antProjectHelper, callback);
        }

        @Override
        protected String[] getPlatformSensitiveActions() {
            return new String[] {};
        }

        @Override
        protected String[] getActionsDisabledForQuickRun() {
            return new String[]{};
        }

        @Override
        public Map<String, String[]> getCommands() {
            final Map<String,String[]> m = new HashMap<>();
            m.put(ActionProvider.COMMAND_CLEAN, new String[]{"clean"});
            return m;
        }

        @Override
        protected Set<String> getScanSensitiveActions() {
            return Collections.emptySet();
        }

        @Override
        protected Set<String> getJavaModelActions() {
            return Collections.emptySet();
        }

        @Override
        protected boolean isCompileOnSaveEnabled() {
            return false;
        }

        @Override
        public String[] getSupportedActions() {
            return new String[] {ActionProvider.COMMAND_CLEAN};
        }

        @Override
        public String[] getTargetNames(String command, Lookup context, Properties p, boolean djc) throws IllegalArgumentException {
            p.setProperty("a", "1");    //NOI18N
            String[] res = super.getTargetNames(command, context, p, djc);
            p.setProperty("b", "2");    //NOI18N
            return res;
        }
    }
}
