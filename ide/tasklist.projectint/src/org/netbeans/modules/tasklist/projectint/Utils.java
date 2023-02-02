/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tasklist.projectint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Utilities
 * 
 * @author S. Aubrecht
 */
class Utils {
    
    public static final String KEY_STATUS_BAR_LABEL = "StatusBarLabel"; //NOI18N
            
    /** Creates a new instance of Utils */
    private Utils() {
    }

    /**
     * Find files opened in editor so that they can be scanned first to improve user-perceived performance.
     */
    static Collection<FileObject> collectEditedFiles() {
        Collection<TopComponent> comps = new ArrayList<TopComponent>( TopComponent.getRegistry().getOpened() );
        
        HashSet<FileObject> collectedFiles = new HashSet<>( comps.size() );
        
        for( final TopComponent tc : comps ) {
            if( WindowManager.getDefault().isOpenedEditorTopComponent( tc ) ) {
                DataObject dob = tc.getLookup().lookup( DataObject.class );
                if( null != dob ) {
                    FileObject fo = dob.getPrimaryFile();
                    if( null != fo ) {
                        collectedFiles.add( fo );
                    }
                }
            }
        }
        return collectedFiles;
    }
}
