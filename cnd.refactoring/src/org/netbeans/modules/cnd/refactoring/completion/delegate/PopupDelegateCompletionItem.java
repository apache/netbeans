/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.refactoring.completion.delegate;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeymap;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 */
public class PopupDelegateCompletionItem implements CompletionItem {

    private static final int PRIORITY = 10;
    private final int substitutionOffset;
    private final Action action;
    private final String rightText;

    private PopupDelegateCompletionItem(int substitutionOffset, Action action, String rightText) {
        this.substitutionOffset = substitutionOffset;
        this.action = action;
        this.rightText = rightText;
    }

    public static PopupDelegateCompletionItem createImplementItem(int substitutionOffset, JTextComponent component) {
        Action action = FileUtil.getConfigObject("Editors/Actions/generate-code.instance", Action.class); // NOI18N
        if (action != null) {
            String rightText = "";
            BaseKit kit = Utilities.getKit(component);
            if (kit != null) {
                Action a = kit.getActionByName((String)action.getValue(Action.NAME));
                MultiKeymap keymap = kit.getKeymap();
                if (keymap != null) {
                    KeyStroke[] keys = keymap.getKeyStrokesForAction(a);
                    if (keys != null && keys.length > 0) {
                        KeyStroke ks = keys[0];
                        String keyModifiersText = KeyEvent.getKeyModifiersText(ks.getModifiers());
                        if (keyModifiersText.length() > 0) {
                            rightText = keyModifiersText + "+" + KeyEvent.getKeyText(ks.getKeyCode()); // NOI18N
                        } else {
                            rightText = KeyEvent.getKeyText(ks.getKeyCode());
                        }
                    }
                }
            }
            return new PopupDelegateCompletionItem(substitutionOffset, action, rightText);
        }
        return null;
    }

    public String getItemText() {
        return NbBundle.getMessage(PopupDelegateCompletionItem.class, "GENERATE"); //NOI18N
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            JTextComponent component = (JTextComponent) evt.getSource();
            int caretOffset = component.getSelectionEnd();
            final int len = caretOffset - substitutionOffset;
            if (len < 0) {
                Completion.get().hideDocumentation();
                Completion.get().hideCompletion();
            }
        }
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return new CompletionTask() {

            @Override
            public void query(CompletionResultSet resultSet) {
                resultSet.setDocumentation(new CompletionDocumentationImpl());
                resultSet.finish();
            }

            @Override
            public void refresh(CompletionResultSet resultSet) {
                query(resultSet);
            }

            @Override
            public void cancel() {
            }

        };
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(true), getRightHtmlText(true), g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(true), getRightHtmlText(true), g, defaultFont, defaultColor, width, height, selected);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(this.getLeftHtmlText(false));
        out.append(this.getRightHtmlText(false)); 
        return out.toString();
    }

    @Override
    public int getSortPriority() {
        return PRIORITY;
    }

    @Override
    public CharSequence getSortText() {
        return getItemText();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    protected ImageIcon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/refactoring/resources/delegate_generate.png")); // NOI18N
    }

    protected String getLeftHtmlText(boolean html) {
        return getItemText();
    }

    protected String getRightHtmlText(boolean html) {
        return rightText;
    }

    protected void substituteText(final JTextComponent c, final int offset, final int origLen) {
        action.actionPerformed(null);
    }

    private static final class CompletionDocumentationImpl implements CompletionDocumentation {

        public CompletionDocumentationImpl() {
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(PopupDelegateCompletionItem.class, "GENERATE_HINT"); //NOI18N
        }

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }
}
