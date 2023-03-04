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
package org.netbeans.modules.web.client.rest.wizard;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;


/**
 * @author ads
 *
 */
public class HtmlPanel implements Panel<WizardDescriptor> {
    
    public static final String HTML_FILE = "html-file";     // NOI18N
    
    static final String PROP_DOCUMENT_BASE ="document_base"; // NOI18N
    static final String PUBLIC_HTML ="public_html"; // NOI18N
    
    HtmlPanel(WizardDescriptor descriptor){
        myDescriptor = descriptor;
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
            myComponent = new HtmlPanelVisual( this );
        }
        return myComponent;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getHelp()
     */
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(JSClientIterator.HELP_ID);
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#isValid()
     */
    @Override
    public boolean isValid() {
        if ( myComponent != null ){
            return myComponent.valid();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#readSettings(java.lang.Object)
     */
    @Override
    public void readSettings( WizardDescriptor descriptor ) {
        myComponent.read( descriptor );
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener( ChangeListener listener ) {
        myListeners.remove( listener );
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#storeSettings(java.lang.Object)
     */
    @Override
    public void storeSettings( WizardDescriptor descriptor ) {
        myComponent.store( descriptor );
    }
    
    WizardDescriptor getDescriptor(){
        return myDescriptor;
    }
    
    void fireChangeEvent(){
        for( ChangeListener listener : myListeners ){
            listener.stateChanged( new ChangeEvent(this));
        }
    }
    
    
    private final List<ChangeListener> myListeners = new CopyOnWriteArrayList<ChangeListener>();
    private HtmlPanelVisual myComponent;
    private final WizardDescriptor myDescriptor;
}
