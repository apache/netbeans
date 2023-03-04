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

package org.netbeans.modules.jumpto.common;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public final class UiUtils {
    private UiUtils() {
        throw new IllegalStateException("No instance allowed.");    //NOI18N
    }

    @NonNull
    public static DocumentFilter newUserInputFilter() {
        return new UserInputFilter();
    }

    @NonNull
    public static String htmlize(@NonNull final CharSequence input) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            switch (c) {
                case '&':   //NOI18N
                    sb.append("&amp;"); //NOI18N
                    break;
                case '<':   //NOI18N
                    sb.append("&lt;");  //NOI18N
                    break;
                case '>':   //NOI18N
                    sb.append("&gt;");  //NOI18N
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }


    private static final class UserInputFilter extends DocumentFilter {

        @Override
        public void insertString(
                @NonNull final FilterBypass fb,
                final int offset,
                @NonNull final String string,
                @NullAllowed final AttributeSet attr) throws BadLocationException {
            if (Utils.isValidInput(string)) {
                super.insertString(fb, offset, string, attr);
            } else {
                handleWrongInput();
            }
        }

        @Override
        public void replace(
                @NonNull final FilterBypass fb,
                final int offset,
                final int length,
                @NullAllowed final String text,
                @NullAllowed final AttributeSet attrs) throws BadLocationException {
            if (text == null || Utils.isValidInput(text)) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                handleWrongInput();
            }
        }

        private static void handleWrongInput() {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(UiUtils.class, "TXT_IllegalContent"));
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
