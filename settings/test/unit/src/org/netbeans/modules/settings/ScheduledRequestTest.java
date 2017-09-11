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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
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

package org.netbeans.modules.settings;

import java.io.IOException;

import org.netbeans.junit.NbTestCase;


import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;

/** JUnit tests
 *
 * @author  Jan Pokorsky
 */
public final class ScheduledRequestTest extends NbTestCase {
    FileSystem fs;

    public ScheduledRequestTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        
        LocalFileSystem lfs = new LocalFileSystem();
        clearWorkDir();
        lfs.setRootDirectory(this.getWorkDir());
        fs = lfs;
    }
    
    public void testSchedule() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        Object obj1 = new Object();
        sr.schedule(obj1);
        assertNotNull("none file lock", sr.getFileLock());
        for (int i = 0; i < 2 && !toRun.finished; i++) {
            Thread.sleep(2500);    
        }
        assertTrue("scheduled request was not performed yet", toRun.finished);        
        assertNull("file is still locked", sr.getFileLock());
    }

    @RandomlyFails // NB-Core-Build #2564
    public void testCancel() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        Object obj1 = new Object();
        sr.schedule(obj1);
        assertNotNull("none file lock", sr.getFileLock());
        sr.cancel();
        assertNull("file lock", sr.getFileLock());
        Thread.sleep(2500);
        assertTrue("scheduled request was performed", !toRun.finished);
        
        Object obj2 = new Object();
        sr.schedule(obj2);
        assertNotNull("none file lock", sr.getFileLock());
        Thread.sleep(2500);
        assertNull("file lock", sr.getFileLock());
        assertTrue("scheduled request was not performed yet", toRun.finished);
    }
    
    public void testForceToFinish() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        Object obj1 = new Object();
        sr.schedule(obj1);
        assertNotNull("none file lock", sr.getFileLock());
        sr.forceToFinish();
        assertTrue("scheduled request was not performed yet", toRun.finished);
        assertNull("file lock", sr.getFileLock());
    }
    
    public void testRunAndWait() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        sr.runAndWait();
        assertTrue("scheduled request was not performed yet", toRun.finished);
        assertNull("file lock", sr.getFileLock());
    }
    
    private static class FSA implements org.openide.filesystems.FileSystem.AtomicAction {
        boolean finished = false;
        public void run() throws IOException {
            finished = true;
        }
        
    }
}
