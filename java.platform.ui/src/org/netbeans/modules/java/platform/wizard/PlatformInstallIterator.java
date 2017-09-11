/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.platform.wizard;

import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.event.*;

import org.netbeans.modules.java.platform.InstallerRegistry;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;
import org.netbeans.spi.java.platform.PlatformInstall;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author  sd99038
 */
public class PlatformInstallIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor>, ChangeListener {
    
    WizardDescriptor.InstantiatingIterator<WizardDescriptor> typeIterator;
    int                     panelIndex; // -1 - not set, 0 - the first panel, 1 - files chooser, 2 - custom panel from PlatformInstall, 3 - custom panel from CustomPlatformInstall
    boolean                 hasSelectorPanel;
    WizardDescriptor          wizard;
    int                     panelNumber = -1;

    ResourceBundle          bundle = NbBundle.getBundle(PlatformInstallIterator.class);
    LocationChooser.Panel   locationPanel = new LocationChooser.Panel();
    SelectorPanel.Panel     selectorPanel = new SelectorPanel.Panel ();
    Collection<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    PlatformInstallIterator() {
        selectorPanel.addChangeListener(this);
        locationPanel.addChangeListener(this);        
    }
    
    public static PlatformInstallIterator create() {
        return new PlatformInstallIterator();
    }
    
    
    /**
     * Used by unit tests
     * Returns the current state of the wizard iterator
     */  
    int getPanelIndex () {
        return this.panelIndex;
    }
    
    void updatePanelsList (final JComponent[] where, final WizardDescriptor.InstantiatingIterator<WizardDescriptor> it) {
        Collection<String> c = new LinkedList<String>();
        if (this.hasSelectorPanel) {
            c.add (bundle.getString("TXT_SelectPlatformTypeTitle"));
        }
        if (this.panelIndex == 1 || this.panelIndex == 2 || 
            (this.panelIndex == 0 && this.selectorPanel.getInstallerIterator()==null)) {
            c.add(bundle.getString("TXT_PlatformFolderTitle")); // NOI18N
        }
        if (it != null) {
            // try to suck stuff out of the iterator's first panel :-(
            WizardDescriptor.Panel p = it.current();
            if (p != null) {
                javax.swing.JComponent pc = (javax.swing.JComponent)p.getComponent();
                String[] steps = (String[])pc.getClientProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
                if (steps != null)
                    c.addAll(Arrays.asList(steps));
            }
        } else {
            c.add(bundle.getString("TITLE_PlatformLocationUnknown")); // NOI18N
        }
        String[] names = c.toArray(new String[c.size()]);
        for (JComponent comp : where) {
            comp.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA,names); // NOI18N
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        if (panelIndex == 0) {
            return selectorPanel;
        }
        else if (panelIndex == 1) {
            return locationPanel;
        } else {
            if (typeIterator == null)
                throw new NullPointerException ("index: " + panelIndex);
            return typeIterator.current();
        }
    }
 
    /**
     * The overall iterator has the next panel iff:
     * - the current panel is the location chooser && the chooser has an iterator
     * selected && that iterator has at least one panel
     * - the current iterator reports it has the next panel
     */
    public boolean hasNext() {        
        if (panelIndex == 0) {
            GeneralPlatformInstall installer = this.selectorPanel.getInstaller();
            if (installer instanceof CustomPlatformInstall) {
                WizardDescriptor.InstantiatingIterator<WizardDescriptor> it = ((CustomPlatformInstall)installer).createIterator();                
                if (it != typeIterator) {
                    updateIterator(it);                    
                }
                return this.typeIterator == null ? false : this.typeIterator.current() != null;
            }
            else {
                return true;
            }            
        }
        else if (panelIndex == 1) {
            WizardDescriptor.InstantiatingIterator typeIt = locationPanel.getInstallerIterator();
            if (typeIt == null) {
                return false;
            } else {
                WizardDescriptor.Panel p = typeIt.current();
                return p != null;
            }            
        } else {
            return this.typeIterator.hasNext();
        }
    }
    
