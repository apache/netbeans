/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
