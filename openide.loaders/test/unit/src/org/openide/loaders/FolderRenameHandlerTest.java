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

import junit.framework.*;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Jan Becicka
 */
public class FolderRenameHandlerTest extends TestCase {
    static {
        System.setProperty ("org.openide.util.Lookup", Lkp.class.getName()); // NOI18N
    }

    private FileObject fo;
    private Node n;
    private FolderRenameHandlerImpl frh = new FolderRenameHandlerImpl();
    
    public FolderRenameHandlerTest (String testName) {
        super (testName);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        FileObject root = FileUtil.getConfigRoot();
        fo = FileUtil.createFolder (root, "test");// NOI18N
        
        DataObject obj = DataObject.find (fo);
        if (!  (obj instanceof DataFolder)) {
            fail ("It should be DataFolder: " + obj);// NOI18N
        }
        
        assertNotNull(obj);
        n = obj.getNodeDelegate();
        assertNotNull(n);
    }
    
    public void tearDown() throws Exception {
        super.tearDown(); 
        fo.delete();
    }

    public void testRenameHandlerNotCalled () throws Exception {
        ((Lkp) Lkp.getDefault()).register(new Object[]{});
        frh.called = false;
        
        n.setName("blabla");
        assertFalse(frh.called);
    }
    
    public void testRenameHandlerCalled () throws Exception {
        ((Lkp) Lkp.getDefault()).register(new Object[]{frh});
        frh.called = false;
        
        n.setName("foo");// NOI18N
        assertTrue(frh.called);
    }
    
    public static class Lkp extends ProxyLookup {
        private static final Lookup mandatorySerices =
            Lookups.singleton(new NbMutexEventProvider());
        public Lkp() {
            super(new Lookup[]{mandatorySerices});
        }
        public void register(Object[] instances) {
            setLookups(new Lookup[] {
                mandatorySerices,
                Lookups.fixed(instances)
            });
        }
    }    
    private static final class FolderRenameHandlerImpl implements FolderRenameHandler {
        boolean called  = false;
        public void handleRename(DataFolder folder, String newName) throws IllegalArgumentException {
            called = true;
        }
    }
    
}
