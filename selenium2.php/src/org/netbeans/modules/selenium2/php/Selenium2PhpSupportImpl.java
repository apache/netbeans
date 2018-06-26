/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
            return Collections.EMPTY_LIST;
        }
        PhpSeleniumProvider seleniumProvider = getSeleniumProvider(p);
        if(seleniumProvider == null) {
            return Collections.EMPTY_LIST;
        }
        // selenium test dir was not set by user during configureProject()
        // user probably pressed Cancel. Do not bother him with extra
        // configuration dialogs.
        if(getSeleniumDir(p, false) == null) {
            return Collections.EMPTY_LIST;
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
