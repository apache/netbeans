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
package org.netbeans.modules.print.editor;

import java.awt.Color;
import java.awt.Font;

import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openide.text.AttributedCharacters;
import org.netbeans.editor.BaseDocument;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.10.17
 */
@org.openide.util.lookup.ServiceProvider(service=java.awt.event.ActionListener.class)
public final class Editor implements ActionListener {

    @SuppressWarnings("unchecked") // NOI18N
    public void actionPerformed(ActionEvent event) {
        Object object = event.getSource();

        if ( !(object instanceof List)) {
            return;
        }
        List list = (List) object;

        if (list.size() != 1 + 2) {
            return;
        }
        Object param0 = list.get(0);

        if ( !(param0 instanceof BaseDocument)) {
            return;
        }
        BaseDocument document = (BaseDocument) param0;
        Object param1 = list.get(1);

        if ( !(param1 instanceof Integer)) {
            return;
        }
        int start = ((Integer) param1).intValue();
        Object param2 = list.get(2);

        if ( !(param2 instanceof Integer)) {
            return;
        }
        int end = ((Integer) param2).intValue();

        PrintContainer container = new PrintContainer();
        document.print(container, false, true, start, end);
        list.add(container.getIterators());
    }

    // --------------------------------------------------------------------------------------
    private static final class PrintContainer implements org.netbeans.editor.PrintContainer {

        PrintContainer() {
            myCharacters = new AttributedCharacters();
            myCharactersList = new ArrayList<AttributedCharacters>();
        }

        public void add(char[] chars, Font font, Color foreColor, Color backColor) {
//out(getString(foreColor) + " " + getString(backColor) + " " + getString(font) + " " + new String(chars));
            myCharacters.append(chars, font, foreColor);
        }

        public void eol() {
//out();
            myCharactersList.add(myCharacters);
            myCharacters = new AttributedCharacters();
        }

        public boolean initEmptyLines() {
            return false;
        }

        AttributedCharacterIterator[] getIterators() {
            AttributedCharacterIterator[] iterators = new AttributedCharacterIterator[myCharactersList.size()];

            for (int i = 0; i < myCharactersList.size(); i++) {
                iterators[i] = myCharactersList.get(i).iterator();
            }
            return iterators;
        }

        private String getString(Color color) {
            return "(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")"; // NOI18N
        }

        private String getString(Font font) {
            String style = ""; // NOI18N

            if (font.isBold()) {
                style += "bold"; // NOI18N
            }
            if (font.isItalic()) {
                style += " italic"; // NOI18N
            }
            else {
                style += " plain"; // NOI18N
            }
            return "[" + font.getName() + ", " + style + ", " + font.getSize() + "]"; // NOI18N
        }

        private void out() {
            System.out.println();
        }

        private void out(Object object) {
            System.out.println("*** " + object); // NOI18N
        }

        private AttributedCharacters myCharacters;
        private List<AttributedCharacters> myCharactersList;
    }
}
