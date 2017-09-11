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

package org.openide.loaders;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.filesystems.*;
import org.openide.cookies.*;
import org.openide.util.*;

import org.netbeans.junit.*;
import org.openide.actions.OpenAction;
import org.openide.actions.SaveAction;

public class FolderInstanceTaskOrderTest extends NbTestCase {
    private Logger err;

    public FolderInstanceTaskOrderTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir ();
        
        err = Logger.getLogger("test." + getName());
        err.info("setUp over: " + getName());
    }

    @RandomlyFails // NB-Core-Build #3078
    public void testReorderingOfExecutionTasksIsOK() throws Exception {
        String[] names = {
            "folder/"
        };
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), names);
        FileObject folder = lfs.findResource("folder");
        DataFolder f = DataFolder.findFolder(folder);

        InstanceDataObject.create(f, null, SaveAction.class);

        err.info("Creating InvCheckFolderInstance");
        ReorderTasksCheck instances = new ReorderTasksCheck(f);
        err.info("Computing result");
        instances.waitFinished(500);
        assertEquals("One task scheduled", 1, instances.tasks.size());

        InstanceDataObject.create(f, null, OpenAction.class);

        instances.waitFinished(500);
        assertEquals("Two tasks scheduled", 2, instances.tasks.size());

        // run in reverse order
        instances.tasks.get(1).run();
        instances.tasks.get(0).run();

        List computed = (List)instances.instanceCreate();
        err.info("Result is here: " + computed);
        assertEquals("Two actions", 2, computed.size());
    }

    public void testRunImmediatelly() throws Exception {
        String[] names = {
            "folder/"
        };
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), names);
        FileObject folder = lfs.findResource("folder");
        DataFolder f = DataFolder.findFolder(folder);

        InstanceDataObject.create(f, null, SaveAction.class);

        err.info("Creating InvCheckFolderInstance");
        RunImmediatelly instances = new RunImmediatelly(f);
        err.info("Computing result");
        List computed = (List)instances.instanceCreate();
        assertEquals("One action", 1, computed.size());

        InstanceDataObject.create(f, null, OpenAction.class);
        computed = (List)instances.instanceCreate();
        err.info("Result is here: " + computed);
        assertEquals("Two actions", 2, computed.size());
    }

    private final class ReorderTasksCheck extends FolderInstance {
        List<Task> tasks = new ArrayList<Task>();

        public ReorderTasksCheck(DataFolder f) {
            super(f);
        }
        
        protected Object createInstance(InstanceCookie[] cookies) throws IOException, ClassNotFoundException {
            ArrayList list = new ArrayList();
            for (int i = 0; i < cookies.length; i++) {
                list.add(cookies[i].instanceCreate());
            }
            return list;
        }
        @Override
        protected Task postCreationTask (Runnable run) {
            Task t = new Task(run);
            tasks.add(t);
            return t;
        }
    }

    private final class RunImmediatelly extends FolderInstance {
        public RunImmediatelly(DataFolder f) {
            super(f);
        }

        protected Object createInstance(InstanceCookie[] cookies) throws IOException, ClassNotFoundException {
            ArrayList list = new ArrayList();
            for (int i = 0; i < cookies.length; i++) {
                list.add(cookies[i].instanceCreate());
            }
            return list;
        }
        @Override
        protected Task postCreationTask (Runnable run) {
            run.run();
            return new FinishedTask();
        }

    }
    private static final class FinishedTask extends Task {
        public FinishedTask() {
            notifyFinished();
        }
    }
}
