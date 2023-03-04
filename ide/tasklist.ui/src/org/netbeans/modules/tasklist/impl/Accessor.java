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

package org.netbeans.modules.tasklist.impl;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.Action;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;

/**
 *
 * @author S. Aubrecht
 */
public class Accessor {
    
    static {
        // invokes static initializer of Task.class
        // that will assign value to the DEFAULT field above
        Class c = Task.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
    }
    
    /** Creates a new instance of Accessor */
    private Accessor() {
    }

    public static URL getURL( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getURL( t );
    }

    public static FileObject getFile( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getFile( t );
    }

    public static String getLocation( Task t ) {
        URL url = getURL(t);
        if( null != url ) {
            return url.toString();
        }
        FileObject fo = getFile(t);
        String location = fo.getPath();
        int line = getLine(t);
        if( line >= 0 )
            location += ":" + line;
        return location;
    }

    public static String getPath( Task t ) {
        URL url = getURL(t);
        if( null != url ) {
            return url.toString();
        }
        FileObject fo = getFile(t);
        return fo.getPath();
    }

    public static String getFileNameExt( Task t ) {
        FileObject fo = getFile(t);
        if( null == fo )
            return null;
        return fo.getNameExt();
    }
    
    public static String getDescription( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDescription( t );
    }
    
    public static TaskGroup getGroup( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getGroup( t );
    }
    
    public static int getLine( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getLine( t );
    }

    public static ActionListener getDefaultAction( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDefaultAction( t );
    }

    public static Action[] getActions( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getActions( t );
    }
    
    
    
    public static String getDisplayName( TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDisplayName( scope );
    }
    
    public static String getDescription( TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDescription( scope );
    }
    
    public static Image getIcon( TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getIcon( scope );
    }
    
    public static boolean isDefault( TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.isDefault( scope );
    }
    
    public static TaskScanningScope.Callback createCallback( TaskManager tm, TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.createCallback( tm, scope );
    }
    
    public static TaskScanningScope getEmptyScope() {
        return org.netbeans.modules.tasklist.trampoline.Accessor.getEmptyScope();
    }



    
    public static String getDisplayName( FileTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDisplayName( scanner );
    }
    
    public static String getDescription( FileTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDescription( scanner );
    }
    
    public static String getOptionsPath( FileTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getOptionsPath( scanner );
    }
    
    public static FileTaskScanner.Callback createCallback( TaskManager tm, FileTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.createCallback( tm, scanner );
    }


    
    public static String getDisplayName( PushTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDisplayName( scanner );
    }
    
    public static String getDescription( PushTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDescription( scanner );
    }
    
    public static String getOptionsPath( PushTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getOptionsPath( scanner );
    }
    
    public static PushTaskScanner.Callback createCallback( TaskManager tm, PushTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.createCallback( tm, scanner );
    }
}
