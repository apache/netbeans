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

package org.netbeans.core.lookup;

import org.netbeans.core.NbLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileUtil;

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest5 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest5(String name) {
        super(name);
    }
    
    public void testReloadSettingsSwitchesLookupByNewClass() throws Exception {
        ERR.log("before twidle enabled");
        twiddle(m2, TWIDDLE_ENABLE);
        ERR.log("Ok twidle enable");
        ClassLoader l1 = null;
        ClassLoader l2 = null;
        try {
            l1 = m2.getClassLoader();
            Class c1 = l1.loadClass("test2.SomeAction");
            assertEquals("Correct loader", l1, c1.getClassLoader());
            assertTrue("SomeAction<1> instance found after module installation",
                existsSomeAction(c1));
            
            ERR.log("Action successfully checked, reload"); 
            twiddle(m2, TWIDDLE_RELOAD);
            ERR.log("Twidle reload");
            l2 = m2.getClassLoader();
            assertTrue("ClassLoader really changed", l1 != l2);
            Class c2 = l2.loadClass("test2.SomeAction");
            assertTrue("Class really changed", c1 != c2);
            
            ERR.log("After successful checks that there was a reload changes");
            
            NbLoaderPool.waitFinished();
            
            ERR.log("Waiting for pool node to update itself");

            assertTrue("SomeAction<1> instance not found after module reload",
                !existsSomeAction(c1));
            assertTrue("SomeAction<2> instance found after module reload",
                existsSomeAction(c2));
        } finally {
            ERR.log("Verify why it failed");
            FileObject fo = FileUtil.getConfigFile("Services/Misc/inst-2.settings");
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
            ERR.log("Disabling");
            twiddle(m2, TWIDDLE_DISABLE);
            ERR.log("Disabled");
        }
    }
    
}
