/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