    public boolean hasPrevious() {
        return this.panelIndex != 0 && 
             !(this.panelIndex == 1 && !hasSelectorPanel) && 
             !(this.panelIndex == 3 && !hasSelectorPanel && this.typeIterator != null && !this.typeIterator.hasPrevious());
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wizard = wiz;
        List<GeneralPlatformInstall> installers = InstallerRegistry.getDefault().getAllInstallers();
        if (installers.isEmpty()) {
            //Probably fixed by: #178256
            final Collection<? extends ModuleInfo> infos = Lookup.getDefault().lookupAll(ModuleInfo.class);
            final StringBuilder sb = new StringBuilder("No PlatformInstallFound in Lookup, enabled modules:\n");    //NOI18N
            for (ModuleInfo info : infos) {
                if (info.isEnabled()) {
                    sb.append(info.getDisplayName());
                    sb.append('('); //NOI18N
                    sb.append(info.getCodeName());
                    sb.append(")\n"); //NOI18N
                }
            }
            throw new IllegalStateException(sb.toString());
        } else if (installers.size()>1) {
            panelIndex = 0;
            hasSelectorPanel = true;
        }
        else {
            if (installers.get(0) instanceof CustomPlatformInstall) {
                panelIndex = 3;
                hasSelectorPanel = false;
                this.typeIterator = ((CustomPlatformInstall) installers.get(0)).createIterator();
                if (this.typeIterator == null)
                    throw new NullPointerException ();
            }
            else {
                panelIndex = 1;
                hasSelectorPanel = false;
                this.locationPanel.setPlatformInstall((PlatformInstall) installers.get(0));
            }
        }            
        updatePanelsList(new JComponent[]{((JComponent)current().getComponent())}, this.typeIterator);
        this.wizard.setTitle(NbBundle.getMessage(PlatformInstallIterator.class,"TXT_AddPlatformTitle"));
        panelNumber = 0;
        wizard.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, // NOI18N
            new Integer(panelNumber));
    }
    
    public java.util.Set instantiate() throws IOException {
        return typeIterator.instantiate();
    }
    
    public String name() {
        if (panelIndex == 0) {
            return bundle.getString("TXT_PlatformSelectorTitle");
        }
        else if (panelIndex == 1) {
            return bundle.getString("TXT_PlatformFolderTitle");
        } else {
            return typeIterator.name();
        }
    }
    
    public void nextPanel() {
        if (this.panelIndex == 0) {
            if (this.selectorPanel.getInstallerIterator()  == null) {
                panelIndex = 1;
            }
            else {
                panelIndex = 3;
                if (typeIterator == null)
                    throw new NullPointerException ();
            }
        } else if (panelIndex == 1) {
            panelIndex = 2;
        }
        else {
            typeIterator.nextPanel();
        }
        panelNumber++;
        wizard.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, // NOI18N
            new Integer(panelNumber));
    }
    
    public void previousPanel() {
        if (panelIndex == 1) {
            panelIndex = 0;
        }
        else if (panelIndex == 2) {
            if (typeIterator.hasPrevious()) {
                typeIterator.previousPanel();
            } else {
                panelIndex = 1;
            }
        } else if (panelIndex == 3) {
            if (typeIterator.hasPrevious()) {
                typeIterator.previousPanel();
            } else {
                panelIndex = 0;
            }                
        } 
        panelNumber--;
        wizard.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, // NOI18N
            new Integer(panelNumber));
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        if (this.typeIterator != null)
            typeIterator.uninitialize (wiz);
    }
    
    public void stateChanged(ChangeEvent e) {
        WizardDescriptor.InstantiatingIterator<WizardDescriptor> it;
        if (e.getSource() == this.locationPanel) {
            it = locationPanel.getInstallerIterator();
        }
        else if (e.getSource() == this.selectorPanel) {
            GeneralPlatformInstall installer = this.selectorPanel.getInstaller();
            if (installer instanceof CustomPlatformInstall) {
                it = ((CustomPlatformInstall)installer).createIterator();
            }
            else {
                it = null;
                this.locationPanel.setPlatformInstall ((PlatformInstall)installer);
            }
        }
        else {
            assert false : "Unknown event source";  //NOI18N
            return;
        }        
        if (it != typeIterator) {
            updateIterator(it);
        }
    }
    
    private void updateIterator (final WizardDescriptor.InstantiatingIterator<WizardDescriptor> it) {
        if (this.typeIterator != null) {
            this.typeIterator.uninitialize (this.wizard);
        }        
        if (it != null) {
            it.initialize (this.wizard);
            updatePanelsList(new JComponent[]{
                (JComponent)selectorPanel.getComponent(),
                (JComponent)locationPanel.getComponent(),
                (JComponent)it.current().getComponent(),
            }, it);
        }
        else {
            updatePanelsList(new JComponent[]{
                (JComponent)selectorPanel.getComponent(),
                (JComponent)locationPanel.getComponent()
            }, null);
        }
        typeIterator = it;
        wizard.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(panelNumber)); // NOI18N
    }                
                
}
