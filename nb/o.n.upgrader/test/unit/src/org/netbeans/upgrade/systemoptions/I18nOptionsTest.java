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

package org.netbeans.upgrade.systemoptions;

/**
 * @author Radek Matous
 */
public class I18nOptionsTest extends BasicTestForImport {
    public I18nOptionsTest(String testName) {
        super(testName, "org-netbeans-modules-i18n-I18nOptions.settings");
    }
    
    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/i18n");
    }
    
    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "replaceResourceValue",
            "regularExpression",
            "initJavaCode",
            "replaceJavaCode",
            "advancedWizard",
            "lastResource2",
            "i18nRegularExpression"
        });
    }
    
    
    public void testReplaceResourceValue() throws Exception {
        assertPropertyType("replaceResourceValue", "java.lang.Boolean");
        assertProperty("replaceResourceValue","false");
    }
    public void testRegularExpression() throws Exception {
        assertPropertyType("regularExpression", "java.lang.String");
        assertProperty("regularExpression","(getString|getBundle)[:space:]*\\([:space:]*{hardString}|// NOI18N");
    }
    public void testInitJavaCode() throws Exception {
        assertPropertyType("initJavaCode", "java.lang.String");
        assertProperty("initJavaCode","java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\")");
    }
    public void testReplaceJavaCode() throws Exception {
        assertPropertyType("replaceJavaCode", "java.lang.String");
        assertProperty("replaceJavaCode","java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\").getString(\"{key}\")");
    }
    public void testAdvancedWizard() throws Exception {
        assertPropertyType("advancedWizard", "java.lang.Boolean");
        assertProperty("advancedWizard","true");
    }
    public void testLastResource2() throws Exception {
        assertProperty("lastResource2","home.local/rmatous/module2/src/org/yourorghere/module2/Bundle.properties");
    }
    public void testI18nRegularExpression() throws Exception {
        assertPropertyType("i18nRegularExpression", "java.lang.String");
        assertProperty("i18nRegularExpression","getString[:space:]*\\([:space:]*{hardString}");
    }
    
}
