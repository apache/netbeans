/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectimport.eclipse.gui;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ImportSourceFilters extends ProjectImporterTestCase {
    WizardOperator importWizard; 
    static final String projectName = "ExcludesIncludesProject"; 
    public ImportSourceFilters(String testName) {
        super(testName);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ExtractToWorkDir(getDataDir(), "testdata.jar");
    }

    public void testImportSourceFilters() {
        importProject(projectName);
        validate();
    }

    private void importProject(String projectName) {
        importWizard = invokeImporterWizard();
        selectProjectFromWS(importWizard,"testdata", projectName);
        importWizard.finish();

        waitForProjectsImporting();

        try {
            NbDialogOperator issuesWindow = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.projectimport.eclipse.core.Bundle", "MSG_ImportIssues"));
            issuesWindow.close();
        } catch (Exception e) {
            // ignore 
        }        
    }

    private void validate() {
        NbDialogOperator propsDialog = invokeProjectPropertiesDialog(projectName,"Sources");
        
        String btnCaption = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "CustomizerSources.includeExcludeButton");
        JButtonOperator btn = new JButtonOperator(propsDialog,btnCaption);
        btn.pushNoBlock();
        String customizerCaption = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "CustomizerSources.title.includeExclude");

        NbDialogOperator customizer = new NbDialogOperator(customizerCaption);
        
        JTextFieldOperator includesBox = new JTextFieldOperator(customizer,1);
        JTextFieldOperator excludesBox = new JTextFieldOperator(customizer,0);

        log(includesBox.getText());
        log(excludesBox.getText());
        
        if(!includesBox.getText().contains("IncludeOne*.java")) {
            fail("Includes doesn't contain expected "+"IncludeOne*.java"+" mask");
        }
        if(!includesBox.getText().contains("IncludeTwo*.java")) {
            fail("Includes doesn't contain expected "+"IncludeTwo*.java"+" mask");
        }
        if(!includesBox.getText().contains("IncludeThree*.java")) {
            fail("Includes doesn't contain expected "+"IncludeThree*.java"+" mask");
        } 
        if(!excludesBox.getText().contains("ExcludeThree*.java")) {
            fail("Excludes doesn't contain expected "+"ExcludeThree*.java"+" mask");
        }
        if(!excludesBox.getText().contains("ExcludeTwo*.java")) {
            fail("Excludes doesn't contain expected "+"ExcludeTwo*.java"+" mask");
        }   
        if(!excludesBox.getText().contains("ExcludeOne*.java")) {
            fail("Excludes doesn't contain expected "+"ExcludeOne*.java"+" mask");
        }           
        customizer.close();
        propsDialog.close();
    }
}
