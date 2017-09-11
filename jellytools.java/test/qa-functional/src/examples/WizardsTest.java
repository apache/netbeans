/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package examples;

import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

public class WizardsTest extends JellyTestCase {

    /** Constructor required by JUnit */
    public WizardsTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        return createModuleTest(WizardsTest.class);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    /** Test new file wizard using predefined operators. */
    public void testWizards() {
        // open new file wizard
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject("SampleProject");
        nfwo.selectCategory("Java");
        nfwo.selectFileType("Java Class");
        // go to next page
        nfwo.next();
        // create operator for the next page
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.txtObjectName().typeText("MyNewClass");
        // finish wizard
        //nfnlso.finish();
        // cancel wizard
        nfnlso.cancel();
    }

    /** Test new project wizard using generic WizardOperator. */
    public void testGenericWizards() {
        // open new project wizard
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Java Web");
        npwo.selectProject("Web Application");
        npwo.next();
        // create operator for next page
        WizardOperator wo = new WizardOperator("Web Application");
        JTextFieldOperator txtName = new JTextFieldOperator((JTextField) new JLabelOperator(wo, "Project Name:").getLabelFor());
        txtName.clearText();
        txtName.typeText("MyApp");
        wo.cancel();
    }
}
