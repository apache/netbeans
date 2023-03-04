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

package org.netbeans.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Action that delegates on delegate action.
 *
 * @author  Miloslav Metelka, Martin Roskanin
 */
public class DelegateAction extends AbstractAction {

    protected Action delegate;
    private PropertyChangeListener pcl;

    public DelegateAction() {
        this(null);
    }

    public DelegateAction(Action delegate) {
        this.delegate = delegate;
        pcl = new PropertyChangeListener(){
             public void propertyChange(PropertyChangeEvent evt){
                 
                 if(evt!=null){
                     if ("enabled".equals(evt.getPropertyName())){ //NOI18N
                         setEnabled(((Boolean)evt.getNewValue()).booleanValue());
                     }else{
                         firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                     }
                 }
             }
        };
    }

    protected final Action getDelegate() {
        return delegate;
    }
    
    protected void setDelegate(Action delegate){
        
        if (this.delegate == delegate) return;
        
        if (delegate == this) throw new IllegalStateException("Cannot delegate on the same action"); // NOI18N
        
        if (this.delegate != null){
            this.delegate.removePropertyChangeListener(pcl);
        }
        if (delegate != null) {
            delegate.addPropertyChangeListener(pcl);
        }

        this.delegate = delegate;
        
        setEnabled((delegate != null) ? delegate.isEnabled() : false);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (delegate != null){
            delegate.actionPerformed(e);
        }
    }
    
    
    public Object getValue(String key) {
        if (delegate != null){
            return delegate.getValue(key);
        }else{
            return super.getValue(key);
        }
    }
    
    
    public void putValue(String key, Object value) {
        if (delegate != null){
            delegate.putValue(key, value);
        }else{
            super.putValue(key, value);
        }
    }
    
    
}
