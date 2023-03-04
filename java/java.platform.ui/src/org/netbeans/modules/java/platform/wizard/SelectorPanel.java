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

package org.netbeans.modules.java.platform.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.platform.InstallerRegistry;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Tomas Zezula
 */
class SelectorPanel extends javax.swing.JPanel implements ItemListener {
        
    private Map<ButtonModel,GeneralPlatformInstall> installersByButtonModels = new IdentityHashMap<ButtonModel,GeneralPlatformInstall>();
    private ButtonGroup group;
    private SelectorPanel.Panel firer;
    
    /** Creates new form SelectorPanel */
    public SelectorPanel(SelectorPanel.Panel firer) {
        this.firer = firer;
        initComponents();
        postInitComponents ();
        this.setName (NbBundle.getMessage(SelectorPanel.class,"TXT_SelectPlatformTypeTitle"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectorPanel.class,"AD_SelectPlatformType"));
    }
    
    private void postInitComponents () {
        InstallerRegistry regs = InstallerRegistry.getDefault();
        List<GeneralPlatformInstall> installers = regs.getAllInstallers();
        this.group = new ButtonGroup ();        
        JLabel label = new JLabel (NbBundle.getMessage(SelectorPanel.class,"TXT_SelectPlatform"));
        label.setDisplayedMnemonic(NbBundle.getMessage(SelectorPanel.class,"AD_SelectPlatform").charAt(0));        
        GridBagConstraints c = new GridBagConstraints ();
        c.gridx = c.gridy = GridBagConstraints.RELATIVE;
        c.gridheight = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.insets = new Insets (12, 12, 6, 12);
        ((GridBagLayout)this.getLayout()).setConstraints(label,c);
        this.add (label);
        Iterator<GeneralPlatformInstall> it = installers.iterator();
        for (int i=0; it.hasNext(); i++) {
            GeneralPlatformInstall pi = it.next ();            
            JRadioButton button = new JRadioButton (pi.getDisplayName());
            if (i==0) {
                label.setLabelFor(button);
            }
            button.addItemListener(this);
            this.installersByButtonModels.put (button.getModel(), pi);
            this.group.add(button);
            c = new GridBagConstraints ();
            c.gridx = c.gridy = GridBagConstraints.RELATIVE;
            c.gridheight = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 1.0;
            c.insets = new Insets (6, 18, it.hasNext()? 0 : 12, 12);
            ((GridBagLayout)this.getLayout()).setConstraints(button,c);
            this.add (button);
        }
        JPanel pad = new JPanel ();
        c = new GridBagConstraints ();
        c.gridx = c.gridy = GridBagConstraints.RELATIVE;
        c.gridheight = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets (12,0,0,12);
        ((GridBagLayout)this.getLayout()).setConstraints(pad,c);
        this.add (pad);
    }
    
    private void readSettings () {
        if (this.group.getSelection()==null) {
            Enumeration<AbstractButton> buttonEnum = this.group.getElements();
            assert buttonEnum.hasMoreElements();
            ((JRadioButton)buttonEnum.nextElement()).setSelected(true);
        }
    }

    public void itemStateChanged(java.awt.event.ItemEvent e) {
        this.firer.cs.fireChange();
    }
    
    
    /**
     * Used by unit tests
     * Select the GeneralPlatformInstall to allow step over this panel
     */
    boolean selectInstaller (GeneralPlatformInstall install) {
        assert install != null;
        for (Map.Entry<ButtonModel,GeneralPlatformInstall> entry : installersByButtonModels.entrySet()) {
            if (entry.getValue().equals(install)) {
                ButtonModel model = entry.getKey();
                model.setSelected(true);
                return true;
            }
        }
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    
    public static class Panel implements WizardDescriptor.Panel<WizardDescriptor> {
        
        private final ChangeSupport cs = new ChangeSupport(this);
        private SelectorPanel component;
        
        public synchronized void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public synchronized void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }                

        public void readSettings(WizardDescriptor wiz) {
            getComponent().readSettings();
        }

        public void storeSettings(WizardDescriptor wiz) {
        }

        public HelpCtx getHelp() {
            return new HelpCtx (SelectorPanel.class);
        }

        public boolean isValid() {
            return this.component != null;
        }

        public SelectorPanel getComponent() {
            if (this.component == null) {
                this.component = new SelectorPanel (this);
            }
            return this.component;
        }
        
        public GeneralPlatformInstall getInstaller () {
            SelectorPanel c = getComponent ();
            ButtonModel bm = c.group.getSelection();
            if (bm != null) {            
                return c.installersByButtonModels.get(bm);
            }
            return null;
        }
        
        public TemplateWizard.InstantiatingIterator getInstallerIterator () {
            GeneralPlatformInstall platformInstall = getInstaller ();
            if (platformInstall instanceof CustomPlatformInstall) {
                return ((CustomPlatformInstall)platformInstall).createIterator();
            }
            return null;
        }
        
    }
}
