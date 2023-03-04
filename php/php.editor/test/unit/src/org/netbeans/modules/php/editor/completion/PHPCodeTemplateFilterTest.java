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
package org.netbeans.modules.php.editor.completion;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.php.editor.PHPCodeTemplateFilter;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.netbeans.modules.php.api.util.FileUtils.PHP_MIME_TYPE;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPCodeTemplateFilterTest extends PHPCodeCompletionTestBase {

    public PHPCodeTemplateFilterTest(String testName) {
        super(testName);
    }

    private void checkAllTemplates(String source, boolean expected) throws Exception {
        assertNotNull(source);
        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String modifiedSource = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Document document = getDocument(modifiedSource, PHP_MIME_TYPE);
        assertNotNull(document);
        Collection<? extends CodeTemplate> codeTemplates = getCodeTemplates(document);
        PHPCodeTemplateFilter filter = new PHPCodeTemplateFilter();
        assertNotNull(filter);
        for (CodeTemplate codeTemplate : codeTemplates) {
            assertEquals("Code template: " + codeTemplate.toString(), expected, filter.accept(codeTemplate));
        }
    }

    private void checkTemplate(String source, boolean expected, String abbreviation) throws Exception {
        assertNotNull(source);
        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String modifiedSource = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Document document = getDocument(modifiedSource, PHP_MIME_TYPE);
        assertNotNull(document);
        Collection<? extends CodeTemplate> codeTemplates = getCodeTemplates(document);
        PHPCodeTemplateFilter filter = new PHPCodeTemplateFilter();
        assertNotNull(filter);
        for (CodeTemplate codeTemplate : codeTemplates) {
            if (codeTemplate.getAbbreviation().equals(abbreviation)) {
                assertEquals("Code template: " + codeTemplate.toString(), expected, filter.accept(codeTemplate));
            }
        }
    }

    private void checkNoContextTemplate(String source, boolean expected) {
        assertNotNull(source);
        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String modifiedSource = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Document document = getDocument(modifiedSource, PHP_MIME_TYPE);
        assertNotNull(document);
        CodeTemplateManager templateManager = CodeTemplateManager.get(document);
        assertNotNull(templateManager);
        CodeTemplate codeTemplate = templateManager.createTemporary("no conext template");
        PHPCodeTemplateFilter filter = new PHPCodeTemplateFilter();
        assertEquals("Code template: " + codeTemplate.toString(), expected, filter.accept(codeTemplate));
    }

    private Collection<? extends CodeTemplate> getCodeTemplates(Document document) {
        CodeTemplateManager templateManager = CodeTemplateManager.get(document);
        assertNotNull(templateManager);
        return templateManager.getCodeTemplates();
    }

    public void testAllDefaultTemplatesInClass() throws Exception {
        String source = "<?php\n^ \n?>";
        checkAllTemplates(source, true);
    }

    public void testClsInCls() throws Exception {
        String source = "<?php\nclass Foo {^} \n?>";
        checkTemplate(source, true, "cls");
    }

    public void testNoContextTemplateInClass() throws Exception {
        String source = "<?php\n class Foo {^} \n?>";
        checkNoContextTemplate(source, true);
    }

    public void testFcomInsideClass() throws Exception {
        String source = "<?php\nclass Foo {^} \n?>";
        checkTemplate(source, true, "fcom");
    }

    public void testFcomOutsideClass() throws Exception {
        String source = "<?php\nclass Foo {} \n^ \n?>";
        checkTemplate(source, true, "fcom");
    }

    public void testFcomInsideFunction() throws Exception {
        String source = "<?php\nclass Foo { \n function foo() { ^ } \n} \n?>";
        checkTemplate(source, true, "fcom");
    }

    public void testIfncInIface() throws Exception {
        String source = "<?php\ninterface IFace { \n ^ \n} \n?>";
        checkTemplate(source, true, "ifnc");
    }

    public void testIfncInCls() throws Exception {
        String source = "<?php\nclass MyCls { \n ^ \n} \n?>";
        checkTemplate(source, true, "ifnc");
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(new FileObject[] {
                FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/lib/codeTemplateFilter"))
            })
        );
    }

}
