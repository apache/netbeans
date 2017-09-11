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

package org.netbeans.spi.tasklist;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.Action;
import org.netbeans.modules.tasklist.trampoline.Accessor;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.openide.filesystems.FileObject;

/**
 * API trampoline
 * 
 * @author S. Aubrecht
 */
class AccessorImpl extends Accessor {
    
    public String getDescription( Task t ) {
        return t.getDescription();
    }

    public FileObject getFile(Task t) {
        return t.getFile();
    }

    public URL getURL(Task t) {
        return t.getURL();
    }

    public TaskGroup getGroup(Task t) {
        return t.getGroup();
    }

    public int getLine(Task t) {
        return t.getLine();
    }
    
    public ActionListener getDefaultAction(Task t) {
        return t.getDefaultAction();
    }

    public Action[] getActions(Task t) {
        return t.getActions();
    }

    public String getDisplayName(TaskScanningScope scope) {
        return scope.getDisplayName();
    }

    public String getDescription(TaskScanningScope scope) {
        return scope.getDescription();
    }

    public Image getIcon(TaskScanningScope scope) {
        return scope.getIcon();
    }
    
    public boolean isDefault( TaskScanningScope scope ) {
        return scope.isDefault();
    }

    public String getDisplayName(FileTaskScanner scanner) {
        return scanner.getDisplayName();
    }

    public String getDescription(FileTaskScanner scanner) {
        return scanner.getDescription();
    }

    public String getOptionsPath(FileTaskScanner scanner) {
        return scanner.getOptionsPath();
    }

    public String getDisplayName(PushTaskScanner scanner) {
        return scanner.getDisplayName();
    }

    public String getDescription(PushTaskScanner scanner) {
        return scanner.getDescription();
    }

    public String getOptionsPath(PushTaskScanner scanner) {
        return scanner.getOptionsPath();
    }

    public TaskScanningScope.Callback createCallback(TaskManager tm, TaskScanningScope scope) {
        return new TaskScanningScope.Callback( tm, scope );
    }

    public FileTaskScanner.Callback createCallback(TaskManager tm, FileTaskScanner scanner) {
        return new FileTaskScanner.Callback( tm, scanner );
    }

    public PushTaskScanner.Callback createCallback(TaskManager tm, PushTaskScanner scanner) {
        return new PushTaskScanner.Callback( tm, scanner );
    }
}
