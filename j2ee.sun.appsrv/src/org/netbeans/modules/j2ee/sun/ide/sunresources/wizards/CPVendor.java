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
/*
 * CPVendor.java
 *
 * Created on February 11, 2009
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.openide.WizardDescriptor;

/** 
 * 
 *
 * @author  Nitya Doraisamy
 */
public class CPVendor implements WizardDescriptor.Panel, ChangeListener {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private CPVendorPanel component;
    private ResourceConfigHelper helper;    
    private Wizard wizardInfo;
    private final List listeners = new ArrayList();
    private WizardDescriptor wizDescriptor;
    
    /** Create the wizard panel descriptor. */
    public CPVendor(ResourceConfigHelper helper, Wizard wizardInfo) {
        this.helper = helper;
        this.wizardInfo = wizardInfo;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new CPVendorPanel(this, this.helper, this.wizardInfo);
            component.addChangeListener(this);
        }
        return component;
    }

    private CPVendorPanel getVisual() {
        return (CPVendorPanel) getComponent();
    }

    public String getResourceName() {
        return this.wizardInfo.getName();
    }
    
    public HelpCtx getHelp() {
         return new HelpCtx("AS_Wiz_ConnPool_chooseDB"); //NOI18N
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public Wizard getWizard() {
        return wizardInfo;
    }
    
    /**
     * Checks if the JNDI Name in the wizard is duplicate name in the
     * Unregistered resource list for JDBC Data Sources, Persistenc Managers, 
     * and Java Mail Sessions.
     *
     * @return boolean true if there is a duplicate name.
     * false if not.
     */
    public boolean isValid() {
        boolean value = getVisual().hasValidData();
        return value;
    }
    
    public void readSettings(Object settings) {
        wizDescriptor = (WizardDescriptor) settings;
        getVisual().read(settings);
    }

    public void storeSettings(Object settings) {
        //getVisual().store((AddServerInstanceWizard)settings);
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public void stateChanged(ChangeEvent event) {
        fireChange(event);
    }

    private void fireChange(ChangeEvent event) {
        ArrayList tempList;

        synchronized (listeners) {
            tempList = new ArrayList(listeners);
        }

        Iterator iter = tempList.iterator();
        while (iter.hasNext()) {
            ((ChangeListener)iter.next()).stateChanged(event);
        }
    }

    public void setErrorMsg(String message) {
        if (this.wizDescriptor != null) {
            this.wizDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);    //NOI18N
        }
    }

    public void setErrorMessage(String msg, String value){
        String message = MessageFormat.format(msg, new Object[] {value});
        setErrorMsg(message);
    }

    public void setInitialFocus(){
        getVisual().setInitialFocus();
    }
}

