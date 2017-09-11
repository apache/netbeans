/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.prep.syntax;

import org.netbeans.jellytools.nodes.Node;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.css.prep.GeneralCSSPrep;

/**
 *
 * @author Vladimir Riha (vriha)
 */
public class SassSyntaxTest extends GeneralCSSPrep{
    
    public static final String PROJECT_NAME = "css_prep";
    
    public SassSyntaxTest(String args){
        super(args);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SassSyntaxTest.class).addTest(
                "openProject",
                "testSyntaxErrors").enableModules(".*").clusters(".*"));
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects(SassSyntaxTest.PROJECT_NAME);
        evt.waitNoEvent(3000);
        openFile("sass_syntax.scss", LessSyntaxTest.PROJECT_NAME);
        endTest();
    }
    
    public void testSyntaxErrors(){
        startTest();
        EditorOperator eo = new EditorOperator("sass_syntax.scss");
        eo.setCaretPositionToEndOfLine(1);
        eo.pressKey(java.awt.event.KeyEvent.VK_ENTER);
        eo.save();
        Object[] annotations = getAnnotations(eo, 0);
        String ideal = "Unknown property colors";
         for (Object object : annotations) {
            String desc = EditorOperator.getAnnotationShortDescription(object);
            desc = desc.replaceAll("<.*>", "");
            assertTrue("Expected: " + ideal + " but found: " + desc.trim(), desc.trim().startsWith(ideal));
        }
        endTest();
    }
   
}
