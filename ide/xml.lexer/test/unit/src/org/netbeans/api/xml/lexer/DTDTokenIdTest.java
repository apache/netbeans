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
package org.netbeans.api.xml.lexer;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class DTDTokenIdTest extends AbstractTestCase {
    
    public DTDTokenIdTest(String testName) {
        super(testName);
    }
    
    private String lexDocument(String resourceName) throws Exception {
        javax.swing.text.Document document = getDocument("resources/" + resourceName + ".dtd");
        String content = document.getText(0, document.getLength());
        String[] lines = content.split("\n");
        int line = 0;
        int offset = 0;
        int lineEnd = offset + lines[line].length() + 1;
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            //assert(ts.tokenCount() == expectedIds.length);
            int overLine = 0;
            while(ts.moveNext()) {
                if (first) {
                    lineEnd = ts.offset()  - overLine + lines[line].length() + 1;
                }
                Token<DTDTokenId> t = ts.token();
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                if (PartType.MIDDLE == t.partType()) {
                    sb.append("{");
                }
                sb.append(t.id().name());
                if (PartType.MIDDLE == t.partType()) {
                    sb.append("}");
                }
                offset += t.length();
                if (offset >= lineEnd) {
                    sb.append("\n");
                    first = true;
                    String s = t.text().toString();
                    int pos = 0;
                    do {
                        int nl = s.indexOf('\n', pos);
                        if (nl == -1) {
                            break;
                        }
                        line++;
                        pos = nl + 1;
                    } while (true);
                    overLine = s.length() - pos;
                }
            }
            return sb.toString();
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }
    
    private void checkTokenSequence(String resource) throws Exception {
        clearWorkDir();
        String text = lexDocument(resource);
        String path = getClass().getName();
        path = path.substring(0, path.lastIndexOf('.')).replace('.', '/');
        
        FileObject w = FileUtil.toFileObject(getWorkDir()).createData(resource + ".out");
        File g = new File(getDataDir(), "goldenfiles/" + path + "/" + resource + ".pass");
        try (OutputStream o = w.getOutputStream(); 
            OutputStreamWriter wr = new OutputStreamWriter(o)) {
            wr.write(text);
        }
        File d = new File(getWorkDir(), resource + ".diff");
        assertFile(FileUtil.toFile(w), g, d);
    }
    
    public void testXMLDeclaration() throws Exception {
        checkTokenSequence("textDeclarations");
    }

    public void testElementDeclarations() throws Exception {
        checkTokenSequence("elementDeclarations");
    }

    public void testAttrlist() throws Exception {
        checkTokenSequence("attrlist");
    }

    @Override
    protected Language getLanguage() {
        return DTDTokenId.language();
    }
    
    public void testEmbeddedEntityRefs() throws Exception {
        checkTokenSequence("embeddedEntities");
    }
    
    public void testComments() throws Exception {
        
    }
}
