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
