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
package org.netbeans.editor.example;

import java.util.Map;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.html.*;
import org.netbeans.editor.ext.html.dtd.*;

/**
* Editor kit implementation for HTML content type
*
* @author Miloslav Metelka
* @version 0.01
*/

public class HTMLKit extends ExtKit {

    private static final String DEFAULT_DOCTYPE =
        "-//W3C//DTD HTML 4.01 Transitional//EN";  // NOI18N

    static final long serialVersionUID =-1381945567613910297L;

    public static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    public static final String shiftInsertBreakAction = "shift-insert-break"; // NOI18N

    static {
        Settings.addInitializer( new HTMLSettingsInitializer( HTMLKit.class ) );
        Settings.addInitializer( new SaHTMLSettingsInitializer() );
        Settings.reset();
      SAReaderProvider.setupReaders();
    }
    
    public HTMLKit() {
        super();
        DTD myDTD = org.netbeans.editor.ext.html.dtd.Registry.getDTD( DEFAULT_DOCTYPE, null );
    }

    public String getContentType() {
        return HTML_MIME_TYPE;
    }

    /** Create new instance of syntax coloring scanner
    * @param doc document to operate on. It can be null in the cases the syntax
    *   creation is not related to the particular document
    */
    public Syntax createSyntax(Document doc) {
        return new HTMLSyntax();
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new HTMLSyntaxSupport(doc);
    }

    public Completion createCompletion(ExtEditorUI extEditorUI) {
        return new HTMLCompletion(extEditorUI);
    }

    protected Action[] createActions() {
        Action[] HTMLActions = new Action[] {
                                   new HTMLShiftBreakAction()
                               };
        return TextAction.augmentList(super.createActions(), HTMLActions);
    }
    

    public static class HTMLShiftBreakAction extends BaseAction {

        static final long serialVersionUID =4004043376345356061L;

        public HTMLShiftBreakAction() {
            super( shiftInsertBreakAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Completion completion = ExtUtilities.getCompletion(target);
                if (completion != null && completion.isPaneVisible()) {
                    if (completion.substituteText( true )) {
//                        completion.setPaneVisible(false);
                    } else {
                        completion.refresh(false);
                    }
                }
            }
        }

    }

    private static class SaHTMLSettingsInitializer extends Settings.AbstractInitializer {
        public SaHTMLSettingsInitializer() {
            super( "sa-html-settings-initializer" ); // NOI18N
        }
            
        public void updateSettingsMap(Class kitClass, Map settingsMap) {
            if (kitClass == HTMLKit.class) {
                SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST, getHTMLKeyBindings());
            }
        }

        public MultiKeyBinding[] getHTMLKeyBindings() {
            return new MultiKeyBinding[] {
                new MultiKeyBinding(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
                    HTMLKit.shiftInsertBreakAction
                )
            };
        }
    }

}
