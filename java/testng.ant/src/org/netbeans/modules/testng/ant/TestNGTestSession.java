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
package org.netbeans.modules.testng.ant;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.java.testrunner.OutputUtils;
import org.netbeans.modules.testng.api.TestNGTestSuite;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 *
 * @author answer
 */
public class TestNGTestSession extends TestSession {

    private FileLocator projectFileLocator = null;
    private final List<TestNGTestSuite> suites = new ArrayList<TestNGTestSuite>();
    private TestSuite currentSuite;

    public TestNGTestSession(String name, Project project, SessionType sessionType) {
        super(name, project, sessionType);
        if (project != null) {
            projectFileLocator = new ProjectFileLocator(project);
        }
    }

    @Override
    public FileLocator getFileLocator() {
        FileLocator locator = super.getFileLocator();
        if (locator == null) {
            return projectFileLocator;
        }
        return locator;
    }

    @Override
    public void addSuite(TestSuite suite) {
        super.addSuite(suite);
        suites.add((TestNGTestSuite) suite);
    }

    @Override
    public TestSuite getCurrentSuite() {
        if (currentSuite == null) {
            return super.getCurrentSuite();
        }
        return currentSuite;
    }

    public void setCurrentSuite(String name) {
        for (TestNGTestSuite ts : suites) {
            if (ts.getName().equals(name)) {
                currentSuite = ts;
                break;
            }
        }
    }
    
    public String getSuiteName(String name) {
        int suitesNum = 0;
        for (TestNGTestSuite suite : suites) {
            if (suite.getName().startsWith(name)) {
                suitesNum++;
            }
        }
        name = suitesNum == 0 ? name : name.concat(" ").concat(Integer.toString(suitesNum));
        return name;
    }


    class ProjectFileLocator implements FileLocator {

        private ClassPath classpath;

        ProjectFileLocator(Project project) {
            this.classpath = getProjectClasspath(project);
        }

        public FileObject find(String filename) {
            return classpath.findResource(filename);
        }

        private ClassPath getProjectClasspath(Project p) {
            ClassPath result = null;
            Set<FileObject> roots = new HashSet<FileObject>();
            Sources sources = ProjectUtils.getSources(p);
            if (sources != null) {
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (SourceGroup group : groups) {
                    roots.add(group.getRootFolder());
                }
            }

            ClassPathProvider cpp = p.getLookup().lookup(ClassPathProvider.class);
            Set<ClassPath> setCP = new HashSet<ClassPath>();
            if (cpp != null) {
                for (FileObject file : roots) {
                    ClassPath path = cpp.findClassPath(file, ClassPath.COMPILE);
                    setCP.add(path);
                }
            }

            for (ClassPath cp : setCP) {
                FileObject[] rootsCP = cp.getRoots();
                for (FileObject fo : rootsCP) {
                    try {
                        FileObject[] aaa = SourceForBinaryQuery.findSourceRoots(fo.toURL()).getRoots();
                        roots.addAll(Arrays.asList(aaa));
                    } catch (Exception e) {
                    }
                }
            }

            String platformId = null;
            try {
                Method evalMethod = p.getClass().getDeclaredMethod("evaluator"); //NOI18N
                PropertyEvaluator evaluator = (PropertyEvaluator) evalMethod.invoke(p);
                if (evaluator != null) {
                    platformId = evaluator.getProperty("platform.active");
                }
            } catch (Exception ex) {
            }

            JavaPlatform platform = OutputUtils.getActivePlatform(platformId); //NOI18N
            if (platform != null) {
                roots.addAll(Arrays.asList(platform.getSourceFolders().getRoots()));
            }

            result = ClassPathSupport.createClassPath(roots.toArray(new FileObject[0]));

            return result;
        }
    }
}
