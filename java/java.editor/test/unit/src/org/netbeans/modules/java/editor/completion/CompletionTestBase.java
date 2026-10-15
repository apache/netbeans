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

package org.netbeans.modules.java.editor.completion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.gen.WhitespaceIgnoringDiff;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.editor.completion.CompletionItemComparator;
import org.netbeans.modules.editor.java.JavaCompletionProvider;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.completion.CompletionTestBaseBase;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.openide.LifecycleManager;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 */
public class CompletionTestBase extends CompletionTestBaseBase {
    
    public CompletionTestBase(String testName) {
        super(testName, "org/netbeans/modules/java/editor/completion/JavaCompletionProviderTest");
    }
    
    protected void performTest(String source, int caretPos, String textToInsert, String toPerformItemRE, String goldenFileName) throws Exception {
        performTest(source, caretPos, textToInsert, toPerformItemRE, goldenFileName, null);
    }
    
    protected void performTest(String source, int caretPos, String textToInsert, String toPerformItemRE, String goldenFileName, String sourceLevel) throws Exception {
        this.sourceLevel.set(sourceLevel);
        File testSource = new File(getWorkDir(), "test/Test.java");
        testSource.getParentFile().mkdirs();
        copyToWorkDir(new File(getDataDir(), "org/netbeans/modules/java/editor/completion/data/" + source + ".java"), testSource);
        FileObject testSourceFO = FileUtil.toFileObject(testSource);
        assertNotNull(testSourceFO);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        assertNotNull(testSourceDO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        assertNotNull(ec);
        final Document doc = ec.openDocument();
        assertNotNull(doc);
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        int textToInsertLength = textToInsert != null ? textToInsert.length() : 0;
        if (textToInsertLength > 0)
            doc.insertString(caretPos, textToInsert, null);
        Source s = Source.create(doc);
        List<? extends CompletionItem> items = JavaCompletionProvider.query(s, CompletionProvider.COMPLETION_QUERY_TYPE, caretPos + textToInsertLength, caretPos + textToInsertLength);
        items.sort(CompletionItemComparator.BY_PRIORITY);
        
        assertNotNull(goldenFileName);            

        Pattern p = Pattern.compile(toPerformItemRE);
        CompletionItem item = null;            
        for (CompletionItem i : items) {
            if (p.matcher(i.toString()).find()) {
                item = i;
                break;
            }
        }            
        assertNotNull(item);

        JEditorPane editor = new JEditorPane();
        SwingUtilities.invokeAndWait(() -> {
            editor.setEditorKit(new JavaKit());
        });
        editor.setDocument(doc);
        editor.setCaretPosition(caretPos + textToInsertLength);
        item.defaultAction(editor);

        SwingUtilities.invokeAndWait(() -> {});

        File output = new File(getWorkDir(), getName() + ".out2");
        Writer out = new FileWriter(output);            
        out.write(doc.getText(0, doc.getLength()));
        out.close();

        File goldenFile = getGoldenFile(goldenFileName);
        File diffFile = new File(getWorkDir(), getName() + ".diff");

        assertFile(output, goldenFile, diffFile, new WhitespaceIgnoringDiff());
        
        LifecycleManager.getDefault().saveAll();
    }

    @ServiceProvider(service=EntityCatalog.class)
    public static final class TestEntityCatalogImpl extends EntityCatalog {

        @Override
        public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
            switch (publicID) {
                case "-//NetBeans//DTD Editor Fonts and Colors settings 1.1//EN":
                    return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/modules/editor/settings/storage/fontscolors/EditorFontsColors-1_1.dtd"));
                case "-//NetBeans//DTD Editor Code Templates settings 1.0//EN":
                    return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/lib/editor/codetemplates/storage/EditorCodeTemplates-1_0.dtd"));
            }
            return null;
        }

    }
}
