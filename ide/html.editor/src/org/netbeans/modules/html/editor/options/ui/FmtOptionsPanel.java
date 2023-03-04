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
package org.netbeans.modules.html.editor.options.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.swing.JPanel;
import org.netbeans.modules.html.editor.options.ui.FmtOptions.CategorySupport;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages("displayName=HTML")
public class FmtOptionsPanel extends JPanel {

    public static PreferencesCustomizer.Factory getController() {
        String preview = "";
        try {
            preview = getPreviewText();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new CategorySupport.Factory("text/html", PreferencesCustomizer.TABS_AND_INDENTS_ID, FmtOptionsPanel.class,
                preview,
                new String[]{FmtOptions.rightMargin, "30"}, //NOI18N
                new String[]{FmtOptions.initialIndent, "0"} //NOI18N
                );
    }

    private static synchronized String getPreviewText() throws IOException {
        StringBuilder sb = new StringBuilder();                         
        InputStream sample = FmtOptionsPanel.class.getClassLoader().getResourceAsStream("org/netbeans/modules/html/editor/options/ui/formatSample.html"); //NOI18N
        Reader sr = new InputStreamReader(sample);
        int read;
        char[] buf = new char[256];
        while ((read = sr.read(buf)) > 0) {
            sb.append(buf, 0, read);
        }
        return sb.toString();
    }

}
