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
package org.netbeans.modules.mercurial.ui.wizards;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class CloneDestinationDirectoryWizardPanel implements WizardDescriptor.Panel, DocumentListener {
    
    static final String CLONE_TARGET_DIRECTORY = "cloneDestinationStep.cloneDirectory"; //NOI18N
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private CloneDestinationDirectoryPanel component;
    private boolean valid;
    private String errorMessage;
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new CloneDestinationDirectoryPanel();
            component.directoryField.getDocument().addDocumentListener(this);
            component.nameField.getDocument().addDocumentListener(this);
            valid();
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(CloneDestinationDirectoryWizardPanel.class);
    }
    
    //public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        //return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    //}
    
    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void changedUpdate(DocumentEvent e) {
        textChanged(e);
    }

    private void textChanged(final DocumentEvent e) {
        // repost later to AWT otherwise it can deadlock because
        // the document is locked while firing event and we try
        // synchronously access its content
        Runnable awt = new Runnable() {
            public void run() {
                if (e.getDocument() == component.nameField.getDocument () || e.getDocument() == component.directoryField.getDocument()) {
                    if (component.isInputValid()) {
                        valid(component.getMessage());
                    } else {
                        invalid(component.getMessage());
                    }
                }
            }
        };
        SwingUtilities.invokeLater(awt);
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    protected final void valid() {
        setValid(true, null);
    }

    protected final void valid(String extErrorMessage) {
        setValid(true, extErrorMessage);
    }

    protected final void invalid(String message) {
        setValid(false, message);
    }

    public final boolean isValid() {
        return valid;
    }

    public final String getErrorMessage() {
        return errorMessage;
    }

    private void setValid(boolean valid, String errorMessage) {
        boolean fire = this.valid != valid;
        fire |= errorMessage != null && (errorMessage.equals(this.errorMessage) == false);
        this.valid = valid;
        this.errorMessage = errorMessage;
        if (fire) {
            fireChangeEvent();
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            HgURL repository = (HgURL) ((WizardDescriptor) settings).getProperty("repository"); // NOI18N

            component.nameField.setText(new File(repository.getPath()).getName());
        }
    }
    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            String directory = ((CloneDestinationDirectoryPanel) component).getDirectory();
            String cloneName = ((CloneDestinationDirectoryPanel) component).getCloneName();
            ((WizardDescriptor) settings).putProperty("directory", directory); //NOI18N
            ((WizardDescriptor) settings).putProperty("cloneName", cloneName);  //NOI18N
        }
    }
}

