/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
