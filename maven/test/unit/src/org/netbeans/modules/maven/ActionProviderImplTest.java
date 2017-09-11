/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.TestFileUtils;

public class ActionProviderImplTest extends NbTestCase {

    public ActionProviderImplTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    
    public void testRunSingleMethodEnabledWhenHaveCoS() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<version>1.0</version>"
                + "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>2.7</version></plugin></plugins></build>"
                + "<dependencies><dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>3.8.2</version><scope>test</scope></dependency></dependencies>"
                + "<properties><netbeans.compile.on.save>all</netbeans.compile.on.save></properties>"
                + "</project>");
        assertSupportsRunSingleMethod(ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir())), false); // used to be true, but now all is run with maven, not JavaRunner.
    }

    public void testRunSingleMethodDisabledWhenDoNotHaveCoSExplicit() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<version>1.0</version>"
                + "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>2.7</version></plugin></plugins></build>"
                + "<dependencies><dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>4.8.2</version><scope>test</scope></dependency></dependencies>"
                + "<properties><netbeans.compile.on.save>none</netbeans.compile.on.save></properties>"
                + "</project>");
        assertSupportsRunSingleMethod(ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir())), false);
    }

    public void testRunSingleMethodDisabledWhenDoNotHaveCoSImplicit() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<version>1.0</version>"
                + "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>2.7</version></plugin></plugins></build>"
                + "<dependencies><dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>4.8.2</version><scope>test</scope></dependency></dependencies>"
                + "</project>");
        assertSupportsRunSingleMethod(ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir())), false);
    }

    public void testRunSingleMethodEnabledForSurefire28() throws Exception { // #196655
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<version>1.0</version>"
                + "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>2.8</version></plugin></plugins></build>"
                + "<dependencies><dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>4.8.2</version><scope>test</scope></dependency></dependencies>"
                + "<properties><netbeans.compile.on.save>none</netbeans.compile.on.save></properties>"
                + "</project>");
        assertSupportsRunSingleMethod(ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir())), true);
    }

    public void testRunSingleMethodDisabledForJUnit3() throws Exception { //SUREFIRE-724
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<version>1.0</version>"
                + "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>2.8</version></plugin></plugins></build>"
                + "<dependencies><dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>3.8.2</version><scope>test</scope></dependency></dependencies>"
                + "<properties><netbeans.compile.on.save>none</netbeans.compile.on.save></properties>"
                + "</project>");
        assertSupportsRunSingleMethod(ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir())), false);
    }

    public void testRunSingleMethodEnabledForUnusualJUnitScope() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<version>1.0</version>"
                + "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>2.8</version></plugin></plugins></build>"
                + "<dependencies><dependency><groupId>junit</groupId><artifactId>junit</artifactId><version>4.8.2</version></dependency></dependencies>"
                + "<properties><netbeans.compile.on.save>none</netbeans.compile.on.save></properties>"
                + "</project>");
        assertSupportsRunSingleMethod(ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir())), true);
    }

    private static void assertSupportsRunSingleMethod(Project p, boolean supports) {
        ActionProviderImpl ap = p.getLookup().lookup(ActionProviderImpl.class);
        assertNotNull(ap);
        assertTrue(ap.runSingleMethodEnabled() ^ !supports);
    }

}
