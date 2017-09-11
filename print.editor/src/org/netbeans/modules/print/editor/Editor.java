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
 * License. When distributing the software, include this License Header
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
