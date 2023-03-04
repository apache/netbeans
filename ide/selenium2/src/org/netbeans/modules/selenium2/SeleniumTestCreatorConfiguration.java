/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
