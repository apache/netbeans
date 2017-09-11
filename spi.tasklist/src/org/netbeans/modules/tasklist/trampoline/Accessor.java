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

