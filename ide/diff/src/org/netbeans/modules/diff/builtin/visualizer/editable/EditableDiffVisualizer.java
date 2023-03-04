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
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.spi.diff.DiffVisualizer;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.DiffPresenter;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.Reader;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.ImageUtilities;

/**
 * Registration of the editable visualizer. 
 * 
 * @author Maros Sandor
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.diff.DiffVisualizer.class)
public class EditableDiffVisualizer extends DiffVisualizer {

    /**
     * Get the display name of this diff visualizer, CALLED VIA REFLECTION.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(EditableDiffVisualizer.class, "CTL_EditableDiffVisualizer_Name"); // NOI18N
    }
    
    /**
     * Get a short description of this diff visualizer, CALLED VIA REFLECTION.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(EditableDiffVisualizer.class, "CTL_EditableDiffVisualizer_Desc"); // NOI18N
    }
    
    public Component createView(Difference[] diffs, String name1, String title1, Reader r1, String name2, String title2, Reader r2, String MIMEType) throws IOException {
        return createDiff(diffs, StreamSource.createSource(name1, title1, MIMEType, r1), StreamSource.createSource(name2, title2, MIMEType, r2)).getComponent();
    }

    public DiffView createDiff(Difference[] diffs, StreamSource s1, StreamSource s2) throws IOException {
        EDVManager manager = new EDVManager(s1, s2);
        manager.init();
        return manager.getView();
    }

    private static class EDVManager implements PropertyChangeListener {
        
        private final StreamSource  s1;
        private final StreamSource  s2;
        
        private EditableDiffView    view;
        
        private Action              nextAction;
        private Action              prevAction;

        public EDVManager(StreamSource s1, StreamSource s2) {
            this.s1 = s1;
            this.s2 = s2;
        }

        public EditableDiffView getView() {
            return view;
        }

        public void init() throws IOException {
            view = new EditableDiffView(s1, s2);
            view.addPropertyChangeListener(this);
            JComponent component = (JComponent) view.getComponent();

            JToolBar toolbar = new JToolBar();
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setRollover(true);

            nextAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onNext();
                }
            };
            nextAction.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/diff/builtin/visualizer/editable/diff-next.png", false)); // NOI18N
            JButton nextButton = new JButton(nextAction);
            nextButton.setMargin(new Insets(2, 2, 2, 2));
            toolbar.add(nextButton);
            
            prevAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onPrev();
                }
            };
            prevAction.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/diff/builtin/visualizer/editable/diff-prev.png", false)); // NOI18N
            JButton prevButton = new JButton(prevAction);
            prevButton.setMargin(new Insets(2, 2, 2, 2));
            toolbar.add(prevButton);
        
            component.putClientProperty(DiffPresenter.PROP_TOOLBAR, toolbar);
            component.getActionMap().put("jumpNext", nextAction);  // NOI18N
            component.getActionMap().put("jumpPrev", prevAction); // NOI18N
            
            refreshComponents();
        }

        private void onPrev() {
            int idx = view.getCurrentDifference();
            if (idx > 0) {
                view.setCurrentDifference(idx - 1);
            }
        }

        private void onNext() {
            int idx = view.getCurrentDifference();
            if (idx < view.getDifferenceCount() - 1) {
                view.setCurrentDifference(idx + 1);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            refreshComponents();
        }

        private void refreshComponents() {
            nextAction.setEnabled(view.getCurrentDifference() < view.getDifferenceCount() - 1);
            prevAction.setEnabled(view.getCurrentDifference() > 0);
        }
    }
} 
