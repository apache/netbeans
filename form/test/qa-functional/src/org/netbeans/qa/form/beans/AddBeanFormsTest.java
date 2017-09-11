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

package org.netbeans.qa.form.beans;

import junit.framework.Test;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests creating Bean Forms from visual and non-visual JavaBeans superclasses
 * and tests value and access rights of inherited properties
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class AddBeanFormsTest extends AddAndRemoveBeansTest {
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public AddBeanFormsTest(String testName) {
        super(testName);
        //this.DELETE_FILES = false;
    }
   
    /**
     * Creates suite from particular test cases.
     * @return nb test suite
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(AddBeanFormsTest.class)
                .addTest("testCompileBeanClasses", "testAddingBeanFormWithVisualBeanSuperclass", "testAddingBeanFormWithNonVisualBeanSuperclass")
                .clusters(".*").enableModules(".*").gui(true));
        
    }

    /** Compiling beans components */
    public void testCompileBeanClasses() {
        Node beanNode = openFile(VISUAL_BEAN_NAME);
        CompileJavaAction action = new CompileJavaAction();
        action.perform(beanNode);

        beanNode = openFile(NONVISUAL_BEAN_NAME);
        action = new CompileJavaAction();
        action.perform(beanNode);
   }
    
    /** Test adding Bean Form with visual bean superclass */
    public void testAddingBeanFormWithVisualBeanSuperclass() {
        String name = createBeanFormFile(VISUAL_BEAN_NAME);

        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.selectComponent(VISUAL_BEAN_NAME);
        Property prop = new Property(inspector.properties(), "text"); // NOI18N
        assertEquals("Text property of component " + name + " was not set correctly.",
            prop.getValue(), TESTED_BEAN_TEXT); // NOI18N

        removeFile(name);
    }

    /** Test adding Bean Form with non-visual bean superclass */
    public void testAddingBeanFormWithNonVisualBeanSuperclass() {
        String name = createBeanFormFile(NONVISUAL_BEAN_NAME);


        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.selectComponent(NONVISUAL_BEAN_NAME);
        
        Property prop = new Property(inspector.properties(), "power"); // NOI18N
        assertEquals("Text property of component " + name + " was not set correctly.",
            prop.getValue(), this.TESTED_BEAN_POWER); // NOI18N
        assertEquals("Property of component " + name + " is read-only.",
            prop.isEnabled(), true); // NOI18N

        prop = new Property(inspector.properties(), "carName"); // NOI18N
        assertEquals("Text property of component " + name + " was not set correctly.",
            prop.getValue(), TESTED_BEAN_TEXT); // NOI18N
        assertEquals("Property of component " + name + " is not read-only.",
            prop.isEnabled(), false); // NOI18N

        removeFile(name);
    }
}
