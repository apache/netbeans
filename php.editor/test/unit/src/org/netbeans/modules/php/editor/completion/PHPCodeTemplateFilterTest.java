/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
