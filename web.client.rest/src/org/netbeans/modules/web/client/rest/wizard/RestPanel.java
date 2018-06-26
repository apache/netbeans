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
package org.netbeans.modules.web.client.rest.wizard;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class RestPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> 
{
    static enum JsUi {
        NO_UI(NbBundle.getMessage(RestPanel.class, "TXT_NoUi")),                // NOI18N
        TABLESORTER(NbBundle.getMessage(RestPanel.class, "TXT_TablesorterUi")); // NOI18N
        
        private JsUi( String displayName ){
            myDisplayName = displayName;
        }
        
        @Override
        public String toString(){
            return myDisplayName;
        }
        private String myDisplayName;
    }
    
    public final static String FILE_NAME = "js-file-name";                // NOI18N
    public final static String ADD_BACKBONE = "backbone";                 // NOI18N
    public final static String EXISTED_BACKBONE ="existed-backbone";      // NOI18N
    public final static String EXISTED_UNDERSCORE ="existed-underscore";  // NOI18N
    public final static String EXISTED_JQUERY ="existed-jquery";          // NOI18N
    public final static String UI ="ui";                                  // NOI18N    
    
    
    RestPanel(WizardDescriptor descriptor) {
        myWizard = descriptor;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener( ChangeListener listener ) {
        myListeners.add( listener );
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getComponent()
     */
    @Override
    public Component getComponent() {
        if ( myComponent == null ){
            myComponent = new RestPanelVisual(this);
        }
        return myComponent;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getHelp()
     */
    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#isValid()
     */
    @Override
    public boolean isValid() {
        return myComponent.valid(myWizard);
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#readSettings(java.lang.Object)
     */
    @Override
    public void readSettings( WizardDescriptor wizard ) {
        myWizard = wizard;
        myComponent.read(wizard);
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener( ChangeListener listener ) {
        myListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#storeSettings(java.lang.Object)
     */
    @Override
    public void storeSettings( WizardDescriptor wizard ) {
        myComponent.store(wizard);
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.FinishablePanel#isFinishPanel()
     */
    @Override
    public boolean isFinishPanel() {
        return true;
    }

    void fireChangeEvent(){
        ChangeEvent event = new ChangeEvent(this);
        for( ChangeListener listener : myListeners ){
            listener.stateChanged(event);
        }
    }
    
    Node getRestNode(){
        return myComponent.getRestNode();
    }
    
    WizardDescriptor getDescriptor(){
        return myWizard;
    }
    
    private RestPanelVisual myComponent;
    private List<ChangeListener> myListeners = new CopyOnWriteArrayList<ChangeListener>();
    private WizardDescriptor myWizard;

}
