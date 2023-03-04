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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class PanelOptionsVisualTest extends NbTestCase {
    
    public PanelOptionsVisualTest(final String name) {
        super(name);
    }
    
    
    public void testMainClassName() {
        assertEquals("project.Project", PanelOptionsVisual.createMainClassName("Project")); //NOI18N
        assertEquals("test.project.TestProject", PanelOptionsVisual.createMainClassName("Test Project"));   //NOI18N        
        assertEquals("test001.Test001", PanelOptionsVisual.createMainClassName("Test001")); //NOI18N
        assertEquals("test.project001.TestProject001", PanelOptionsVisual.createMainClassName("Test Project001")); //NOI18N
        assertEquals("test.project.pkg001.TestProject001", PanelOptionsVisual.createMainClassName("Test Project 001"));   //NOI18N
        assertEquals("pkg001.Main", PanelOptionsVisual.createMainClassName("001"));    //NOI18N
        assertEquals("testing.pkgfor.keywords.TestingForKeywords", PanelOptionsVisual.createMainClassName("Testing for keywords"));    //NOI18N
        assertEquals("pkgfor.For", PanelOptionsVisual.createMainClassName("For"));    //NOI18N
        assertEquals("test.project.TestProject", PanelOptionsVisual.createMainClassName("Test-Project"));   //NOI18N        
        assertEquals("testproject.TestProject", PanelOptionsVisual.createMainClassName("TestProject"));   //NOI18N
        assertEquals("pkg001.app.App", PanelOptionsVisual.createMainClassName("001 App"));   //NOI18N
        assertEquals("Main", PanelOptionsVisual.createMainClassName(""));   //NOI18N
        assertEquals("project.Project",PanelOptionsVisual.createMainClassName(" Project")); //NOI18N
    }

}
