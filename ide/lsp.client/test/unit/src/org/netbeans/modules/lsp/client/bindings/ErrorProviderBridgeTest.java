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
package org.netbeans.modules.lsp.client.bindings;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

public class ErrorProviderBridgeTest {

    public ErrorProviderBridgeTest() {
    }

    @Test
    public void testListeningAndLifecycle() throws Exception {
        JTextComponent c = new JTextArea();
        FileObject file = FileUtil.createMemoryFileSystem().getRoot().createData("c.txt");
        RequestProcessor rp = new RequestProcessor("ErrorProviderBridgeTest");
        MockErrorProvider mp = new MockErrorProvider(c);

        List<List<ErrorDescription>> appliedDescriptions = new ArrayList<>();
        ErrorProviderBridge b = new ErrorProviderBridge(c.getDocument(), file, Collections.singleton(mp), rp) {
            @Override
            protected void applyHints(String prefix, org.netbeans.spi.lsp.ErrorProvider p, List<ErrorDescription> arr) {
                if (prefix.equals("lsp:errors")) {
                    appliedDescriptions.add(arr);
                }
            }
        };

        b.start();
        b.waitFinished();

        assertEquals("One set of errors reported", 1, appliedDescriptions.size());
        assertTrue("It is empty so far", appliedDescriptions.remove(0).isEmpty());

        c.getDocument().insertString(0, "Ahoj\nERR\nOK", null);
        b.waitFinished();

        {
            assertEquals("Another set of errors reported", 1, appliedDescriptions.size());
            final List<ErrorDescription> errors = appliedDescriptions.remove(0);
            assertEquals("There is one error", 1, errors.size());
            ErrorDescription descr = errors.get(0);
            assertEquals("Starts right", 5, descr.getRange().getBegin().getOffset());
            assertEquals("Ends right", 8, descr.getRange().getEnd().getOffset());
        }

        c.getDocument().remove(0, 5);
        b.waitFinished();

        {
            assertEquals("Yet another set of errors reported", 1, appliedDescriptions.size());
            final List<ErrorDescription> errors = appliedDescriptions.remove(0);
            assertEquals("There is one error", 1, errors.size());
            ErrorDescription descr = errors.get(0);
            assertEquals("Starts right", 0, descr.getRange().getBegin().getOffset());
            assertEquals("Ends right", 3, descr.getRange().getEnd().getOffset());
        }

        Reference<Object> ref = new WeakReference<>(c);
        c = null;
        mp = null;

        NbTestCase.assertGC("Component can disappear", ref);

        ref = new WeakReference<>(b);
        b = null;
        NbTestCase.assertGC("Bridge can disappear", ref);
    }

    private static final class MockErrorProvider implements ErrorProvider {

        private final Reference<JTextComponent> c;

        MockErrorProvider(JTextComponent c) {
            this.c = new WeakReference<>(c);
        }

        @Override
        public List<? extends Diagnostic> computeErrors(Context context) {
            List<Diagnostic> arr = new ArrayList<>();
            JTextComponent tmp = c.get();
            if (tmp == null) {
                return arr;
            }
            Document doc = tmp.getDocument();
            try {
                String txt = doc.getText(0, doc.getLength());
                for (int from = -1;;) {
                    int at = txt.indexOf("ERR", from + 1);
                    if (at == -1) {
                        break;
                    }
                    Diagnostic d = Diagnostic.Builder.create(() -> at, () -> at + 3, "at" + at)
                        .setSeverity(Diagnostic.Severity.Error)
                        .build();
                    arr.add(d);
                    from = at;
                }
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
            return arr;
        }

    }
}
