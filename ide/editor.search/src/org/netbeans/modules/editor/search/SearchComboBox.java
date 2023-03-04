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
package org.netbeans.modules.editor.search;

import java.awt.Dimension;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;

public class SearchComboBox<E> extends JComboBox<E> {

    private static final int DEFAULT_INCREMANTAL_SEARCH_COMBO_WIDTH = 200;
    private static final int MAX_INCREMANTAL_SEARCH_COMBO_WIDTH = 350;
    private final SearchComboBoxEditor searchComboBoxEditor = new SearchComboBoxEditor();

    public SearchComboBox() {
        setEditable(true);
        setEditor(searchComboBoxEditor);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        int width;
        int editsize = this.getEditor().getEditorComponent().getPreferredSize().width + 10;
        if (editsize > DEFAULT_INCREMANTAL_SEARCH_COMBO_WIDTH && editsize < MAX_INCREMANTAL_SEARCH_COMBO_WIDTH) {
            width = editsize;
        } else if (editsize >= MAX_INCREMANTAL_SEARCH_COMBO_WIDTH) {
            width = MAX_INCREMANTAL_SEARCH_COMBO_WIDTH;
        } else {
            width = DEFAULT_INCREMANTAL_SEARCH_COMBO_WIDTH;
        }
        return new Dimension(width,
                super.getPreferredSize().height);
    }

    public JEditorPane getEditorPane() {
        return searchComboBoxEditor.getEditorPane();
    }


}
