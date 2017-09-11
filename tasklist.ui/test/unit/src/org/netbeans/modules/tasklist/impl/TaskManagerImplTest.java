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

import java.util.Collections;
import java.util.Iterator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner.Callback;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class TaskManagerImplTest extends NbTestCase {
    
    public TaskManagerImplTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 300000;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workDir);
        
        file1 = workDir.createData("file1.txt");
        super.setUp();
    }

    private FileObject file1;
    
    /**IZ #100463
     */
    public void testProviderCanStartImmediately() throws Exception {
        final PushTaskScanner scanner = new PushTaskScanner("", "", null) {
            public void setScope(TaskScanningScope scope, Callback callback) {
                callback.started();
                callback.setTasks(file1, Collections.singletonList(Task.create(file1, "unknown", "x", 2)));
                callback.finished();
            }
        };
        
        TaskManagerImpl impl = new TaskManagerImpl() {
            @Override
            Iterable<? extends FileTaskScanner> getFileScanners() {
                return Collections.<FileTaskScanner>emptyList();
            }

            @Override
            Iterable<? extends PushTaskScanner> getPushScanners() {
                return Collections.singletonList(scanner);
            }
        };
        
        impl.observe(new TaskScanningScope("", "", null) {
            public boolean isInScope(FileObject resource) {
                return (resource == file1);
            }
            
            public void attach(Callback callback) {
            }
            
            public Lookup getLookup() {
                return Lookups.singleton(file1);
            }
            
            public Iterator<FileObject> iterator() {
                return Collections.singletonList(file1).iterator();
            }
        }, TaskFilter.EMPTY);

        impl._waitFinished();
        
        assertEquals(1, impl.getTasks().getTasks().size());
    }

    @RandomlyFails // NB-Core-Build #1732: impl._waitFinished() hangs
    public void testProviderCanRemoveTasks() throws Exception {
        final Callback[] cb = new Callback[1];
        final PushTaskScanner scanner = new PushTaskScanner("", "", null) {
            public void setScope(TaskScanningScope scope, Callback callback) {
                cb[0] = callback;
                callback.started();
                callback.setTasks(file1, Collections.singletonList(Task.create(file1, "unknown", "x", 2)));
                callback.finished();
            }
        };
        
        TaskManagerImpl impl = new TaskManagerImpl() {
            @Override
            Iterable<? extends FileTaskScanner> getFileScanners() {
                return Collections.<FileTaskScanner>emptyList();
            }
            
            @Override
            Iterable<? extends PushTaskScanner> getPushScanners() {
                return Collections.singletonList(scanner);
            }
        };
        
        impl.observe(new TaskScanningScope("", "", null) {
            public boolean isInScope(FileObject resource) {
                return (resource == file1);
            }
            
            public void attach(Callback callback) {
            }
            
            public Lookup getLookup() {
                return Lookups.singleton(file1);
            }
            
            public Iterator<FileObject> iterator() {
                return Collections.singletonList(file1).iterator();
            }
        }, TaskFilter.EMPTY);
        
        impl._waitFinished();
        
        assertEquals(1, impl.getTasks().getTasks().size());

        cb[0].started();
        cb[0].setTasks(file1, Collections.<Task>emptyList());
        cb[0].finished();
        
        assertTrue(impl.getTasks().getTasks().isEmpty());
    }
    
}
