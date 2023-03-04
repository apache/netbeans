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
    public abstract static class ItemEditorModel {

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
