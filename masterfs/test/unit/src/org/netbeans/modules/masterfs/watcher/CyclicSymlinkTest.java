/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.masterfs.watcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/** Check behavior of symlinks.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CyclicSymlinkTest extends NbTestCase implements FileChangeListener {
    private int cnt;
    private File lnk;

    public CyclicSymlinkTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        if (lnk != null) {
            lnk.delete();
        }
    }
    
    
    
    public void testCyclicSymlink() throws Exception {
        if (Utilities.isWindows()) {
            return;
        }
        clearWorkDir();
        assertCyclic(getWorkDir());
    }

    public void testCyclicSymlinkInASymlink() throws Exception {
        if (Utilities.isWindows()) {
            return;
        }
        clearWorkDir();
        
        File one = new File(getWorkDir(), "one");
        File two = new File(one, "two");
        File three = new File(two, "three");
        three.mkdirs();
        assertExec("Symlink created OK", makeSymlink(two, getWorkDir()));
        
        File l = new File(new File(getWorkDir(), "lnk"), "three");
        assertTrue("Link exists", l.exists());
        assertTrue("Link is directory", l.isDirectory());
        
        assertCyclic(l);
    }

    public void testCyclicSymlinkOnASymlink() throws Exception {
        if (Utilities.isWindows()) {
            return;
        }
        clearWorkDir();
        
        File one = new File(getWorkDir(), "one");
        File two = new File(one, "two");
        File three = new File(two, "three");
        three.mkdirs();
        assertExec("Symlink is OK", makeSymlink(three, getWorkDir()));
        
        File l = new File(getWorkDir(), "lnk");
        assertTrue("Link exists", l.exists());
        assertTrue("Link is directory", l.isDirectory());
        
        assertCyclic(l);
    }

    public void testAcyclicSymlink() throws Exception {
        if (Utilities.isWindows()) {
            return;
        }
        clearWorkDir();
        assertAcyclic(getWorkDir());
    }

    public void testAcyclicSymlinkInASymlink() throws Exception {
        if (Utilities.isWindows()) {
            return;
        }
        clearWorkDir();
        final File wd = getWorkDir();
        doAcyclicTesting(wd);
    }
    public void testAcyclicSymlinkInASymlinkInASpace() throws Exception {
        if (Utilities.isWindows()) {
            return;
        }
        clearWorkDir();
        final File wd = new File(getWorkDir(), "space in path");
        wd.mkdirs();
        doAcyclicTesting(wd);
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        cnt++;
    }

    @Override
    public void fileChanged(FileEvent fe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fileDeleted(FileEvent fe) {
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void assertCyclic(final File root) throws Exception {
        File one = new File(root, "one");
        File two = new File(one, "two");
        File three = new File(two, "three");
        StringBuilder up = new StringBuilder("../..");
        lnk = new File(three, "lnk");
        three.mkdirs();
        assertExec("Created OK", makeSymlink(up.toString(), three));
        assertTrue("It is directory", lnk.isDirectory());
        
        FileUtil.addRecursiveListener(this, one);
        
        File newTxt = new File(two, "new.txt");
        newTxt.createNewFile();

        FileUtil.toFileObject(two).getFileSystem().refresh(true);

        assertEquals("One data created event", 1, cnt);
    }

    private void assertAcyclic(final File root) throws Exception {
        File one = new File(root, "one");
        File independent = new File(root, "independent");
        File two = new File(one, "two");
        File three = new File(two, "three");
        lnk = new File(three, "lnk");
        three.mkdirs();
        independent.mkdirs();
        
        assertExec("Symlink is OK", makeSymlink( independent, three));
        assertTrue("It is directory", lnk.isDirectory());
        
        FileUtil.addRecursiveListener(this, one);
        File newTxt = new File(independent, "new.txt");
        newTxt.createNewFile();

        FileUtil.toFileObject(two).getFileSystem().refresh(true);

        assertEquals("One data created event", 1, cnt);
    }

    private Process makeSymlink(File orig, File where) throws IOException {
        return makeSymlink(orig.getPath(), where);
    }
    private Process makeSymlink(String orig, File where) throws IOException {
        final String[] exec = { "/bin/ln", "-s", orig,  "lnk" };
        try {
            return Runtime.getRuntime().exec(exec, null, where);
        } catch (IOException ex) {
            Exceptions.attachMessage(ex, "cmd: " + Arrays.toString(exec) + " at: " + where);
            throw ex;
        }
    }

    private void assertExec(String msg, Process proc) throws Exception {
        proc.waitFor();
        final int ev = proc.exitValue();
        if (ev == 0) {
            return;
        }
        fail(msg + " exit value: " + ev + "\n" + toString(proc.getInputStream()) + "\n" + toString(proc.getErrorStream()));
    }
    
    private static StringBuilder toString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            if (is.available() == 0) {
                return sb;
            }
            sb.append((char)is.read());
        }
    }

    private void doAcyclicTesting(final File wd) throws Exception {
        File one = new File(wd, "one");
        File two = new File(one, "two");
        File three = new File(two, "three");
        three.mkdirs();
        assertTrue("Directory two created", two.isDirectory());
        assertExec("Symlink created OK", makeSymlink(two, wd));
        
        File l = new File(new File(wd, "lnk"), "three");
        assertTrue("Link exists", l.exists());
        assertTrue("Link is directory", l.isDirectory());
        assertAcyclic(l);
    }
}
