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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author S. Aubrecht
 */
public final class ScanningScopeList {
    
    public static final String PROP_SCOPE_LIST = "scopeList"; //NOI18N
    
    private static final String SCOPE_LIST_PATH = "TaskList/ScanningScopes"; //NOI18N
    
    private static ScanningScopeList theInstance;
    
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );
    
    private Lookup.Result<TaskScanningScope> lookupRes;
    
    /** Creates a new instance of ScanningScopeList */
    private ScanningScopeList() {
    }
    
    public static ScanningScopeList getDefault() {
        if( null == theInstance ) {
            theInstance = new ScanningScopeList();
        }
        return theInstance;
    }
    
    public void addPropertyChangeListener( PropertyChangeListener pcl ) {
        propertySupport.addPropertyChangeListener( pcl );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener pcl ) {
        propertySupport.removePropertyChangeListener( pcl );
    }
    
    public List<TaskScanningScope> getTaskScanningScopes() {
        if( null == lookupRes ) {
            lookupRes = initLookup();
            lookupRes.addLookupListener( new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    fireScopeListChange();
                }
            });
        }
        return new ArrayList<TaskScanningScope>( lookupRes.allInstances() );
    }
    
    public TaskScanningScope getDefaultScope() {
        List<TaskScanningScope> scopes = getTaskScanningScopes();
        for( TaskScanningScope ss : scopes ) {
            if( Accessor.isDefault( ss ) ) {
                return ss;
            }
        }
        return scopes.isEmpty() ? null : scopes.get( 0 );
    }
    
    private Lookup.Result<TaskScanningScope> initLookup() {
        Lookup lkp = Lookups.forPath( SCOPE_LIST_PATH );
        Lookup.Template<TaskScanningScope> template = new Lookup.Template<TaskScanningScope>( TaskScanningScope.class );
        Lookup.Result<TaskScanningScope> res = lkp.lookup( template );
        return res;
    }
    
    private void fireScopeListChange() {
        propertySupport.firePropertyChange( PROP_SCOPE_LIST, null, getTaskScanningScopes() );
    }
}
