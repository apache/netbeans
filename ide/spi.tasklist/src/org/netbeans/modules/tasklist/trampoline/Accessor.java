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

package org.netbeans.modules.tasklist.trampoline;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.Action;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;


/**
 * API trampoline
 * 
 * @author S. Aubrecht
 */
public abstract class Accessor {
    
    public static Accessor DEFAULT;
    
    static {
        // invokes static initializer of Item.class
        // that will assign value to the DEFAULT field above
        Class c = Task.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
//        assert DEFAULT != null : "The DEFAULT field must be initialized";
    }
    
    public abstract String getDescription( Task t );
    
    public abstract FileObject getFile( Task t );

    public abstract URL getURL( Task t );
    
    public abstract TaskGroup getGroup( Task t );
    
    public abstract int getLine( Task t );
    
    public abstract ActionListener getDefaultAction( Task t );

    public abstract Action[] getActions( Task t );
    
    
    public abstract String getDisplayName( TaskScanningScope scope );
    
    public abstract String getDescription( TaskScanningScope scope );
    
    public abstract Image getIcon( TaskScanningScope scope );
    
    public abstract boolean isDefault( TaskScanningScope scope );
    
    public abstract TaskScanningScope.Callback createCallback( TaskManager tm, TaskScanningScope scope );
    
    
    public abstract String getDisplayName( FileTaskScanner scanner );
    
    public abstract String getDescription( FileTaskScanner scanner );
    
    public abstract String getOptionsPath( FileTaskScanner scanner );
    
    public abstract FileTaskScanner.Callback createCallback( TaskManager tm, FileTaskScanner scanner );
    
    
    public abstract String getDisplayName( PushTaskScanner scanner );
    
    public abstract String getDescription( PushTaskScanner scanner );
    
    public abstract String getOptionsPath( PushTaskScanner scanner );
    
    public abstract PushTaskScanner.Callback createCallback( TaskManager tm, PushTaskScanner scanner );

    private static TaskScanningScope EMPTY_SCOPE = null;
    public static TaskScanningScope getEmptyScope() {
        if( null == EMPTY_SCOPE )
            EMPTY_SCOPE = new EmptyScanningScope();
        return EMPTY_SCOPE;
    }
}

