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
package org.netbeans.modules.php.project.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.UIManager;

/**
 * Inspired by HTMLArea from profiler.
 */
public class HintArea extends JTextPane {

    private static final long serialVersionUID = 76873543674545L;


    public HintArea() {
        Color hintBackground = Utils.getHintBackground();
        setOpaque(true);
        setAutoscrolls(true);
        setForeground(UIManager.getColor("Label.foreground")); // NOI18N
        setFont(UIManager.getFont("Label.font")); // NOI18N
        setBackground(hintBackground);
        setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, hintBackground));
        setContentType("text/html"); // NOI18N
        setEditable(false);
    }

    @Override
    public void setText(String value) {
        if (value == null) {
            return;
        }

        Font font = getFont();
        Color textColor = getForeground();
        value = value.replaceAll("\\n\\r|\\r\\n|\\n|\\r", "<br>"); // NOI18N
        value = value.replace("<code>", "<code style=\"font-size: " + font.getSize() + "pt;\">"); // NOI18N

        String colorText = "rgb(" + textColor.getRed() + "," + textColor.getGreen() + "," + textColor.getBlue() + ")"; // NOI18N
        String newText = "<html><body style=\"color: " + colorText + "; font-size: " + font.getSize()  // NOI18N
                + "pt; font-family: " + font.getName() + ";\">" + value + "</body></html>"; // NOI18N

        setDocument(getEditorKit().createDefaultDocument()); // Workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5042872
        super.setText(newText);
    }

}
