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

package org.netbeans.core.lookup;

import org.netbeans.core.NbLoaderPool;

import org.openide.util.Lookup;
import javax.swing.Action;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileUtil;

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest4 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest4(String name) {
        super(name);
    }
    
    /** Currently fails (lookup gets a result not assignable to its template),
     * probably because this is not supported with *.instance (?).
     */
    public void testReloadDotInstanceSwitchesLookupByNewClass() throws Exception {
        twiddle(m1, TWIDDLE_ENABLE);
        ClassLoader l1 = null, l2 = null;
        try {
            l1 = m1.getClassLoader();
            Class c1 = l1.loadClass("test1.SomeAction");
            assertEquals("Correct loader", l1, c1.getClassLoader());
            assertTrue("SomeAction<1> instance found after module installation",
                existsSomeAction(c1));
            
            ClassLoader g1 = Lookup.getDefault().lookup(ClassLoader.class);
            ERR.log("Before reload: " + g1);
            twiddle(m1, TWIDDLE_RELOAD);
            ClassLoader g2 = Lookup.getDefault().lookup(ClassLoader.class);
            ERR.log("After reload: " + g2);
            // Sleeping for a few seconds here does *not* help.
            l2 = m1.getClassLoader();
            assertTrue("ClassLoader really changed", l1 != l2);
            Class c2 = l2.loadClass("test1.SomeAction");
            assertTrue("Class really changed", c1 != c2);
            
            assertTrue("Glboal Class loaders really changed", g1 != g2);
            
            
            NbLoaderPool.waitFinished();
            ERR.log("After waitFinished");
            assertTrue("SomeAction<1> instance not found after module reload",
                !existsSomeAction(c1));
            assertTrue("SomeAction<2> instance found after module reload",
                existsSomeAction(c2));
        } finally {
            ERR.log("Verify why it failed");
            FileObject fo = FileUtil.getConfigFile("Services/Misc/inst-1.instance");
            ERR.log("File object found: " + fo);
            if (fo != null) {
                DataObject obj = DataObject.find(fo);
                ERR.log("data object found: " + obj);
                InstanceCookie ic = (InstanceCookie)obj.getCookie(InstanceCookie.class);
                ERR.log("InstanceCookie: " + ic);
                if (ic != null) {
                    ERR.log("value: " + ic.instanceCreate());
                    ERR.log(" cl  : " + ic.instanceCreate().getClass().getClassLoader());
                    ERR.log(" l1  : " + l1);
                    ERR.log(" l2  : " + l2);
                }
            }
            
            ERR.log("Before disable");
            twiddle(m1, TWIDDLE_DISABLE);
        }
    }
    
    /** Though this works in test #5, seems to get "poisoned" here by running
     * in the same VM as the previous test.
     */
    public void testReloadSettingsSwitchesLookupByNewClass() throws Exception {
        assertTrue("There is initially nothing in lookup",
            !existsSomeAction(Action.class));
        twiddle(m2, TWIDDLE_ENABLE);
        try {
            ClassLoader l1 = m2.getClassLoader();
            Class c1 = l1.loadClass("test2.SomeAction");
            assertEquals("Correct loader", l1, c1.getClassLoader());
            assertTrue("SomeAction<1> instance found after module installation",
                existsSomeAction(c1));
            
            ERR.log("Before reload");
            twiddle(m2, TWIDDLE_RELOAD);
            ERR.log("After reload");
            ClassLoader l2 = m2.getClassLoader();
            assertTrue("ClassLoader really changed", l1 != l2);
            Class c2 = l2.loadClass("test2.SomeAction");
            assertTrue("Class really changed", c1 != c2);
            // Make sure the changes take effect
            NbLoaderPool.waitFinished();
            ERR.log("After waitFinished");
            
            assertTrue("SomeAction<1> instance not found after module reload",
                !existsSomeAction(c1));
            assertTrue("SomeAction<2> instance found after module reload",
                existsSomeAction(c2));
        } finally {
            ERR.log("Finally disable");
            twiddle(m2, TWIDDLE_DISABLE);
        }
    }
    
}
