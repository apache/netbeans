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

package org.netbeans.modules.tasklist.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author S. Aubrecht
 */
public final class ScannerList <T>  {
    
    public static final String PROP_TASK_SCANNERS = "TaskScannerList"; //NOI18N
    
    private static final String SCANNER_LIST_PATH = "TaskList/Scanners"; //NOI18N

    private static ScannerList<FileTaskScanner> fileInstance;
    private static ScannerList<PushTaskScanner> pushInstance;
    
    private PropertyChangeSupport propertySupport;
    
    private Lookup.Result<T> lkpResult;
    
    private List<T> scanners;
    private Class<T> clazz;
    
    /** Creates a new instance of ProviderList */
    private ScannerList( Class<T> clazz ) {
        this.clazz = clazz;
    }

    public static ScannerList<FileTaskScanner> getFileScannerList() {
        if( null == fileInstance ) {
            fileInstance = new ScannerList<FileTaskScanner>( FileTaskScanner.class );
        }
        return fileInstance;
    }

    public static List<FileTaskScanner> getFileScanners(TaskFilter filter) {
        if( null == fileInstance ) {
            fileInstance = new ScannerList<FileTaskScanner>( FileTaskScanner.class );
        }
        if(fileInstance.scanners == null){
           fileInstance.init();
        }
        ArrayList<FileTaskScanner> result = new ArrayList<FileTaskScanner>(fileInstance.scanners.size());
        for( FileTaskScanner scanner : fileInstance.getScanners() ) {
            if( filter.isEnabled(scanner) ) {
                result.add( scanner );
            }
        }
        return result;
    }
    
    public static ScannerList<PushTaskScanner> getPushScannerList() {
        if( null == pushInstance ) {
            pushInstance = new ScannerList<PushTaskScanner>( PushTaskScanner.class );
        }
        return pushInstance;
    }

    public List<? extends T> getScanners() {
        init();
        return scanners;
    }

//    private Class<? extends T> getCheckedClass() {
//        return T.class;
//    }
    
    private void init() {
        if( null == scanners ) {
            if( null == lkpResult ) {
                lkpResult = initLookup();
                lkpResult.addLookupListener( new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                        scanners = null;
                        firePropertyChange();
                    }
                });
            }
            scanners = new ArrayList<T>( lkpResult.allInstances() );
        }
    }
    
    public void addPropertyChangeListener( PropertyChangeListener pcl ) {
        if( null == propertySupport )
            propertySupport = new PropertyChangeSupport( this );
        propertySupport.addPropertyChangeListener( pcl );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener pcl ) {
        if( null != propertySupport )
            propertySupport.removePropertyChangeListener( pcl );
    }
    
    private void firePropertyChange() {
        if( null != propertySupport ) {
            propertySupport.firePropertyChange( PROP_TASK_SCANNERS, null, getScanners() );
        }
    }

    private Lookup.Result<T> initLookup() {
        Lookup lkp = Lookups.forPath( SCANNER_LIST_PATH );
        
        Lookup.Template<T> template = new Lookup.Template<T>( clazz );
        Lookup.Result<T> res = lkp.lookup( template );
        return res;
    }
}
