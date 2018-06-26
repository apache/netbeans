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
package org.netbeans.modules.ws.qaf.rest.suite;

import java.io.File;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ws.qaf.rest.JEE6MvnCRUDTest;
import org.netbeans.modules.ws.qaf.rest.JEE6MvnFromDBTest;
import org.netbeans.modules.ws.qaf.rest.JEE6MvnPatternsTest;

/**
 * Maven Java EE 7 tests.
 *
 * @author Jiri Skrivanek
 */
public class MvnJEE7Suite extends J2eeTestCase {

    public MvnJEE7Suite(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf);//register server
        conf = conf.addTest(JEE7MvnFromDBTest.class,
                "testFromDB",
                "testRun",
                "testUndeploy");
        conf = conf.addTest(JEE7MvnCRUDTest.class,
                "testRfE", //NOI18N
                "testPropAccess", //NOI18N
                "testRun", //NOI18N
                "testCreateRestClient", //NOI18N
                "testUndeploy");
        conf = conf.addTest(JEE7MvnPatternsTest.class,
                "testSingletonDef", //NOI18N
                "testContainerIDef", //NOI18N
                "testCcContainerIDef", //NOI18N
                "testSingleton1", //NOI18N
                "testCcContainerI1", //NOI18N
                "testSingleton2", //NOI18N
                "testContainerI1", //NOI18N
                "testContainerI2", //NOI18N
                "testSingleton3", //NOI18N
                "testContainerI3", //NOI18N
                "testCcContainerI2", //NOI18N
                "testCcContainerI3", //NOI18N
                "testRun", //NOI18N
                "testUndeploy");
        return conf.suite();
    }

    public static class JEE7MvnFromDBTest extends JEE6MvnFromDBTest {

        public JEE7MvnFromDBTest(String name) {
            super(name);
        }

        @Override
        protected JavaEEVersion getJavaEEversion() {
            return JavaEEVersion.JAVAEE7;
        }
    }

    public static class JEE7MvnCRUDTest extends JEE6MvnCRUDTest {

        public JEE7MvnCRUDTest(String testName) {
            super(testName);
        }

        @Override
        protected JavaEEVersion getJavaEEversion() {
            return JavaEEVersion.JAVAEE7;
        }
    }

    public static class JEE7MvnPatternsTest extends JEE6MvnPatternsTest {

        public JEE7MvnPatternsTest(String name) {
            super(name);
        }

        @Override
        protected JavaEEVersion getJavaEEversion() {
            return JavaEEVersion.JAVAEE7;
        }
        

        @Override
        protected void closeCreatedFiles(Set<File> files) {
            for (File f : files) {
                // remove annotation just to have one set of golden files for all EE levels
                EditorOperator eo = new EditorOperator(f.getName());
                eo.setCaretPosition("RequestScoped", true);
                eo.deleteLine(eo.getLineNumber());
                eo.setCaretPosition("@RequestScoped", true);
                eo.deleteLine(eo.getLineNumber());
                eo.save();
                eo.close();
            }
        }
    }
}
