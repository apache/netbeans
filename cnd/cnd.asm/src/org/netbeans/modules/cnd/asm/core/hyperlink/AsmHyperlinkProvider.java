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
package org.netbeans.modules.cnd.asm.core.hyperlink;

import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

import org.netbeans.modules.cnd.asm.core.assistance.GoToLabelAction;
import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.cnd.asm.model.AsmModelAccessor;
import org.netbeans.modules.cnd.asm.model.AsmState;

public class AsmHyperlinkProvider implements HyperlinkProvider {

    private Document lastDocument;
    private int[] lastResult;
    private final GoToLabelAction labelResolver = new GoToLabelAction();

    public AsmHyperlinkProvider() {
        lastResult = new int[]{-1, -1, -1};
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        AsmModelAccessor acc = AsmObjectUtilities.getAccessor(doc);
        if (acc == null) {
            return false;
        }
        AsmState state = acc.getState();
        lastDocument = doc;
        if (state != null) {
            int res[] = labelResolver.computeLabel(state, offset);
            if (res[0] != -1) {
                lastResult = res;
                return true;
            }
        }

        return false;
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (doc != lastDocument && !checkResult()) {
            return null;
        }

        return new int[]{lastResult[0],
                    lastResult[1]
                };
    }

    public void performClickAction(Document doc, int offset) {

        if (doc != lastDocument || !checkResult()) {
            return;
        }

        DataObject ob = NbEditorUtilities.getDataObject(doc);
        int position = lastResult[2];

        if (!openFileInEditor(ob)) {
            return;
        }

        EditorCookie ed = ob.getCookie(org.openide.cookies.EditorCookie.class);

        if (ed != null) {
            try {
                ed.openDocument();
            } catch (IOException ex) {
                return;
            }

            JEditorPane pane = ed.getOpenedPanes()[0];
            pane.setCaretPosition(position);

            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, pane);
            if (tc != null) {
                tc.requestActive();
            }
        }
    }

    private boolean checkResult() {
        return !(lastResult[0] == -1 || lastResult[1] == -1 ||
                lastResult[2] == -1);
    }

    private boolean openFileInEditor(DataObject ob) {
        EditCookie ck = ob.getLookup().lookup(EditCookie.class);
        if (ck != null) {
            ck.edit();
            return true;
        }
        OpenCookie oc = ob.getLookup().lookup(OpenCookie.class);
        if (oc != null) {
            oc.open();
            return true;
        }
        return false;
    }
}
