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

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.ErrorManager;

import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public abstract class ResourceWizardPanel implements WizardDescriptor.FinishablePanel, ChangeListener, WizardConstants {

    private final List listeners = new ArrayList();

    /** Default preferred width of the panel - should be the same for all panels within one wizard */
    private static final int DEFAULT_WIDTH = 600;
    /** Default preferred height of the panel - should be the same for all panels within one wizard */
    private static final int DEFAULT_HEIGHT = 390;
   
    public WizardDescriptor wizDescriptor;
    public ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); //NOI18N
    
    public ResourceWizardPanel() { }

    /** @return preferred size of the wizard panel - it should be the same for all panels within one Wizard
    * so that the wizard dialog does not change its size when switching between panels */
    
    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public HelpCtx getHelp() {
        return null; // HelpCtx.DEFAULT_HELP;
    }

    public void stateChanged(ChangeEvent event) {
        fireChange(event);
    }
    
    public void fireChange (Object source) {
        ArrayList lst;

        synchronized (listeners) {
            lst = new ArrayList(listeners);
        }

        ChangeEvent event = new ChangeEvent(source);
        for (int i=0; i< lst.size(); i++){
            ChangeListener listener = (ChangeListener) lst.get(i);
            listener.stateChanged(event);
        }
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
    
    public boolean isFinishPanel() {
        return false;
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
    
    public void readSettings(Object settings) {
        this.wizDescriptor = (WizardDescriptor)settings;
    }
    
    public void storeSettings(Object settings) {
    }
    
    public Wizard getWizardInfo(String dataFile){
        Wizard wizardInfo = null;
        try{
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(dataFile);
            wizardInfo = Wizard.createGraph(in);
        }catch(Exception ex){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return wizardInfo;
    }
}
