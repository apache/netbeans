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


import java.awt.Button;
import java.awt.Color;
import java.beans.*;
import java.beans.beancontext.BeanContextChildSupport;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import javax.swing.JButton;
import junit.framework.Test;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.AbstractLookup;

public class InstanceDataObjectLookupWarningTest extends NbTestCase {
    /** folder to create instances in */
    private DataFolder folder;
    /** filesystem containing created instances */
    private FileSystem lfs;
    
    /** Creates new DataFolderTest */
    public InstanceDataObjectLookupWarningTest(String name) {
        super (name);
    }
    
    
    protected void setUp () throws Exception {
        TestUtilHid.destroyLocalFileSystem (getName());
        clearWorkDir();
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), new String[0]);

    }

    /** #28118, win sys relies that instance data object fires cookie 
     * changes when its settings file removed, it gets into corruped state otherwise. */
    public void testNoWarnings() throws Exception {
        CharSequence log = Log.enable("org.openide.loaders", Level.WARNING);
        
        FileObject fo = FileUtil.createData(lfs.getRoot(), "x.instance");
        fo.setAttribute("instanceClass", "javax.swing.JButton");
        DataObject obj = DataObject.find(fo);
        assertEquals("IDO", InstanceDataObject.class, obj.getClass());
        
        InstanceCookie ic = obj.getLookup().lookup(InstanceCookie.class);
        assertNotNull("We have cookie", ic);
        
        Object o = ic.instanceCreate();
        assertNotNull("Obj created", o);
        
        assertEquals("button", JButton.class, o.getClass());
        
        if (log.length() > 0) {
            fail("No warnings, but: " + log);
        }
    }
}
