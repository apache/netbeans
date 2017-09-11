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

package org.netbeans.spi.tasklist;

import java.awt.Image;
import java.util.Map;
import javax.swing.AbstractAction;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * A class that defines the set of resources (files and/or folders) that will be scanned for Tasks.
 * 
 * @author S. Aubrecht
 */
public abstract class TaskScanningScope implements Iterable <FileObject>, Lookup.Provider {
    
    private String displayName;
    private String description;
    private Image icon;
    private boolean isDefault;
    
    /**
     * Create a new instance
     * @param displayName Label for Task List's popup menu
     * @param description Description for tooltips Task List's toolbar
     * @param icon Icon to be displayed in Task List's toolbar
     */
    public TaskScanningScope( String displayName, String description, Image icon ) {
        this( displayName, description, icon, false );
    }
    
    /**
     * Create a new instance
     * @param displayName Label for Task List's popup menu
     * @param description Description for tooltips Task List's toolbar
     * @param icon Icon to be displayed in Task List's toolbar
     * @param isDefault True if this scope should be selected by default when the Task List is opened for the first time.
     */
    public TaskScanningScope( String displayName, String description, Image icon, boolean isDefault ) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.isDefault = isDefault;
    }
    
    /**
     * Display name used for Task List's popup menu, cannot be null.
     * @return Display name used for Task List's popup menu, cannot be null.
     */
    final String getDisplayName() {
        String res = null;
        Map<String,String> labels = getLookup().lookup(Map.class);
        if( null != labels ) {
            res = labels.get(AbstractAction.NAME);
        }
        if( null == res )
            res = displayName;
        return res;
    }
    
    /**
     * Long description (e.g. for tooltip)
     * @return Long description (e.g. for tooltip)
     */
    final String getDescription() {
        String res = null;
        Map<String,String> labels = getLookup().lookup(Map.class);
        if( null != labels ) {
            res = labels.get(AbstractAction.SHORT_DESCRIPTION);
        }
        if( null == res )
            res = description;
        return res;
    }
    
    /**
     * Icon to be displayed in Task List's window toolbar, cannot be null.
     * @return Icon to be displayed in Task List's window toolbar, cannot be null.
     */
    final Image getIcon() {
        return icon;
    }
    
    /**
     * True if this scope should be selected by default when the Task List is opened for the first time.
     * @return True if this scope should be selected by default when the Task List is opened for the first time.
     */
    final boolean isDefault() {
        return isDefault;
    }
    
    /**
     * Check whether the given resource is in this scanning scope.
     * @param resource Resource to be checked.
     * @return True if the given resource is in this scope.
     */
    public abstract boolean isInScope( FileObject resource );
    
    /**
     * Called by the framework when the user switches to this scanning scope.
     * 
     * @param callback 
     */
    public abstract void attach( Callback callback );
    
    /**
     * Lookup with scope's contents.
     * @return Lookup that contains either the {@link org.openide.filesystems.FileObject}s to be scanned (for example when 
     * the scope is 'currently edited file') or {@link org.netbeans.api.project.Project}s that are in this scope.
     */
    public abstract Lookup getLookup();
    
    /**
     * Callback to Task List's framework.
     */
    public static final class Callback {
        private TaskScanningScope scope;
        private TaskManager tm;
        
        Callback( TaskManager tm, TaskScanningScope scope ) {
            this.tm = tm;
            this.scope = scope;
        }
        
        /**
         * Notify the framework that all resources under this scope must be re-scanned.
         */
        public void refresh() {
            tm.refresh( scope );
        }
    }
}
