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

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.memorylint.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;


//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.heapwalk.memorylint.Rule.class)
public class RetainedSetByInstance extends IteratingRule {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class Customizer extends JPanel {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        JTextField txtFld;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        Customizer() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JLabel caption = new JLabel(NbBundle.getMessage(RetainedSetByInstance.class, "LBL_ClassName"));
            caption.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            add(caption);
            txtFld = new JTextField(CLASSNAME, 15);
            txtFld.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        RetainedSetByInstance.this.updateClassName(txtFld.getText());
                    }

                    public void insertUpdate(DocumentEvent e) {
                        RetainedSetByInstance.this.updateClassName(txtFld.getText());
                    }

                    public void removeUpdate(DocumentEvent e) {
                        RetainedSetByInstance.this.updateClassName(txtFld.getText());
                    }
                });
            add(txtFld);
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static String CLASSNAME = "java.io.File";  // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Heap heap;
    private Set<Histogram> allDocs = new HashSet<Histogram>();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public RetainedSetByInstance() {
        super(NbBundle.getMessage(RetainedSetByInstance.class, "LBL_RSBI_Name"),
                NbBundle.getMessage(RetainedSetByInstance.class, "LBL_RSBI_Desc"),
                CLASSNAME);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    @Override
    public String getHTMLDescription() {
        return NbBundle.getMessage(RetainedSetByInstance.class, "LBL_RSBI_LongDesc");
    }

    public void perform(Instance hm) {
        Set<Instance> retained = Utils.getRetainedSet(hm, heap);
        Histogram<Histogram.Entry> hist = new Histogram<Histogram.Entry>();

        for (Instance i : retained) {
            String key = Utils.printClass(getContext(), i.getJavaClass().getName());
            hist.add(key, new Histogram.Entry(i.getSize()));
        }

        allDocs.add(hist);
    }

    @Override
    protected JComponent createCustomizer() {
        return new Customizer();
    }

    @Override
    protected void prepareRule(MemoryLint context) {
        heap = context.getHeap();
    }
    
    @Override
    protected String resultsHeader() {
        return "<h2>" + getDisplayName() + " (" + Utils.printClass(getContext(), CLASSNAME) + ")</h2>"; // NOI18N
    }

    @Override
    protected void summary() {
        for (Histogram h : allDocs) {
//            getContext().appendResults("<hr>Histogram of retained size:<br>");
            getContext().appendResults(h.toString(0));
        }
    }

    void updateClassName(String className) {
        CLASSNAME = className;
        setClassNamePattern(className);
    }
}
