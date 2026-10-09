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
package org.netbeans.modules.web.jsf.editor.el;

import java.io.IOException;
import java.util.List;

import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.jsf.editor.TestBaseForTestProject;
import org.openide.filesystems.FileObject;

public class JsfELVariableResolverTest extends TestBaseForTestProject {

    private final JsfELVariableResolver variableResolver = new JsfELVariableResolver();
    private final ResolverContext resolverContext = new ResolverContext();

    public JsfELVariableResolverTest(String name) {
        super(name);
    }

    private String getTestFilePath() {
        return "testWebProject/web/test.xhtml";
    }

    private FileObject getTestFileObject() throws IOException {
        return getWorkFile(getTestFilePath());
    }

    public void testGetInjectableField() throws Exception {
        ELVariableResolver.FieldInfo fieldInfoMBean = variableResolver.getInjectableField("MBean", getTestFileObject(), resolverContext);
        assertNotNull(fieldInfoMBean);
        assertEquals("beans.MBean", fieldInfoMBean.getType());
        assertEquals("beans.MBean", fieldInfoMBean.getEnclosingClass());
        ELVariableResolver.FieldInfo fieldInfoNull = variableResolver.getInjectableField("NonExistingBean", getTestFileObject(), resolverContext);
        assertNull(fieldInfoNull);
    }

    public void testGetBeanName() throws Exception {
        String mBeanBeanName = variableResolver.getBeanName("beans.MBean", getTestFileObject(), resolverContext);
        assertEquals("MBean", mBeanBeanName);
        String invalidBeanName = variableResolver.getBeanName("not.existent.Class", getTestFileObject(), resolverContext);
        assertNull(invalidBeanName);
    }

    public void testGetManagedBeans() throws Exception {
        List<ELVariableResolver.VariableInfo> beans = variableResolver.getManagedBeans(getTestFileObject(), resolverContext);
        assertNotNull(beans);
        List<String> beanNames = beans.stream().map(v -> v.name).toList();
        assertTrue(beanNames.contains("MBean"));
        assertTrue(beanNames.contains("SessionBean"));
    }

    public void testGetVariables() throws Exception {
        ParseResultInfo parseResult = parse(getTestFilePath());
        Snapshot snapshot = parseResult.topLevelSnapshot;
        assertNotNull(snapshot);

        List<ELVariableResolver.VariableInfo> variablesAtStart = variableResolver.getVariables(snapshot, 0, resolverContext);
        assertNotNull(variablesAtStart);
        assertTrue(variablesAtStart.isEmpty());

        List<ELVariableResolver.VariableInfo> variablesInsideTable = variableResolver.getVariables(snapshot, 500, resolverContext);
        assertNotNull(variablesInsideTable);
        assertEquals(1, variablesInsideTable.size());
        ELVariableResolver.VariableInfo var = variablesInsideTable.get(0);
        assertEquals("prop", var.name);
        assertEquals("#{ProductMB.all}", var.expression);
        assertNull(var.clazz);

        // TODO getVariables should be empty after the end of the table
    }
    
    public void testGetRawObjectProperties() throws Exception {
        ParseResultInfo parseResult = parse("testWebProject/web/resources/ezcomp/test.xhtml");
        Snapshot snapshot = parseResult.topLevelSnapshot;
        assertNotNull(snapshot);

        List<ELVariableResolver.VariableInfo> emptyVariables = variableResolver.getRawObjectProperties("invalid", snapshot, resolverContext);
        assertNotNull(emptyVariables);
        assertTrue(emptyVariables.isEmpty());

        List<ELVariableResolver.VariableInfo> ccVariables = variableResolver.getRawObjectProperties("cc", snapshot, resolverContext);
        assertNotNull(ccVariables);
        List<String> ccVariablesNames = ccVariables.stream().map(v -> v.name).toList();
        assertTrue(ccVariablesNames.contains("id"));
        assertTrue(ccVariablesNames.contains("rendered"));
        assertTrue(ccVariablesNames.contains("attrs"));

        List<ELVariableResolver.VariableInfo> attrVariables = variableResolver.getRawObjectProperties("attrs", snapshot, resolverContext);
        assertNotNull(attrVariables);
        List<String> attrVariablesName = attrVariables.stream().map(v -> v.name).toList();
        assertTrue(attrVariablesName.contains("id"));
        assertTrue(attrVariablesName.contains("rendered"));
        assertTrue(attrVariablesName.contains("testAttr"));
    }

    public void testGetBeansInScope() throws Exception {
        ParseResultInfo parseResult = parse(getTestFilePath());
        Snapshot snapshot = parseResult.topLevelSnapshot;
        assertNotNull(snapshot);

        List<ELVariableResolver.VariableInfo> invalidScopeBeans = variableResolver.getBeansInScope("invalid", snapshot, resolverContext);
        assertNotNull(invalidScopeBeans);
        assertTrue(invalidScopeBeans.isEmpty());
        
        List<ELVariableResolver.VariableInfo> requestBeans = variableResolver.getBeansInScope("request", snapshot, resolverContext);
        assertNotNull(requestBeans);
        List<String> requestBeanNames = requestBeans.stream().map(v -> v.name).toList();
        assertTrue(requestBeanNames.contains("MBean"));
        assertFalse(requestBeanNames.contains("SessionBean"));
        
        List<ELVariableResolver.VariableInfo> sessionBeans = variableResolver.getBeansInScope("session", snapshot, resolverContext);
        assertNotNull(sessionBeans);
        List<String> sessionBeanNames = sessionBeans.stream().map(v -> v.name).toList();
        assertFalse(sessionBeanNames.contains("MBean"));
        assertTrue(sessionBeanNames.contains("SessionBean"));
    }

}
