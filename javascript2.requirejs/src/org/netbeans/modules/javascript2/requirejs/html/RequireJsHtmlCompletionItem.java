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

package org.netbeans.modules.javascript2.requirejs.html;

import java.awt.Color;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem.hexColorCode;
import org.netbeans.modules.web.common.ui.api.FileReferenceCompletion;
import org.netbeans.spi.editor.completion.LazyCompletionItem;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class RequireJsHtmlCompletionItem {

    public static HtmlCompletionItem createFileCompletionItem(FileObject file, int substitutionOffset) {
        boolean folder = file.isFolder();
        String name = new StringBuilder().append(file.getNameExt()).append(folder ? '/' : "").toString();
        Color color = folder ? Color.BLUE : null;
        ImageIcon icon = FileReferenceCompletion.getIcon(file);
        return new FileAttributeValue(folder, name, substitutionOffset, color, icon);
    }

    /**
     * Item representing a File attribute
     */
    public static class FileAttributeValue extends HtmlCompletionItem implements PropertyChangeListener, LazyCompletionItem {

        private javax.swing.ImageIcon icon;
        private final Color color;
        private boolean visible;
        private final boolean folder;

        FileAttributeValue(boolean folder, String text, int substitutionOffset, Color color, javax.swing.ImageIcon icon) {
            super(text, substitutionOffset);
            this.folder = folder;
            this.color = color;
            this.icon = icon;
        }

        @Override
        protected ImageIcon getIcon() {
            return icon;
        }

        @Override
        protected String getLeftHtmlText() {
            if (color == null) {
                return getItemText();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("<font color=#"); //NOI18N
                sb.append(hexColorCode(color));
                sb.append(">"); //NOI18N
                sb.append(getItemText());
                sb.append("</font>"); //NOI18N
                return sb.toString();
            }
        }

        @Override
        public CharSequence getSortText() {
            return folder ? new StringBuilder().append("_").append(getItemText()).toString() : getItemText();
        }
        
        private void iconLoaded(ImageIcon icon) {
            this.icon = icon;
            if (visible) {
                repaintCompletionView_EDT();
            }
        }

        private void repaintCompletionView_EDT() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    repaintCompletionView();
                }
            });
        }

        private static void repaintCompletionView() {
            try {
                Completion completion = Completion.get();
                Class<? extends Completion> clz = completion.getClass();
                Method method = clz.getDeclaredMethod("repaintCompletionView"); //NOI18N
                method.setAccessible(true);
                method.invoke(completion);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                //ignore
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("iconLoaded")) { //NOI18N
                iconLoaded((ImageIcon) evt.getNewValue());
            }
        }

        @Override
        public boolean accept() {
            visible = true;
            return true;
        }

        @Override
        protected String getSubstituteText() {
            String subText = super.getSubstituteText();
            if (!folder) {
                int index = subText.lastIndexOf('.');
                if (index > 0) {
                    subText = subText.substring(0, index);
                }
            }
            return subText;
        }
    }
}
