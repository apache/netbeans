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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import junit.extensions.*;
import junit.textui.TestRunner;

import org.openide.filesystems.*;
import junit.framework.*;
import org.netbeans.junit.*;
import java.io.IOException;
import org.openide.loaders.DataLoader.RecognizedFiles;
import org.openide.nodes.Node;
import java.lang.ref.WeakReference;
import java.io.*;
import java.util.*;
import java.beans.PropertyVetoException;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.openide.*;
import org.openide.util.Enumerations;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.nodes.CookieSet;

/**
 * @author Jaroslav Tulach
 */
public class RejectOnCopyCreatesDefaultTest extends LoggingTestCaseHid {
    FileSystem fs;
    DataObject one;
    DataFolder from;
    DataFolder to;
    ErrorManager err;
    
    
    /** Creates new DataObjectTest */
    public RejectOnCopyCreatesDefaultTest (String name) {
        super (name);
    }
    
    public void setUp() throws Exception {
        clearWorkDir();
        
        SimpleLoader.active = true;
        
        super.setUp();
        
        registerIntoLookup(new Pool());
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        fs = lfs;
        FileUtil.createData(fs.getRoot(), "from/x.prima");
        FileUtil.createFolder(fs.getRoot(), "to/");
        
        one = DataObject.find(fs.findResource("from/x.prima"));
        assertEquals(MultiDataObject.class, one.getClass());
        assertEquals(SimpleLoader.getLoader(SimpleLoader.class), one.getLoader());
        
        from = one.getFolder();
        to = DataFolder.findFolder(fs.findResource("to/"));
        
        assertEquals("Nothing there", 0, to.getPrimaryFile().getChildren().length);
    }
    
    public void testCopyAndSee() throws Exception {
        
        SimpleLoader.active = false;
        
        DataObject copy = one.copy(to);
        
        if (copy.getLoader() == one.getLoader()) {
            fail("Loaders shall be different:\n" + one + "\nand:\n" + copy);
        }
    }

    private static class Pool extends DataLoaderPool {
        protected Enumeration loaders() {
            return Enumerations.singleton(SimpleLoader.getLoader(SimpleLoader.class));
        }
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(MultiDataObject.class.getName());
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        
        public static boolean active = true;
        
        protected FileObject findPrimaryFile(FileObject fo) {
            if (!fo.isFolder() && active) {
                return fo.hasExt("prima") ? fo : null;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject(primaryFile, this);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
}
