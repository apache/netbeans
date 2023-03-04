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
package org.netbeans.modules.javascript2.editor.qaf.nav;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author vriha
 */
public class TypeDefNavTest extends GeneralJavaScript {

    public TypeDefNavTest(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(TypeDefNavTest.class).addTest(
                        "openProject",
                        "testTypDef1",
                        "testTypDef2",
                        "testTypDef3",
                        "testTypDef4").enableModules(".*").clusters(".*"));
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("completionTest");
        evt.waitNoEvent(2000);
        GeneralJavaScript.currentFile = "typdef.js";
        openFile("typdef.js", "completionTest");
        endTest();
    }

    public void testTypDef1() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("typdef.js"), 94);
        endTest();
    }
    public void testTypDef2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("typdef.js"), 96);
        endTest();
    }
    public void testTypDef3() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("typdef.js"), 100);
        endTest();
    }
    public void testTypDef4() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("typdef.js"), 102);
        endTest();
    }
 
    
}
