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


package org.netbeans.core.windows;

import java.util.Set;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instance of this class keeps list of (weak references to) recently activated TopComponents.
 *
 * @author  Marek Slama
 */
final class RecentViewList implements PropertyChangeListener {

    /** List of TopComponent IDs. First is most recently
     * activated. */
    private List<String> tcIdList = new ArrayList(20);
    private Map<String, Reference<TopComponent>> tcCache = new HashMap<String, Reference<TopComponent>>(20);

    public RecentViewList (WindowManager wm) {
        // Starts listening on Registry to be notified about activated TopComponent.
        wm.getRegistry().addPropertyChangeListener(this);
    }


    /** Used to get array for view and for persistence */
    public TopComponent [] getTopComponents() {
        List<TopComponent> tcList = new ArrayList<TopComponent>(tcIdList.size());
        WindowManager wm = WindowManager.getDefault();
        for (int i = 0; i < tcIdList.size(); i++) {
            String tcId = tcIdList.get( i );
            TopComponent tc = null;
            Reference<TopComponent> ref = tcCache.get( tcId );
            if( null != ref ) {
                tc = ref.get();
            }
            if( null == tc ) {
                tc = wm.findTopComponent( tcId );
                if( null != tc )
                    tcCache.put( tcId, new WeakReference<TopComponent>( tc ) );
            }
            if ((tc != null) && tc.isOpened()) {
                tcList.add(tc);
            }
        }
        return tcList.toArray(new TopComponent[tcList.size()]);
    }

    public String [] getTopComponentIDs() {
        return tcIdList.toArray(new String[tcIdList.size()]);
    }

    /** Used to set initial values from persistence */
    public void setTopComponents(String[] tcIDs) {
        tcIdList.clear();
        tcIdList.addAll( Arrays.asList( tcIDs ) );
        tcCache.clear();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) evt.getNewValue();
            if (tc != null) {
                String tcId = WindowManager.getDefault().findTopComponentID( tc );
                //Update list
                tcIdList.remove( tcId );
                tcIdList.add( 0, tcId );
                // #69486: ensure all components are listed
                fillList(TopComponent.getRegistry().getOpened());
            }
        }
    }

    /** Fills list of weak references with TCs that are in given
     * input list but are not yet contained in list of weak references.
     */
    private void fillList(Set<TopComponent> openedTCs) {
        tcCache.clear();
        WindowManager wm = WindowManager.getDefault();
        for (TopComponent curTC: openedTCs) {
            String id = wm.findTopComponentID( curTC );
            if( !tcIdList.contains( id ) ) {
                if( tcIdList.size() > 1 )
                    tcIdList.add( 1, id );
                else
                    tcIdList.add( id );
            }
            tcCache.put( id, new WeakReference<TopComponent>( curTC ) );
        }
    }

}
