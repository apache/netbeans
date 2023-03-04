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

package org.netbeans.modules.palette;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Libor Kotouc
 */
class ActiveEditorDropDefaultProvider implements InstanceContent.Convertor<String,ActiveEditorDrop> {

    private static ActiveEditorDropDefaultProvider instance = new ActiveEditorDropDefaultProvider();

    /** Creates a new instance of ActiveEditorDropDefaultProvider */
    private ActiveEditorDropDefaultProvider() {
    }
    
    static ActiveEditorDropDefaultProvider getInstance() {
        return instance;
    }
    
    public Class<? extends ActiveEditorDrop> type(String obj) {
        //able to convert String instances only
        return ActiveEditorDrop.class;
    }

    public String id(String obj) {
        return obj;
    }

    public String displayName(String obj) {
        return obj;
    }

    public ActiveEditorDrop convert(String obj) {
        return getActiveEditorDrop(obj);
    }
    
    private ActiveEditorDrop getActiveEditorDrop(String body) {

        return new ActiveEditorDropDefault(body);
    }
    
    private static class ActiveEditorDropDefault implements ActiveEditorDrop {

        String body;

        public ActiveEditorDropDefault(String body) {
            this.body = body;
        }

        public boolean handleTransfer(JTextComponent targetComponent) {

            if (targetComponent == null)
                return false;

            try {
                Document doc = targetComponent.getDocument();
                Caret caret = targetComponent.getCaret();
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                doc.remove(p0, p1 - p0);

                //replace selected text by the inserted one
                int start = caret.getDot();
                doc.insertString(start, body, null);
            }
            catch (BadLocationException ble) {
                return false;
            }

            return true;
        }
    }
    
}
