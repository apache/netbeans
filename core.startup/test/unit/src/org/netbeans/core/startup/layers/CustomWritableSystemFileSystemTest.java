/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.core.startup.layers;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.actions.SystemAction;

/**
 * Tests whether the system property 
 * <code>org.netbeans.core.systemfilesystem.custom</code> correctly
 * installs a custom filesystem.
 * 
 * @author David Strupl
 */
public class CustomWritableSystemFileSystemTest extends NbTestCase {

    private SystemFileSystem sfs;

    public CustomWritableSystemFileSystemTest(String testName) {
        super(testName);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        System.setProperty("org.netbeans.core.systemfilesystem.custom", PoohFileSystem.class.getName());
        sfs = (SystemFileSystem) Repository.getDefault().getDefaultFileSystem();
    }

    public void testCustomSFSWritableLayerPresent() throws Exception {
        FileSystem writable = sfs.createWritableOn(null);
        assertTrue(writable instanceof ModuleLayeredFileSystem);
        ModuleLayeredFileSystem mlf = (ModuleLayeredFileSystem) writable;
        assertTrue("Expected fs" + mlf.getWritableLayer(), mlf.getWritableLayer() instanceof PoohFileSystem);
        PoohFileSystem pooh = (PoohFileSystem)mlf.getWritableLayer();
        assertEquals("Proper value of nb user", getWorkDirPath(), pooh.netbeansUser);
    }

    public static class PoohFileSystem extends FileSystem {
        String netbeansUser;
        
        public PoohFileSystem() {
            netbeansUser = System.getProperty("netbeans.user");
        }
        
        public String getDisplayName() {
            return "Pooh";
        }

        public boolean isReadOnly() {
            return false;
        }

        public FileObject getRoot() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public FileObject findResource(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public SystemAction[] getActions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
