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
package org.netbeans.modules.web.el.navigation;

import org.netbeans.modules.web.el.ELTestBaseForTestProject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ELDeclarationFinderTest extends ELTestBaseForTestProject {

    public ELDeclarationFinderTest(String name) {
        super(name);
    }

    public void testSimpleBean() throws Exception {
        checkDeclaration("projects/testWebProject/web/navigation/navigation01.xhtml", "#{bea^n}", "Bean.java", 314);
    }

    public void testBeanProperty() throws Exception {
        checkDeclaration("projects/testWebProject/web/navigation/navigation02.xhtml", "#{bean.myArr^ay}", "Bean.java", 525);
    }

    public void testBeanPropertyField() throws Exception {
        checkDeclaration("projects/testWebProject/web/navigation/navigation03.xhtml", "#{bean.myCypris.ge^tName()}", "Cypris.java", 103);
    }

    public void testResourceBundleIdentifier() throws Exception {
        checkDeclaration("projects/testWebProject/web/navigation/navigation04.xhtml", "#{bu^ndle.cokolivJineho}", "Messages.properties", 0);
    }

    public void testResourceBundleKeyAsSuffix() throws Exception {
        checkDeclaration("projects/testWebProject/web/navigation/navigation04.xhtml", "#{bundle.cokoliv^Jineho}", "Messages.properties", 48);
    }

    public void testResourceBundleKeyAsProperty() throws Exception {
        checkDeclaration("projects/testWebProject/web/navigation/navigation04.xhtml", "#{bundle['cokoliv^Jineho']}", "Messages.properties", 48);
    }
    
}
