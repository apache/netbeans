/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
