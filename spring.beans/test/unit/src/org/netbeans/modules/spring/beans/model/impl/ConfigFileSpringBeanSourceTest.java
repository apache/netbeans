/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
