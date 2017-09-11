/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
