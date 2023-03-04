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
