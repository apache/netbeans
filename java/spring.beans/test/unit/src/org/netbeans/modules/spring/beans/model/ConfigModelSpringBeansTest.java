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

package org.netbeans.modules.spring.beans.model;

import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.TestUtils;

/**
 *
 * @author Rohan Ranade
 */
public class ConfigModelSpringBeansTest extends ConfigFileTestCase {
    
    public ConfigModelSpringBeansTest(String testName) {
        super(testName);
    }            

    public void testRecursiveAliasSearch() throws Exception {
        String text = TestUtils.createXMLConfigText("<alias alias='foo' name='bar'/>" +
                "<alias alias='bar' name='baz'/>" +
                "<bean name='baz'/>");
        TestUtils.copyStringToFile(text, configFile);
        SpringConfigModel model = createConfigModel(configFile);
        final String[] beanName = { null };
        model.runReadAction(new Action<SpringBeans>() {
            public void run(SpringBeans beans) {
                SpringBean bean = beans.findBean("foo");
                beanName[0] = bean.getNames().get(0);
            }
        });
        
        assertEquals("baz", beanName[0]);
    }

}
