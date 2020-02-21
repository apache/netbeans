/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makefile.editor;

import org.netbeans.junit.NbTestCase;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.parsing.api.Embedding;
import java.util.List;
import org.netbeans.modules.parsing.api.Source;
import javax.swing.text.Document;
import org.junit.Test;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.modules.cnd.api.script.MakefileTokenId;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;

/**
 */
public class ShellEmbeddingProviderTest extends NbTestCase {

    public ShellEmbeddingProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MimePath mimePath = MimePath.parse(MIMENames.MAKEFILE_MIME_TYPE);
        MockMimeLookup.setInstances(mimePath, MakefileTokenId.language());
    }

    @Test
    public void testOneLine() throws Exception {
        List<Embedding> embeddings = getShellEmbeddings("a:\n\techo $${PATH} # echo PATH\n");
        assertEquals(1, embeddings.size());

        Embedding e = embeddings.get(0);
        assertEquals("echo ${PATH} # echo PATH", e.getSnapshot().getText().toString());
    }

    @Test
    public void testMultiLine() throws Exception {
        List<Embedding> embeddings = getShellEmbeddings("a:\n\techo a\n\techo b\n");
        assertEquals(2, embeddings.size());

        Embedding e1 = embeddings.get(0);
        assertEquals("echo a", e1.getSnapshot().getText().toString());

        Embedding e2 = embeddings.get(1);
        assertEquals("echo b", e2.getSnapshot().getText().toString());
    }

    @Test
    public void testEscapedNewline() throws Exception {
        List<Embedding> embeddings = getShellEmbeddings("a:\n\techo a\\\n\techo b\n");
        assertEquals(1, embeddings.size());

        Embedding e1 = embeddings.get(0);
        assertEquals("echo a\\\n\techo b", e1.getSnapshot().getText().toString());
    }

    @Test
    public void testNoNewline() throws Exception {
        List<Embedding> embeddings = getShellEmbeddings("a:\n\techo a");
        assertEquals(1, embeddings.size());

        Embedding e1 = embeddings.get(0);
        assertEquals("echo a", e1.getSnapshot().getText().toString());
    }

    private static List<Embedding> getShellEmbeddings(String text) throws BadLocationException {
        Document doc = new NbEditorDocument(MIMENames.MAKEFILE_MIME_TYPE);
        doc.insertString(0, text, null);
        return new ShellEmbeddingProvider().getEmbeddings(Source.create(doc).createSnapshot());
    }
}
