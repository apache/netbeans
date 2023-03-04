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

package org.netbeans.modules.groovy.qaf;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests for Groovy specific new file wizards
 *
 * @author lukas
 */
public class GroovyWizardsTest extends GroovyTestCase {

    public GroovyWizardsTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return "GroovyWizards"; //NOI18N
    }

    /**
     * Test create new Groovy class
     */
    public void testGroovy() {
        //Groovy Class
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.support.resources.Bundle", "Templates/Groovy/GroovyClass.groovy");
        createNewGroovyFile(getProject(), label);
        //in default package
        String name = "MyGroovyClass"; //NOI18N
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        op.setPackage(""); //NOI18N
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".groovy"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("package") < 0); //NOI18N
        //in a custom package
        name = "PkgGroovyClass"; //NOI18N
        String pkg = "my.groovy.pkg"; //NOI18N
        createNewGroovyFile(getProject(), label);
        op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        op.setPackage(pkg);
        op.finish();
        eo = new EditorOperator(name + ".groovy"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("package " + pkg) > -1); //NOI18N
    }

    /**
     * Test create new Groovy script
     */
    public void testGroovyScript() {
        //Groovy Script
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.support.resources.Bundle", "Templates/Groovy/GroovyScript.groovy");
        createNewGroovyFile(getProject(), label);
        //in default package
        String name = "MyGroovyScript"; //NOI18N
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        op.setPackage(""); //NOI18N
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".groovy"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("package") < 0); //NOI18N
        //in a custom package
        name = "PkgGroovyScript"; //NOI18N
        String pkg = "my.groovy.pkg"; //NOI18N
        createNewGroovyFile(getProject(), label);
        op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        op.setPackage(pkg);
        op.finish();
        eo = new EditorOperator(name + ".groovy"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("package " + pkg) > -1); //NOI18N
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(GroovyWizardsTest.class)
                .enableModules(".*").clusters(".*")); //NOI18N
    }

}
