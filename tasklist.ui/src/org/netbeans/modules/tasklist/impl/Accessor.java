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
