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

package org.netbeans.modules.spring.beans.model.impl;

import java.util.List;
import java.util.Set;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeanProperty;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.TestUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Andrei Badea
 */
public class ConfigFileSpringBeanSourceTest extends ConfigFileTestCase {

    public ConfigFileSpringBeanSourceTest(String testName) {
        super(testName);
    }
    public void testParse() throws Exception {
        String contents = TestUtils.createXMLConfigText("<alias name='foo' alias='xyz'/>" +
                "<bean id='foo' name='bar baz' " +
                "parent='father' factory-bean='factory' factory-method='createMe' " +
                "class='org.example.Foo'/>");
        TestUtils.copyStringToFile(contents, configFile);
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(configFile));
        BaseDocument doc = (BaseDocument)dataObject.getCookie(EditorCookie.class).openDocument();
        ConfigFileSpringBeanSource source = new ConfigFileSpringBeanSource();
        source.parse(doc);
        List<SpringBean> beans = source.getBeans();
        assertEquals(1, beans.size());
        SpringBean bean = beans.get(0);
        assertSame(bean, source.findBean("foo"));
        assertSame(bean, source.findBean("bar"));
        assertSame(bean, source.findBean("baz"));
        assertSame(bean, source.findBeanByID("foo"));
        assertNull(source.findBeanByID("bar"));
        assertNull(source.findBeanByID("baz"));
        assertEquals("father", bean.getParent());
        assertEquals("factory", bean.getFactoryBean());
        assertEquals("createMe", bean.getFactoryMethod());
        assertEquals(1, source.getAliases().size());
        assertEquals("foo", source.findAliasName("xyz"));
        int offset = contents.indexOf("<bean ");
        Location location = bean.getLocation();
        assertEquals(offset, location.getOffset());
        assertEquals(FileUtil.toFileObject(configFile), location.getFile());
    }
    
    public void testPropertyParseWithPNamespace() throws Exception {
        String contents = TestUtils.createXMLConfigText(
                "<bean id='foo' name='bar baz' " +
                "parent='father' factory-bean='factory' factory-method='createMe' " +
                "class='org.example.Foo' p:p2='v2' p:p3-ref='v3'>" +
                "<property name='p1' value='v1'/>" + 
                "</bean>");
        TestUtils.copyStringToFile(contents, configFile);
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(configFile));
        BaseDocument doc = (BaseDocument)dataObject.getCookie(EditorCookie.class).openDocument();
        ConfigFileSpringBeanSource source = new ConfigFileSpringBeanSource();
        source.parse(doc);
        List<SpringBean> beans = source.getBeans();
        assertEquals(1, beans.size());
        SpringBean bean = beans.get(0);
        assertSame(bean, source.findBean("foo"));
        Set<SpringBeanProperty> properties = bean.getProperties();
        assertEquals(3, properties.size());
    }
    
    public void testPropertyParseWithoutPNamespace() throws Exception {
        String contents = TestUtils.createXMLConfigText(
                "<bean id='foo' name='bar baz' " +
                "parent='father' factory-bean='factory' factory-method='createMe' " +
                "class='org.example.Foo' p:p2='v2' p:p3-ref='v3'>" +
                "<property name='p1' value='v1'/>" + 
                "</bean>", false);
        TestUtils.copyStringToFile(contents, configFile);
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(configFile));
        BaseDocument doc = (BaseDocument)dataObject.getCookie(EditorCookie.class).openDocument();
        ConfigFileSpringBeanSource source = new ConfigFileSpringBeanSource();
        source.parse(doc);
        List<SpringBean> beans = source.getBeans();
        assertEquals(1, beans.size());
        SpringBean bean = beans.get(0);
        assertSame(bean, source.findBean("foo"));
        Set<SpringBeanProperty> properties = bean.getProperties();
        assertEquals(1, properties.size());
    }
}
