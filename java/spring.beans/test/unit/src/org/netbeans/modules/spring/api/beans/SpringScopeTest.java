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

package org.netbeans.modules.spring.api.beans;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.ConfigFileManagerAccessor;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.SpringConfigModelAccessor;
import org.netbeans.modules.spring.beans.SpringScopeAccessor;
import org.netbeans.modules.spring.beans.TestUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class SpringScopeTest extends ConfigFileTestCase {

    public SpringScopeTest(String testName) {
        super(testName);
    }

    public void testGetConfigModelAdHoc() throws Exception {
        String contents = TestUtils.createXMLConfigText("<bean id='foo' name='bar baz' class='org.example.Foo'/>");
        TestUtils.copyStringToFile(contents, configFile);
        ConfigFileManager manager = ConfigFileManagerAccessor.getDefault().createConfigFileManager(new DefaultConfigFileManagerImpl());
        SpringScope scope = SpringScopeAccessor.getDefault().createSpringScope(manager);

        final FileObject configFO = FileUtil.toFileObject(configFile);
        SpringConfigModel model = SpringScopeAccessor.getDefault().getConfigModel(scope, configFO);
        final int[] beanCount = { 0 };
        model.runReadAction(new Action<SpringBeans>() {
            public void run(SpringBeans beans) {
                beanCount[0] = beans.getFileBeans(configFO).getBeans().size();
            }
        });
        assertEquals(1, beanCount[0]);
    }

    public void testGetConfigModel() throws IOException {
        TestUtils.copyStringToFile(TestUtils.createXMLConfigText("<bean id='foo' class='org.example.Foo'/>"), configFile);
        final File configFile2 = createConfigFileName("anotherContext.xml");
        TestUtils.copyStringToFile(TestUtils.createXMLConfigText("<bean id='bar' class='org.example.Bar'/>"), configFile2);
        ConfigFileGroup group = ConfigFileGroup.create(Arrays.asList(configFile, configFile2));
        final ConfigFileManager manager = ConfigFileManagerAccessor.getDefault().createConfigFileManager(new DefaultConfigFileManagerImpl(group));
        SpringScope scope = SpringScopeAccessor.getDefault().createSpringScope(manager);

        final FileObject configFO = FileUtil.toFileObject(configFile);
        SpringConfigModel model = SpringScopeAccessor.getDefault().getConfigModel(scope, configFO);
        final Set<String> beanNames = new HashSet<String>();
        model.runReadAction(new Action<SpringBeans>() {
            public void run(SpringBeans beans) {
                for (SpringBean bean : beans.getFileBeans(configFO).getBeans()) {
                    beanNames.add(bean.getId());
                }
            }
        });
        assertEquals(1, beanNames.size());
        assertTrue(beanNames.contains("foo"));

        final FileObject configFO2 = FileUtil.toFileObject(configFile2);
        beanNames.clear();
        model.runReadAction(new Action<SpringBeans>() {
            public void run(SpringBeans beans) {
                for (SpringBean bean : beans.getFileBeans(configFO2).getBeans()) {
                    beanNames.add(bean.getId());
                }
            }
        });
        assertEquals(1, beanNames.size());
        assertTrue(beanNames.contains("bar"));

        beanNames.clear();
        model.runReadAction(new Action<SpringBeans>() {
            public void run(SpringBeans beans) {
                for (SpringBean bean : beans.getBeans()) {
                    beanNames.add(bean.getId());
                }
            }
        });
        assertEquals(2, beanNames.size());
        assertTrue(beanNames.contains("foo"));
        assertTrue(beanNames.contains("bar"));
    }

    public void testGetAllConfigModels() throws IOException {
        TestUtils.copyStringToFile(TestUtils.createXMLConfigText("<bean id='foo' class='org.example.Foo'/>"), configFile);
        final File configFile2 = createConfigFileName("anotherContext.xml");
        TestUtils.copyStringToFile(TestUtils.createXMLConfigText("<bean id='bar' class='org.example.Bar'/>"), configFile2);
        ConfigFileGroup group = ConfigFileGroup.create(Arrays.asList(configFile, configFile2));
        final File configFile3 = createConfigFileName("yetAnotherContext.xml");
        TestUtils.copyStringToFile(TestUtils.createXMLConfigText("<bean id='baz' class='org.example.Baz'/>"), configFile3);
        final ConfigFileManager manager = ConfigFileManagerAccessor.getDefault().createConfigFileManager(new DefaultConfigFileManagerImpl(
                new File[] { configFile, configFile2, configFile3 },
                new ConfigFileGroup[] { group }
        ));
        SpringScope scope = SpringScopeAccessor.getDefault().createSpringScope(manager);

        List<SpringConfigModel> models = scope.getAllConfigModels();
        assertEquals(2, models.size());
        assertSame(group, SpringConfigModelAccessor.getDefault().getConfigFileGroup(models.get(0)));
        ConfigFileGroup configFile3GRoup = SpringConfigModelAccessor.getDefault().getConfigFileGroup(models.get(1));
        assertEquals(1, configFile3GRoup.getFiles().size());
        assertEquals(configFile3, configFile3GRoup.getFiles().get(0));
    }
}
