/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.utils.helper.Text;

public class NbiTextsDialog extends NbiDialog {
    private NbiTabbedPane textsTabbedPane;
    
    private String            title;
    private Map<String, Text> texts;
    
    public NbiTextsDialog(String title, Map<String, Text> texts) {
        super();
        
        this.title = title;
        this.texts = texts;
        
        initComponents();
        initialize();
    }
    
    public NbiTextsDialog(NbiFrame owner, String title, Map<String, Text> texts) {
        super(owner);
        
        this.title = title;
        this.texts = texts;
        
        initComponents();
        initialize();
    }
    
    private void initialize() {
        setTitle(title);
        
        textsTabbedPane.removeAll();
        
        for (String tabTitle: texts.keySet()) {
            textsTabbedPane.addTab(tabTitle, createTab(texts.get(tabTitle)));
        }
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        
        textsTabbedPane = new NbiTabbedPane();
        
        add(textsTabbedPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 11, 11), 0, 0));
    }
    
    private JComponent createTab(Text text) {
        NbiTextPane   textPane;
        NbiPanel      textPanel;
        NbiScrollPane textScrollPane;
        
        textPane = new NbiTextPane();
        textPane.setText(text);
        
        textPanel = new NbiPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(textPane, BorderLayout.CENTER);
        
        textScrollPane = new NbiScrollPane(textPanel);
        textScrollPane.setViewportBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        return textScrollPane;
    }
}
