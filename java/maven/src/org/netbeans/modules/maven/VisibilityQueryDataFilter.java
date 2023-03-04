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
package org.netbeans.modules.maven;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataObject;

/**
 *
 * @author  Milos Kleint
 */

    
public final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {

    public static final VisibilityQueryDataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();

    EventListenerList ell = new EventListenerList();        
    
    @SuppressWarnings("LeakingThisInConstructor")
    public VisibilityQueryDataFilter() {
        VisibilityQuery.getDefault().addChangeListener(this);
    }
            
    @Override
    public boolean acceptDataObject(DataObject obj) {                
        FileObject fo = obj.getPrimaryFile();                
        return VisibilityQuery.getDefault().isVisible(fo);
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {            
        Object[] listeners = ell.getListenerList();     
        ChangeEvent event = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == ChangeListener.class) {             
                if (event == null) {
                    event = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i+1]).stateChanged(event);
            }
        }
    }        
    
    @Override
    public void addChangeListener(ChangeListener listener) {
        ell.add(ChangeListener.class, listener);
    }        
                    
    @Override
    public void removeChangeListener(ChangeListener listener) {
        ell.remove(ChangeListener.class, listener);
    }
    
}
