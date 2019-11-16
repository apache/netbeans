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

    public void testRunSingleMethodDisabledForJUnit5() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<version>1.0</version>"
                + "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>2.22.2</version></plugin></plugins></build>"
                + "<dependencies><dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter-api</artifactId><version>5.5.2</version><scope>test</scope></dependency><dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter-params</artifactId><version>5.5.2</version><scope>test</scope></dependency><dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter-engine</artifactId><version>5.5.2</version><scope>test</scope></dependency></dependencies>"
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
