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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.qa.form.options;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.qa.form.OptionsForFormOperator;

/**
 * Componentes declaration test
 *
 * @author Jiri Vagner
 *
 * <b>Adam Senk</b> 26 APRIL 2011 WORKS
 */
public class GeneratedComponentsDestionationTest extends ExtJellyTestCase {

    /**
     * Constructor required by JUnit
     */
    public GeneratedComponentsDestionationTest(String testName) {
        super(testName);
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(GeneratedComponentsDestionationTest.class).addTest(
                "testGeneratedComponentsDestionationLocal",
                "testGeneratedComponentsDestionationClassField").clusters(".*").enableModules(".*").gui(true));
    }

    /**
     * Tests generation component declaration code with properties
     * LocalVariables=true Test for issue 95518
     */
    public void testGeneratedComponentsDestionationLocal() {
        testGeneratedComponentsDestionation(true);
    }

    /**
     * Tests generation component declaration code with properties
     * LocalVariables=false
     */
    public void testGeneratedComponentsDestionationClassField() {
        testGeneratedComponentsDestionation(false);
    }

    /**
     * Tests generation component declaration code with properties
     * LocalVariables=false
     *
     * @param local "Local Variables" settings
     */
    private void testGeneratedComponentsDestionation(Boolean local) {
        OptionsForFormOperator.invoke();
        //add timeout
        waitNoEvent(1000);
        log("Option dialog was opened");

        OptionsForFormOperator options = new OptionsForFormOperator();


        //add timeout
        waitNoEvent(1000);
        if (local) {
            options.selectJava();
            //add timeout
            waitNoEvent(1000);
            JTabbedPaneOperator jtpo = new JTabbedPaneOperator(options);
            jtpo.selectPage("GUI Builder");
            waitNoEvent(1000);
        }
        waitNoEvent(500);

        JRadioButtonOperator jrbo = new JRadioButtonOperator(options, "Local Variables in initComponents() Method");
        //int i = 0;
        if (!local) {
            // i = 1;
            jrbo = new JRadioButtonOperator(options, "Fields in the Form Class");
        }

        //new JRadioButtonOperator(options, i);
        jrbo.setSelected(true);

        waitNoEvent(1000);
        options.ok();
        waitAMoment();

        String name = createJFrameFile();
        waitAMoment();

        FormDesignerOperator designer = new FormDesignerOperator(name);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N

                runPopupOverNode("Add From Palette|Swing Controls|Label", node); // NOI18N
            }
        });

        waitAMoment();

        String code = "private javax.swing.JLabel jLabel1";  // NOI18N
        if (local) {
            missInCode(code, designer);
        } else {
            findInCode(code, designer);
        }

        waitAMoment();
        removeFile(name);
    }
}
