/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.vcs;

import java.text.MessageFormat;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class HookUtils {

    /**
     * Replaces the given keys with index elements as needed by {@link MessageFormat}<br>
     * Remaining sequences enclosed by {} brackets will be escaped so what {@link MessageFormat}
     * will ignore them.
     *
     * @param formatString
     * @param keys
     * @return
     */
    public static String prepareFormatString(String formatString, String... keys) {
        formatString = formatString.replaceAll("\\{", "'\\{'");                 // NOI18N
        formatString = formatString.replaceAll("\\}", "'\\}'");                 // NOI18N
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            formatString =
                formatString.replaceAll(
                    "'\\{'" + key + "'\\}'",                                    // NOI18N
                    "\\{" + i + "\\}"                                           // NOI18N
                );

        }
        return formatString;
    }

    /**
     * Determines if the given string contains some other sequences
     * enclosed by {} then the given variables.
     *
     * @param txt
     * @param variables
     * @return
     */
    public static boolean containsUnsupportedVariables(String txt, String... variables) {
        txt = prepareFormatString(txt, variables);
        int idx = txt.indexOf("'{");                                          // NOI18N
        if(idx < 0) {
            return false;
        }
        idx = txt.indexOf("'}", idx);                                         // NOI18N
        return idx > 0;
    }

    public static boolean show(JPanel panel, String title, String okName, HelpCtx helpCtx) {
        JButton ok = new JButton(okName);
        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        JButton cancel = new JButton(NbBundle.getMessage(HookUtils.class, "LBL_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());
        final DialogDescriptor dd =
            new DialogDescriptor(
                    panel,
                    title,
                    true,
                    new Object[]{ok, cancel},
                    ok,
                    DialogDescriptor.DEFAULT_ALIGN,
                    helpCtx,
                    null);
        return DialogDisplayer.getDefault().notify(dd) == ok;
    }
}
