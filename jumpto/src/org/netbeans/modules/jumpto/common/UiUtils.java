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
