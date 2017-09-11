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

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.cookies.*;


import org.netbeans.junit.*;
import org.openide.util.test.MockLookup;

/** Simulate deadlock from issue 47707.
 *
 * @author Radek Matous, Jaroslav Tulach
 */
public class InstanceDataObjectIssue47707Test extends NbTestCase {
    /** folder to create instances in */
    private DataObject inst;
    /** filesystem containing created instances */
    private FileSystem lfs;
    
    public InstanceDataObjectIssue47707Test(String name) {
        super (name);
    }
    
    protected @Override void setUp() throws Exception {
        MockLookup.setInstances(new EP());
        
        String fsstruct [] = new String [] {
            "A.settings",
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("A.settings");
        
        inst = DataObject.find (bb);
    }

    public void testGetCookieCanBeCalledTwice () throws Exception {
        Object cookie = inst.getCookie (org.openide.cookies.InstanceCookie.class);
        
        assertNotNull ("There is at least data object", cookie);
        assertEquals ("Of right type", LkpForDO.class, cookie.getClass ());
        
    }
    
    private static final class EP implements Environment.Provider {
        public org.openide.util.Lookup getEnvironment (DataObject obj) {
            return new LkpForDO (new org.openide.util.lookup.InstanceContent (), obj);
        }
    }
    
    private static final class LkpForDO extends org.openide.util.lookup.AbstractLookup
    implements org.openide.cookies.InstanceCookie, Runnable {
        private boolean triedToDeadlock;
        private DataObject obj;
        
        private LkpForDO (org.openide.util.lookup.InstanceContent ic, DataObject obj) {
            super (ic);
            ic.add (this);
            this.obj = obj;
        }
        
        public void run () {
            // tries to query instance data object from other thread
            Object o = obj.getCookie (InstanceCookie.class);
            assertNotNull ("Cookie is there", o);
        }

        protected @Override void beforeLookup(Template template) {
            if (!triedToDeadlock) {
                triedToDeadlock = true;
                org.openide.util.RequestProcessor.getDefault ().post (this).waitFinished ();
            }
        }
        
        
        public String instanceName () {
            return getClass ().getName ();
        }

        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return getClass ();
        }

        public Object instanceCreate ()
        throws java.io.IOException, ClassNotFoundException {
            return this;
        }

        public @Override String toString() {
            return getClass().getName();
        }
    } // end LkpForDO
    
}
