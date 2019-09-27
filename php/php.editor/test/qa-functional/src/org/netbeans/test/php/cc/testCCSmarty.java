/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.php.cc;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author tester
 */
public class testCCSmarty extends cc{
    static final String TEST_PHP_NAME = "PhpProject_cc_0003";
    
    public testCCSmarty(String arg0) {
        super(arg0);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCSmarty.class).addTest(
                "CreateApplication",
                //"testPhpSmartyCodeCompletion",
                "testPhpSmartyCodeTemplateCompletion").enableModules(".*").clusters(".*") //.gui( true )                
                );
    }
    
    public void CreateApplication() {
        startTest();
        CreatePHPApplicationInternal(TEST_PHP_NAME);
        endTest();
    }
    
    public void testPhpSmartyCodeTemplateCompletion() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "Smarty Template", null);
        startTest();
        TypeCode(file, "as");
        file.pushTabKey();
        new EventTool().waitNoEvent(1000);
        String ideal = "{assign var=\"var\" value=\"value\"}";
        CheckResult(file, ideal);//file.getLineNumber(2);
        endTest();
    }
}
