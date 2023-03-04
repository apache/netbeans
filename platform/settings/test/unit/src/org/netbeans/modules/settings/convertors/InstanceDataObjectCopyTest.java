/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
