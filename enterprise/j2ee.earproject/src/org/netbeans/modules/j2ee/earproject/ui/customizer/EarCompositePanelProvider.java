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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint, tmysik
 */
public class EarCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";
    
    private static final String EAR = "Ear";
    public static final String RUN = "Run";
    public static final String COMPILE = "Compile";
//    private static final String RUN_TESTS = "RunTests";

    private String name;
    
    /** Creates a new instance of EarCompositePanelProvider */
    public EarCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle(CustomizerProviderImpl.class);
        ProjectCustomizer.Category toReturn = null;
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    bundle.getString("LBL_Config_Sources"), // NOI18N
                    null);
        } else if (LIBRARIES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    LIBRARIES,
                    bundle.getString("LBL_Config_Libraries"), // NOI18N
                    null);
        } else if (EAR.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    EAR,
                    bundle.getString("LBL_Config_Ear"), // NOI18N
                    null);
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    bundle.getString("LBL_Config_Run"), // NOI18N
                    null);
        } else if (COMPILE.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    COMPILE,
                    bundle.getString("LBL_Config_Compile"), // NOI18N
                    null);
        }
        assert toReturn != null : "No category for name:" + name;
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        EarProjectProperties uiProps = context.lookup(EarProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerGeneral(uiProps);
        } else if (LIBRARIES.equals(nm)) {
            return new CustomizerLibraries(uiProps);
        } else if (EAR.equals(nm)) {
            return new CustomizerJarContent(uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps);
        } else if (COMPILE.equals(nm)) {
            return new CustomizerCompile(uiProps);
        }
        return new JPanel();
    }

    public static EarCompositePanelProvider createSources() {
        return new EarCompositePanelProvider(SOURCES);
    }

    public static EarCompositePanelProvider createLibraries() {
        return new EarCompositePanelProvider(LIBRARIES);
    }

    public static EarCompositePanelProvider createEar() {
        return new EarCompositePanelProvider(EAR);
    }

    public static EarCompositePanelProvider createRun() {
        return new EarCompositePanelProvider(RUN);
    }
    
    public static EarCompositePanelProvider createCompile() {
        return new EarCompositePanelProvider(COMPILE);
    }
}
