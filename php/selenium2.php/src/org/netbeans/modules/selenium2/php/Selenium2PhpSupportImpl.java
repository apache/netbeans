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
package org.netbeans.modules.selenium2.php;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.php.project.api.PhpSeleniumProvider;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.selenium2.server.api.Selenium2Server;
import org.netbeans.modules.selenium2.spi.Selenium2SupportImpl;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service = Selenium2SupportImpl.class)
public class Selenium2PhpSupportImpl extends Selenium2SupportImpl {

    @Override
    public boolean isSupportActive(Project p) {
        return getSeleniumProvider(p) != null;
    }

    @Override
    public void configureProject(FileObject targetFolder) {
        Project p = FileOwnerQuery.getOwner(targetFolder);
        if (p == null) {
            return;
        }
        getSeleniumDir(p, true);
    }

    @Override
    public WizardDescriptor.Panel createTargetChooserPanel(WizardDescriptor wiz) {
        Project project = Templates.getProject(wiz);
        SourceGroup seleniumSourceGroup = getSeleniumSourceGroup(project);
        return Templates.buildSimpleTargetChooser(project, new SourceGroup[]{seleniumSourceGroup}).create();
    }

    @Override
    public String getTemplateID() {
        return "Templates/SeleniumTests/SeleneseTest.php";
    }

    @Override
    public boolean isSupportEnabled(FileObject[] activatedFOs) {
        if (activatedFOs.length == 0) {
            return false;
        }
        
        Project p = FileOwnerQuery.getOwner(activatedFOs[0]);
        if (p == null) {
            return false;
        }
        PhpSeleniumProvider seleniumProvider = getSeleniumProvider(p);
        if(seleniumProvider == null) {
            return false;
        }
        return seleniumProvider.isSupportEnabled(activatedFOs);
    }

    private PhpSeleniumProvider getSeleniumProvider(Project project) {
        return project.getLookup().lookup(PhpSeleniumProvider.class);
    }

    private FileObject getSeleniumDir(Project project, boolean showCustomizer) {
        return getSeleniumProvider(project).getTestDirectory(showCustomizer);
    }
    
    @NbBundle.Messages("sources_display_name=Selenium Sources")
    private SourceGroup getSeleniumSourceGroup(Project project) {
        FileObject dir = getSeleniumDir(project, true);
        if (dir == null) {
            return ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC)[0];
        }
        String sourcesDisplayName = Bundle.sources_display_name();
        return GenericSources.group(project, dir, "SeleniumDir", sourcesDisplayName, null, null);
    }

    @Override
    public List<Object> getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject refFileObject) {
        configureProject(refFileObject);
        Project p = FileOwnerQuery.getOwner(refFileObject);
        if (p == null) {
            return Collections.<Object>emptyList();
        }
        PhpSeleniumProvider seleniumProvider = getSeleniumProvider(p);
        if(seleniumProvider == null) {
            return Collections.<Object>emptyList();
        }
        // selenium test dir was not set by user during configureProject()
        // user probably pressed Cancel. Do not bother him with extra
        // configuration dialogs.
        if(getSeleniumDir(p, false) == null) {
            return Collections.<Object>emptyList();
        }
        return seleniumProvider.getTestSourceRoots(createdSourceRoots, refFileObject);
    }

    @Override
    public String[] getSourceAndTestClassNames(FileObject fo, boolean isTestNG, boolean isSelenium) {
        String[] result = {"", ""};
        Project p = FileOwnerQuery.getOwner(fo);
        if (p != null) {
            Collection<? extends ClassPathProvider> providers = p.getLookup().lookupAll(ClassPathProvider.class);
            for (ClassPathProvider provider : providers) {
                ClassPath cp = provider.findClassPath(fo, PhpSourcePath.SOURCE_CP);
                if (cp != null) {
                    result[0] =  cp.getResourceName(fo, '.', false);
                    result[1] = result[0].concat(TestCreatorProvider.TEST_CLASS_SUFFIX);
                }
            }
        }
        return result;
    }

    @Override
    public void runTests(FileObject[] activatedFOs, boolean isSelenium) {
        if (isSelenium) {
            Project p = FileOwnerQuery.getOwner(activatedFOs[0]);
            if (p != null) {
                PhpSeleniumProvider seleniumProvider = getSeleniumProvider(p);
                if (seleniumProvider != null) {
                    Selenium2Server.getInstance().startServer();
                    seleniumProvider.runAllTests();
                    Selenium2Server.getInstance().stopServer();
                }
            }
        }
    }
    
}
