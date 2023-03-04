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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Adding ability for a NetBeans modules to provide a GUI customizer.
 *
 * @author Martin Krauskopf
 */
public final class CustomizerProviderImpl extends BasicCustomizer {
    
    // Programmatic names of categories
    static final String CATEGORY_SOURCES = "Sources"; // NOI18N
    static final String CATEGORY_DISPLAY = "Display"; // NOI18N
    static final String CATEGORY_LIBRARIES = "Libraries"; // NOI18N
    public static final String CATEGORY_VERSIONING = "Versioning"; // NOI18N
    public static final String SUBCATEGORY_VERSIONING_PUBLIC_PACKAGES = "publicPackages"; // NOI18N
    static final String CATEGORY_BUILD = "Build"; // NOI18N
    static final String CATEGORY_COMPILING = "Compiling"; // NOI18N
    static final String CATEGORY_PACKAGING = "Packaging"; // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    
    private SingleModuleProperties moduleProps;
    
    public CustomizerProviderImpl(final Project project, final AntProjectHelper helper,
            final PropertyEvaluator evaluator) {
        super(project, "Projects/org-netbeans-modules-apisupport-project/Customizer");
        this.helper = helper;
        this.evaluator = evaluator;
    }
    
    void storeProperties() throws IOException {
        moduleProps.triggerLazyStorages();
        moduleProps.storeProperties();
    }
    
    void dialogCleanup() {
        moduleProps = null;
    }
    
    protected Lookup prepareData() {
        Lookup lookup = getProject().getLookup();
        SuiteProvider sp = lookup.lookup(SuiteProvider.class);
        NbModuleType type = ((NbModuleProject) getProject()).getModuleType();
        moduleProps = new SingleModuleProperties(helper, evaluator, sp, type,lookup.lookup(LocalizedBundleInfo.Provider.class));
        return Lookups.fixed(moduleProps, getProject());
    }
}
