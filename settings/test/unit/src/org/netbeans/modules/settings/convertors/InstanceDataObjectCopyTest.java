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

package org.netbeans.modules.settings.convertors;

import java.awt.Button;
import java.awt.GraphicsEnvironment;
import java.util.Date;
import java.io.*;
import javax.swing.Action;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.awt.Actions;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.ModuleInfo;
import org.openide.util.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstanceDataObjectCopyTest extends org.netbeans.junit.NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(InstanceDataObjectCopyTest.class);
    }

    private FileObject fo;
    private Action openAction;
    
    
    public InstanceDataObjectCopyTest (String testName) {
        super (testName);
    }
    
    @Override
    protected void setUp () throws java.lang.Exception {
        clearWorkDir ();
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        openAction = Actions.forID("System", "org.openide.actions.OpenAction");
    }

    public void testSettingsFileOnNonSFSAfterCopyShouldHaveEditor () throws Exception {
        doSettingsFileOnNonSFSAfterCopyShouldHaveEditor (true);
    }
    public void testSettingsFileOnNonSFSAfterCreateFTShouldHaveEditor () throws Exception {
        doSettingsFileOnNonSFSAfterCopyShouldHaveEditor (false);
    }
        
    private void doSettingsFileOnNonSFSAfterCopyShouldHaveEditor (boolean copy) throws Exception {
        clearWorkDir ();
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (getWorkDir ());
        
        FileObject set = createSettings (lfs.getRoot (), "x.settings");
        DataObject old = DataObject.find (set);
        Date d = set.lastModified();
        
        InstanceCookie ic = (InstanceCookie)old.getCookie(InstanceCookie.class);
        assertNotNull ("The cookie is there", ic);
        Object instance = ic.instanceCreate();
        assertNotNull ("It produces a result", instance);
        assertEquals ("It is Button", Button.class, instance.getClass ());
        
        FileObject tgt = FileUtil.createFolder(lfs.getRoot (), "moved");
        DataFolder fld = DataFolder.findFolder (tgt);
        
        DataObject obj = copy ? old.copy (fld) : old.createFromTemplate(fld);
        
        assertEquals ("No change in modifications", d, set.lastModified());
        assertEquals ("The same name", obj.getPrimaryFile().getNameExt (), set.getNameExt());
        
        assertEquals (InstanceDataObject.class, obj.getClass ());
        assertNotNull ("It has edit cookie", obj.getCookie (EditCookie.class));
        assertNotNull ("It has open cookie", obj.getCookie (OpenCookie.class));
        assertNotNull ("It has editor cookie", obj.getCookie (EditorCookie.class));

        Object o = obj.getNodeDelegate ().getPreferredAction ();
        assertEquals ("Default actions should be open on non-SFS", openAction, o);
    }

    private FileObject createSettings (FileObject root, String name) throws IOException {
        FileObject set = FileUtil.createData (root, name);

        FileLock lock = set.lock ();
        PrintStream os = new PrintStream (set.getOutputStream (lock));
        
        os.println ("<?xml version=\"1.0\"?>");
        os.println ("<!DOCTYPE settings PUBLIC \"-//NetBeans//DTD Session settings 1.0//EN\" \"http://www.netbeans.org/dtds/sessionsettings-1_0.dtd\">");
        os.println ("<settings version=\"1.0\">");
//        os.println ("<module name=\"org.apache.tools.ant.module/3\" spec=\"3.15\"/>");
        os.println ("<instanceof class=\"java.io.Serializable\"/>");
        os.println ("<instanceof class=\"java.lang.Object\"/>");
        os.println ("<instanceof class=\"java.awt.Component\"/>");
        os.println ("<instance class=\"java.awt.Button\"/>");
        os.println ("</settings>");
        
        os.close ();
        lock.releaseLock();
        return set;
    }
}
