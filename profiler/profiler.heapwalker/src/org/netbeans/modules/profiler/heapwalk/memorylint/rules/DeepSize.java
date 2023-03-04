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

package org.netbeans.modules.profiler.heapwalk.memorylint.rules;

import javax.swing.BorderFactory;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.memorylint.*;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;


//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.heapwalk.memorylint.Rule.class)
public class DeepSize extends IteratingRule {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class Customizer extends JPanel {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        JTextField txtFld;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        Customizer() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JLabel caption = new JLabel(NbBundle.getMessage(DeepSize.class, "LBL_ClassName"));
            caption.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            add(caption);
            txtFld = new JTextField(className, 15);
            txtFld.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        className = txtFld.getText();
                    }

                    public void insertUpdate(DocumentEvent e) {
                        className = txtFld.getText();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        className = txtFld.getText();
                    }
                });
            add(txtFld);
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    Walker walker;
    private static String className = "java.io.File"; // NOI18N

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public DeepSize() {
        super(NbBundle.getMessage(DeepSize.class, "LBL_DS_Name"),
                NbBundle.getMessage(DeepSize.class, "LBL_DS_Desc"),
                ""); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    @Override
    public String getHTMLDescription() {
        return NbBundle.getMessage(DeepSize.class, "LBL_DS_LongDesc");
    }

    @Override
    protected JComponent createCustomizer() {
        return new Customizer();
    }

    protected void perform(Instance hm) {
        walker.walk(hm);
    }

    protected @Override void prepareRule(MemoryLint context) {
        setClassNamePattern(className);
        walker = new Walker();
    }

    @Override
    protected String resultsHeader() {
        return NbBundle.getMessage(DeepSize.class, "LBL_DS_ResHeader", Utils.printClass(getContext(), className));
    }

    protected @Override void summary() {
        Distribution res = walker.getResults();
        String str = res.toString();
        str = str.replace("\n", "<br>"); // NOI18N
        str = str.replace("  ", "&nbsp;&nbsp;"); // NOI18N
        getContext().appendResults(str);
    }
}
