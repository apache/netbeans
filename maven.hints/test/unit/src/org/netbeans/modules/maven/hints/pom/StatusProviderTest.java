/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.hints.pom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class StatusProviderTest extends NbTestCase {

    public StatusProviderTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testModelLoading() throws Exception { // #212152
        // new File(StatusProviderTest.class.getResource(...).toURI()) may not work in all environments, e.g. testdist
        File pom = new File(getWorkDir(), "pom.xml");
        InputStream is = StatusProviderTest.class.getResourceAsStream("pom-with-warnings.xml");
        try {
            OutputStream os = new FileOutputStream(pom);
            try {
                FileUtil.copy(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
        String warnings = StatusProvider.runMavenValidationImpl(pom).toString().replace(pom.getAbsolutePath(), "pom.xml");
        assertEquals("["
                + "[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-compiler-plugin is missing. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 22, column 12, "
                + "[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-surefire-plugin is missing. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 72, column 12, "
                + "[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-war-plugin is missing. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 64, column 12]"
//#223562                + "[WARNING] The <reporting> section is deprecated, please move the reports to the <configuration> section of the new Maven Site Plugin. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 102, column 13, "
                // not reported since upgrade to 3.3.9 + "[WARNING] 'reporting.plugins.plugin.version' for org.apache.maven.plugins:maven-surefire-report-plugin is missing. @ test:mavenproject4:1.0-SNAPSHOT, pom.xml, line 127, column 12]"
                , warnings);
    }

}
