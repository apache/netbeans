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
package org.netbeans.modules.xml.multiview;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * The class provides link between editor text component and related data model
 * to make easier implementation of item editors
 *
 * @author pfiala
 */
public class ItemEditorHelper implements Refreshable {

    ItemEditorHelper.ItemDocument doc;

    /**
     * Model of item providing unified interface between text component and item data
     */
    public static abstract class ItemEditorModel {

        private ItemEditorHelper itemEditorHelper;

        /**
         * Retrieves edited text from editor component
         *
         * @return text
         */
        public final String getEditorText() {
            return itemEditorHelper == null ? null : itemEditorHelper.getEditorText();
        }

        /**
         * Editor component getter
         *
         * @return editor component
         */
        public final JTextComponent getEditorComponent() {
            return itemEditorHelper == null ? null : itemEditorHelper.getEditorComponent();
        }

        /**
         * Called by editor helper to retrieve item value from model
         *
         * @return value of edited item
         */
        public abstract String getItemValue();

        /**
         * Called by the editor helper when finished editing of the text component.
         * An implementation can perform validation, update item value by the editor text
         * or define behavior in case of fail
         *
         * @param value a new value of edited item
         * @return true - the new value is accepted, false - the new value is invalid
         */
        public abstract boolean setItemValue(String value);

        /**
         * Called by the editor support whenever edited text is changed.
         * An implementation can perform immediate validation
         * or update of item value in the model
         */
        public abstract void documentUpdated();

    }

    private JTextComponent getEditorComponent() {
        return editorComponent;
    }

    private final JTextComponent editorComponent;
    private ItemEditorModel model;

    /**
     * Creates item editor helper for given text component with default implementation of data model.
     *
     * @param textComponent editor component
     */
    public ItemEditorHelper(final JTextComponent textComponent) {
        this(textComponent, null);
    }

    /**
     * Creates item editor helper for given text component using user defined data model.
     * Various implementations of model's methods {@link ItemEditorHelper.ItemEditorModel#getItemValue()},
     * {@link ItemEditorHelper.ItemEditorModel#setItemValue(String)} and
     * {@link ItemEditorHelper.ItemEditorModel#documentUpdated()} can define required behavior of the editor.
     *
     * @param textComponent editor component
     * @param model         item data model defining behavior
     */
    public ItemEditorHelper(final JTextComponent textComponent, ItemEditorModel model) {
        this.editorComponent = textComponent;
        doc = new ItemDocument();
        setModel(model);
        editorComponent.setDocument(doc);
        refresh();
    }

    /**
     * Gets item data model of the item editor helper
     *
     * @return item data model
     */
    public ItemEditorModel getModel() {
        return model;
    }

    private void setModel(ItemEditorModel model) {
        this.model = model != null ? model : createDefaultModel();
        this.model.itemEditorHelper = this;
    }

    private static ItemEditorModel createDefaultModel() {
        return new ItemEditorModel() {
            private String value;

            public String getItemValue() {
                return value;
            }

            public boolean setItemValue(String value) {
                this.value = value;
                return true;
            }

            public void documentUpdated() {
            }
        };
    }

    /**
     * Updates editor text by item value from model
     */
    public void refresh() {
        doc.refresh();
    }

    /**
     * Retrieves edited text from editor component
     *
     * @return text of editor component
     */
    public String getEditorText() {
        return editorComponent.getText();
    }

    private class ItemDocument extends PlainDocument {

        boolean refreshing = false;

        public void remove(int offs, int len) throws BadLocationException {
            super.remove(offs, len);
            updateModel();
        }

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            super.insertString(offs, str, a);
            updateModel();
        }

        private void updateModel() {
            if (!refreshing) {
                model.documentUpdated();
                refresh();
            }
        }

        public void refresh() {
            refreshing = true;
            try {
                String itemValue = model.getItemValue();
                String text;
                try {
                    text = getText(0, getLength());
                } catch (BadLocationException e) {
                    text = "";
                    e.printStackTrace();
                }
                if (!text.equals(itemValue)) {
                    try {
                        super.remove(0, getLength());
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                    try {
                        super.insertString(0, itemValue, null);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                refreshing = false;
            }
        }
    }
}
