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
