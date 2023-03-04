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

package org.netbeans.modules.ide.ergonomics.fod;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class BrokenProjectInfo implements HyperlinkListener {

    static JEditorPane getErrorPane(String reason) {
        JEditorPane errorLabel = new JEditorPane();
        JLabel infoLabel = new JLabel();

        errorLabel.setContentType("text/html"); // NOI18N
        errorLabel.setEditable(false);
        errorLabel.setForeground(UIManager.getDefaults().getColor("nb.errorForeground"));
        errorLabel.setRequestFocusEnabled(false);
        errorLabel.setBackground(infoLabel.getBackground());
        errorLabel.setFont(infoLabel.getFont());

        errorLabel.addHyperlinkListener(new BrokenProjectInfo());

        errorLabel.setText(reason);
        
        return errorLabel;
    }

    public static void showInfo(BrokenProject prj) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(getErrorPane(prj.msg));
        DialogDisplayer.getDefault().notify(nd);
    }
    
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (HyperlinkEvent.EventType.ACTIVATED == evt.getEventType()) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(evt.getURL());
        }
    }
}
