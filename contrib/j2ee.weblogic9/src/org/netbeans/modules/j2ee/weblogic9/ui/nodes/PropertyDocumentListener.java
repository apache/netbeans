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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;


/**
 * @author ads
 *
 */
class PropertyDocumentListener implements DocumentListener {
    
    PropertyDocumentListener(WLDeploymentManager manager , String property,
            JTextComponent textComponent )
    {
        this.manager = manager;
        propertyName = property;
        component = textComponent;
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    public void changedUpdate( DocumentEvent e ) {
        update();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    public void insertUpdate( DocumentEvent e ) {
        update();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    public void removeUpdate( DocumentEvent e ) {
        update();
    }
    
    private void update( ){
        manager.getInstanceProperties().setProperty( propertyName, 
                component.getText() );
    }

    private WLDeploymentManager manager;
    private String propertyName; 
    private JTextComponent component;
}
