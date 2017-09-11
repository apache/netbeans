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
package org.netbeans.modules.selenium2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration;
import org.netbeans.modules.selenium2.api.Selenium2Support;
import org.netbeans.modules.selenium2.spi.Selenium2SupportImpl;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 * @author Theofanis Oikonomou
 */
public class SeleniumTestCreatorConfiguration extends TestCreatorConfiguration {
    
    private final FileObject[] activatedFileObjects;

    SeleniumTestCreatorConfiguration(FileObject[] activatedFileObjects) {
        assert activatedFileObjects != null;
        this.activatedFileObjects = activatedFileObjects;
    }
    
    
    /**
     *
     * @param framework the value of framework
     * @return the boolean
     */
    @Override
    public boolean canHandleProject(String framework) {
        return framework.equals(TestCreatorProvider.FRAMEWORK_SELENIUM);
    }

    @Override
    public void persistConfigurationPanel(Context context) {
        
    }

    @Override
    public Pair<String, String> getSourceAndTestClassNames(FileObject fileObj, boolean isTestNG, boolean isSelenium) {
        String[] result = {"", ""};
        Project p = FileOwnerQuery.getOwner(fileObj);
        if (p != null) {
            Selenium2SupportImpl selenium2Support = Selenium2Support.findSelenium2Support(p);
            if(selenium2Support != null) {
                result = selenium2Support.getSourceAndTestClassNames(fileObj, isTestNG, isSelenium);
            }
        }
        return Pair.of(result[0], result[1]);
    }

    

    @Override
    public Object[] getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject fo) {
        List<Object> folders = new ArrayList<Object>();
        Project p = FileOwnerQuery.getOwner(fo);
        if (p != null) {
            Selenium2SupportImpl selenium2Support = Selenium2Support.findSelenium2Support(p);
            if(selenium2Support != null) {
                folders = selenium2Support.getTestSourceRoots(createdSourceRoots, fo);
            }
        }
        return folders.toArray();
    }
    
}
