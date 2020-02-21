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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.asm.core.ui.top;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.modules.cnd.asm.core.dataobjects.AsmDataObject;
import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.cnd.asm.model.AsmModel;

final class NavigatorTopComponent extends TopComponent implements LookupListener {
    
    private static NavigatorTopComponent instance;

    static final String ICON_PATH = "org/netbeans/modules/cnd/asm/core/resources/asm_icon.png"; // NOI18N
    
    private static final String PREFERRED_ID = "NavigatorTopComponent"; // NOI18N
    
    //private NavigatorTab []tabs;
    
    private final RegisterUsagesPanel regUsagePanel;
    
    /** label signalizing no available providers */
    private final JLabel notAvailLbl = new JLabel(
            NbBundle.getMessage(NavigatorTopComponent.class, "MSG_NotAvailable")); //NOI18N
            
    private NavigatorTopComponent() {
        initComponents();
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        setName(NbBundle.getMessage(NavigatorTopComponent.class, "CTL_NavigatorTopComponent")); // NOI18N
        //setToolTipText(NbBundle.getMessage(NavigatorTopComponent.class, "HINT_NavigatorTopComponent"));
        //mainTabbedPanel.setVisible(false);
        
        regUsagePanel = RegisterUsagesPanel.getInstance();
        
        // Copied from the default NavigatorTC
        notAvailLbl.setHorizontalAlignment(SwingConstants.CENTER);
        notAvailLbl.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        notAvailLbl.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        // to ensure our background color will have effect
        notAvailLbl.setOpaque(true);
        
        showPanel(false);
       
        /*tabs = new NavigatorTab[] {      
            RegisterUsagesPanel.getInstance()
        };
         
        for (NavigatorTab tab : tabs) {
            mainTabbedPanel.add(tab.getName(), tab.getPanel());
        } */               
    }
    
    private void showPanel(boolean show) {
        if (show) {
            remove(notAvailLbl);
            add(regUsagePanel, BorderLayout.CENTER);
        } else {
            if (notAvailLbl.isShowing()) {
                // already empty
                return;
            }
            remove(regUsagePanel);
            add(notAvailLbl, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized NavigatorTopComponent getDefault() {
        if (instance == null) {
            instance = new NavigatorTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the NavigatorTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized NavigatorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find Navigator component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof NavigatorTopComponent) {
            return (NavigatorTopComponent)win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
          
    private Lookup.Result<AsmDataObject> lookupResult;

    @Override
    protected void componentClosed() {
        super.componentClosed();
        regUsagePanel.closed();
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        regUsagePanel.opened();
    }
    
    @Override
    public void componentShowing() {        
        lookupResult = Utilities.actionsGlobalContext().lookup(new Lookup.Template<AsmDataObject>(AsmDataObject.class));
        lookupResult.addLookupListener (this);
        resultChanged (null);
    }
    
    @Override
    public void componentHidden() {      
        lookupResult.removeLookupListener(this);
        showPanel(false);
        //mainTabbedPanel.setVisible(false);
        lookupResult = null;
        setActivatedNodes(new Node[0]);
    }

    
    public void resultChanged(LookupEvent lookupEvent) {
        Collection <? extends DataObject> objs = lookupResult.allInstances();
        DataObject dob = objs.isEmpty() ? null : objs.iterator().next();  
        
        if (dob == null) {
            setActivatedNodes(new Node[0]);            
            //mainTabbedPanel.setVisible(false);
            showPanel(false);
            return;                         
        }
        
        setActivatedNodes(new Node[] {dob.getNodeDelegate()});
        
        AsmModel model = AsmObjectUtilities.getModel(dob);
        
        if (model != null) {
            addPanelsForModel(dob);
        }        
    }    
    
    private void addPanelsForModel(DataObject dob) {      
        //mainTabbedPanel.setVisible(true);
        showPanel(true);
        regUsagePanel.setDocument(dob);
        
        /*for (NavigatorTab tab : tabs) {
            tab.setDocument(dob);
        }*/
    }
    
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    @Override
    public String preferredID() {
        return PREFERRED_ID;
    }
                
    static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return NavigatorTopComponent.getDefault();
        }
    }    
}
