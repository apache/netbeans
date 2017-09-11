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
package org.netbeans.modules.masterfs.filebasedfs;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.Action;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class RecursiveValidityTest extends NbTestCase {
    private FileObject root;
    private FileObject next;
    private File rf;

    public RecursiveValidityTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        rf = new File(getWorkDir(), "wd");
        next = FileUtil.toFileObject(recreateFolders(rf));
        root = FileUtil.toFileObject(rf);

        MockServices.setServices(AP.class);
        AP.il = new IL();
    }

    public void testConsistencyWhenDeletingRoot() throws Exception {
        assertTrue("Is valid", root.isValid());
        assertTrue("Is valid leaf", next.isValid());

        clearWorkDir();
        assertFalse("Root file is gone", rf.exists());

        root.refresh();

        assertFalse("Became invalid", root.isValid());
        assertFalse("Leaf is invalid as well", next.isValid());
        
    }

    public void testConsistencyWhenDeletingRootAndRecreatingInMiddle() throws Exception {
        assertTrue("Is valid", root.isValid());
        assertTrue("Is valid leaft", next.isValid());
        
        FileObject ch1 = root.getChildren()[0];

        clearWorkDir();
        assertFalse("Root file is gone", rf.exists());

        cnt = 5;
        root.refresh();

        assertFalse("Whole tree invalidated", root.isValid());
        assertFalse("Leaf invalidated too", next.isValid());
        assertFalse("But the first child of root is certainly gone", ch1.isValid());
    }

    int cnt;
    final void assertValidity() {
        if (next.isValid()) {
            FileObject test = next;
            do {
                test = test.getParent();
                if (!next.isValid()) {
                    break;
                }
                assertTrue("Leaf is valid" + next + " and thus " + test + " has to be too", test.isValid());
            } while (test != root);
        }
        if (--cnt == 0) {
            try {
                recreateFolders(rf);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    private File recreateFolders(File from) throws IOException {
        File r = from;
        for (int i = 0; i < 10; i++) {
            r = new File(r, "i" + i);
        }
        r.getParentFile().mkdirs();
        r.createNewFile();
        return r;
    }
    
    class IL extends ProvidedExtensions {
        @Override
        public void beforeCreate(FileObject parent, String name, boolean isFolder) {
            assertValidity();
        }

        @Override
        public void createSuccess(FileObject fo) {
            assertValidity();
        }

        @Override
        public void createFailure(FileObject parent, String name, boolean isFolder) {
            assertValidity();
        }

        @Override
        public void beforeDelete(FileObject fo) {
            assertValidity();
        }

        @Override
        public void deleteSuccess(FileObject fo) {
            assertValidity();
        }

        @Override
        public void deleteFailure(FileObject fo) {
            assertValidity();
        }

        @Override
        public void createdExternally(FileObject fo) {
            assertValidity();
        }

        @Override
        public void deletedExternally(FileObject fo) {
            assertValidity();
        }

        @Override
        public void moveSuccess(FileObject from, File to) {
            assertValidity();
        }

        @Override
        public void moveFailure(FileObject from, File to) {
            assertValidity();
        }

        @Override
        public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
            assertValidity();
            return super.refreshRecursively(dir, lastTimeStamp, children);
        }
        
    }
    
    public static final class AP extends BaseAnnotationProvider {
        static InterceptionListener il;
        
        @Override
        public String annotateName(String name, Set<? extends FileObject> files) {
            return name;
        }


        @Override
        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            return name;
        }

        @Override
        public InterceptionListener getInterceptionListener() {
            return il;
        }
        
    }
}
