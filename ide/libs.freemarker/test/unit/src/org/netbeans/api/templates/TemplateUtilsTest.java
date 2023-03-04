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
package org.netbeans.api.templates;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.templates.ScriptingCreateFromTemplateHandler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sdedic
 */
public class TemplateUtilsTest extends NbTestCase {

    public TemplateUtilsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    /**
     * Checks creation of templates without any special processing.
     * @throws Exception 
     */
    public void testCreatePlain() throws Exception {
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        FileObject template = dataRoot.getFileObject("templates/ClassWithoutReplacements.java");
        template.setAttribute("template", Boolean.TRUE);
        FileObject workRoot = FileUtil.toFileObject(getWorkDir());
        FileObject result = FileBuilder.createFromTemplate(template, workRoot, "NoReplacements", null, FileBuilder.Mode.FORMAT);
        FileObject pass = dataRoot.getFileObject("golden/ClassWithoutReplacements.java");
        assertFile(FileUtil.toFile(result), FileUtil.toFile(pass));
    }
    
    /**
     * Forces plain default processing, although the template contains replaceable parts
     */
    public void testCreateForcePlain() throws Exception {
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        FileObject template = dataRoot.getFileObject("templates/SimpleReplacements.java");
        template.setAttribute("template", Boolean.TRUE);
        FileObject workRoot = FileUtil.toFileObject(getWorkDir());
        Map m = new HashMap();
        m.put("USER", "foobar");
        FileObject result = FileBuilder.createFromTemplate(template, workRoot, "NoReplacements", m, FileBuilder.Mode.COPY);
        FileObject pass = dataRoot.getFileObject("golden/ForceNoReplacements.java");
        assertFile(FileUtil.toFile(result), FileUtil.toFile(pass));
    }
    
    /**
     * Uses a simple format, this is the mode applied by former implementation in MultiDataObject
     */
    public void testCreateReplaceSimple() throws Exception {
        clearWorkDir();
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        FileObject template = dataRoot.getFileObject("templates/SimpleReplacements.java");
        template.setAttribute("template", Boolean.TRUE);
        FileObject workRoot = FileUtil.toFileObject(getWorkDir());
        Map m = new HashMap();
        m.put("USER", "foobar");
        FileObject result = FileBuilder.createFromTemplate(template, workRoot, "SimpleReplacements", m, FileBuilder.Mode.FORMAT);
        FileObject pass = dataRoot.getFileObject("golden/SimpleReplacements.java");
        assertFile(FileUtil.toFile(result), FileUtil.toFile(pass));
    }
    
    public void testScriptingTemplate() throws Exception {
        clearWorkDir();
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        FileObject template = dataRoot.getFileObject("templates/GeneratedMethodBody.java");
        template.setAttribute("template", Boolean.TRUE);
        template.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "freemarker");
        FileObject workRoot = FileUtil.toFileObject(getWorkDir());
        Map m = new HashMap();
        m.put("default_return_value", "42");
        FileObject result = FileBuilder.createFromTemplate(template, workRoot, "GeneratedMethodBody", m, FileBuilder.Mode.FORMAT);
        FileObject pass = dataRoot.getFileObject("golden/GeneratedMethodBody.java");
        assertFile(FileUtil.toFile(result), FileUtil.toFile(pass));
    }
    
    @SuppressWarnings("PackageVisibleInnerClass")
    class DefaultValueAttribute implements CreateFromTemplateAttributes {
        @Override
        public Map<String, ?> attributesFor(CreateDescriptor desc) {
            FileObject template = desc.getTemplate();
            FileObject dataRoot = FileUtil.toFileObject(getDataDir());
            FileObject t = dataRoot.getFileObject("templates/GeneratedMethodBody.java");
            if (t != template) {
                return null;
            }
            
            Map m = new HashMap();
            m.put("default_return_value", "42");
            
            return m;
        }
    }
    
    public void testAddTemplateParameters() throws Exception {
        MockLookup.setLookup(
                Lookups.metaInfServices(getClass().getClassLoader()),
                Lookups.fixed(new DefaultValueAttribute()));
        clearWorkDir();
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        FileObject template = dataRoot.getFileObject("templates/GeneratedMethodBody.java");
        template.setAttribute("template", Boolean.TRUE);
        template.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "freemarker");
        FileObject workRoot = FileUtil.toFileObject(getWorkDir());
        FileObject result = FileBuilder.createFromTemplate(template, workRoot, "GeneratedMethodBody", null, FileBuilder.Mode.FORMAT);
        FileObject pass = dataRoot.getFileObject("golden/GeneratedMethodBody.java");
        assertFile(FileUtil.toFile(result), FileUtil.toFile(pass));
    }
    
    public void testOverridenParameters() throws Exception {
        MockLookup.setLookup(
                Lookups.metaInfServices(getClass().getClassLoader()),
                Lookups.fixed(new DefaultValueAttribute()));
        clearWorkDir();
        FileObject dataRoot = FileUtil.toFileObject(getDataDir());
        FileObject template = dataRoot.getFileObject("templates/GeneratedMethodBody.java");
        template.setAttribute("template", Boolean.TRUE);
        template.setAttribute(ScriptingCreateFromTemplateHandler.SCRIPT_ENGINE_ATTR, "freemarker");
        FileObject workRoot = FileUtil.toFileObject(getWorkDir());
        Map m = new HashMap();
        m.put("default_return_value", "24");
        FileObject result = FileBuilder.createFromTemplate(template, workRoot, "GeneratedMethodBody", m, FileBuilder.Mode.FORMAT);
        FileObject pass = dataRoot.getFileObject("golden/GeneratedMethodBody2.java");
        assertFile(FileUtil.toFile(result), FileUtil.toFile(pass));
    }
}
