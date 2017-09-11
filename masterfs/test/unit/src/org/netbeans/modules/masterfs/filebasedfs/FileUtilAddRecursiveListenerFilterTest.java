/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FileUtilAddRecursiveListenerFilterTest extends NbTestCase
implements FileChangeListener {
    private FileObject root;
    private final List<FileEvent> events = new ArrayList<FileEvent>();
    @SuppressWarnings("NonConstantLogger")
    private Logger LOG;

    public FileUtilAddRecursiveListenerFilterTest(String n) {
        super(n);
    }

    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
        
        root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("Root found", root);

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                root.createData("" + i, "txt");
            } else {
                root.createFolder("" + i);
            }
        }
    }

    public void testAddListenerGetsFiveCallbacks() throws IOException {
        class AtMostFive implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                assertTrue("It is folder", pathname.isDirectory());
                int number = Integer.parseInt(pathname.getName());
                return number <= 5;
            }
            
        }
        
        FileUtil.addRecursiveListener(this, getWorkDir(), new AtMostFive(), null);

        File fifthChild = new File(new File(getWorkDir(), "5"), "new.5.txt");
        assertTrue(fifthChild.createNewFile());
        FileUtil.refreshFor(getWorkDir());
        assertEquals("One event delivered: " + events, 1, events.size());
        
        File seventhChild = new File(new File(getWorkDir(), "7"), "new.7.txt");
        assertTrue(seventhChild.createNewFile());
        FileUtil.refreshFor(getWorkDir());
        assertEquals("No other even delivered: " + events, 1, events.size());
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        LOG.log(Level.INFO, "fileFolderCreated: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        addEventToList(fe);
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        LOG.log(Level.INFO, "fileDataCreated: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        addEventToList(fe);
    }

    @Override
    public void fileChanged(FileEvent fe) {
        LOG.log(Level.INFO, "fileChanged: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        addEventToList(fe);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        LOG.log(Level.INFO, "fileDeleted: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        addEventToList(fe);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        LOG.log(Level.INFO, "fileRenamed: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        addEventToList(fe);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        LOG.log(Level.INFO, "fileAttributeChanged: {0}", fe.getFile());
        LOG.log(Level.INFO, "Thread dump", new Exception());
        addEventToList(fe);
    }

    private void addEventToList(FileEvent fe) {
        // Ignore changes in root itself, e.g. modifications of the local
        // log file.
        if (!fe.getSource().equals(root)) {
            events.add(fe);
        }
    }
}
