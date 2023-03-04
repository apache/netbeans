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

package org.netbeans.modules.web.jsf.navigation.pagecontentmodel;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 *
 * @author joelle lam
 */
public abstract class PageContentModel {
    /**
     * Returns the current Page Content Items
     * @return 
     */
    public abstract Collection<PageContentItem> getPageContentItems();
    /**
     * addPageContentItem
     * @param pageContentItem 
     */
    public abstract void addPageContentItem(PageContentItem pageContentItem);
    /**
     * remove page content item.
     * @param pageContentItem 
     */
    public abstract void removePageContentItem(PageContentItem pageContentItem);
    
    /**
     * retrive all actions specific to this ndoe 
     * @return actions
     */
    public abstract Action[] getActions();
    
    /**
     * Called when the model is no longer needed.
     * @throws java.io.IOException 
     */
    public abstract void destroy() throws IOException;
    
    private final Set<? extends PageContentItem> pageContentItems = new HashSet<PageContentItem>();
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
    /**
     * Add the change listener.
     * @param l 
     */
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    /**
     * Remove the change listener.
     * @param l 
     */
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    private final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    /**
     * Handle Model Change Event To Update The Page.  This is this time
     * to update your page content items.  
     */
    public final void handleModelChangeEvent() {
        fireChangeEvent();
    }
    
    
    
    
}
