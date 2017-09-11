/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.NbBundle;

import static org.netbeans.modules.csl.core.Bundle.*;

/**
 * This class provides access to tasklist settings. The settings are only available
 * to push scanners, so this 'push scanner' is a singleton, which can be queried for 
 * settings, but really does not push any tasks to the tasklist. 
 * <p/>
 * Be aware that the scope can change to null at any time.
 * @author sdedic
 */
@NbBundle.Messages({
    "DN_tlIndexerName=Hints-based tasks",
    "DESC_tlIndexerName=Tasks provided by language hints"
})
class TasklistStateBackdoor extends PushTaskScanner {
    private static final TasklistStateBackdoor INSTANCE = new TasklistStateBackdoor();
    
    private volatile TaskScanningScope scope;
    private volatile Callback callback;
    private volatile boolean seenByTlIndexer = true;
    private boolean wasScanning;
    
    TasklistStateBackdoor() {
        super(DN_tlIndexerName(), DESC_tlIndexerName(), null);
    }
    
    static TasklistStateBackdoor getInstance() {
        return INSTANCE;
    }
    
    boolean isCurrentEditorScope() {
        Callback c = this.callback;
        seenByTlIndexer = true;
        return c != null && c.isCurrentEditorScope();
    }
    
    boolean isObserved() {
        Callback c = this.callback;
        seenByTlIndexer = true;
        return c != null && c.isObserved();
    }
    
    TaskScanningScope getScope() {
        return scope;
    }
    
    @Override
    public void setScope(TaskScanningScope scope, Callback callback) {
        this.callback = callback;
        if (scope == null) {
            // ignore; for example project switch when ctx menu is displayed
            // sets scope to null, then back to the project scope to force refresh/reload
            return;
        }
        synchronized (this) {
            boolean newScanning = !callback.isCurrentEditorScope();
            if (!callback.isObserved()) {
                scope = null;
                newScanning = false;
            }
            this.scope = scope;
            if (!callback.isObserved() || callback.isCurrentEditorScope() || 
                    !newScanning || wasScanning == newScanning || !seenByTlIndexer) {
                wasScanning = newScanning;
                seenByTlIndexer = false;
                return;
            }
            wasScanning = newScanning;
            seenByTlIndexer = false;
            IndexingManager.getDefault().refreshAllIndices(TLIndexerFactory.INDEXER_NAME);
        }
    }
}
