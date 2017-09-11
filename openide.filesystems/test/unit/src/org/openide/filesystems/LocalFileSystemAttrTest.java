/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LocalFileSystemAttrTest extends NbTestCase {
    private LFS testedFS;

    public LocalFileSystemAttrTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        testedFS = new LFS();
        testedFS.setRootDirectory(getWorkDir());
    }
    
    public void testRenameWhenTargetFileExists() throws Exception {
        FileObject fo = FileUtil.createData(testedFS.getRoot(), "A/B/C.java");
        fo.setAttribute("test", "value");
        File parent = FileUtil.toFile(fo.getParent());
        final File attrs = new File(parent, ".nbattrs");
        assertTrue("Exists " + attrs, attrs.exists());
        File attrsbak = new File(parent, ".nbattrs~");
        assertFalse("Backup file does not exists", attrsbak.exists());
        testedFS.hook = new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                attrs.createNewFile();
            }
        };
        fo.setAttribute("test", "nova");
        assertEquals("nova", fo.getAttribute("test"));
    }    
    
    private static final class LFS extends LocalFileSystem {
        AtomicAction hook;
        
        @Override
        protected void rename(String oldName, String newName) throws IOException {
            AtomicAction h = hook;
            hook = null;
            if (h != null) {
                h.run();
            }
            super.rename(oldName, newName);
        }
        
    }
}
