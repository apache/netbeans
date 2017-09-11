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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.openide.loaders;

import java.io.IOException;
import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/** Checks race condition in the Lkp.beforeLookup
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #4489: Second has cookie expected:<FileEntityResolver66438Test$Env@...> but was:<null>
public class FileEntityResolver66438Test extends NbTestCase {
    
    public FileEntityResolver66438Test(String testName) {
        super(testName);
    }

    public void testRaceCondition() throws Exception {
        MockLookup.setInstances(new ErrManager());
        
        // register Env as a handler for PublicIDs "-//NetBeans//Test//EN" which
        // is will contain the settings file we create
        FileObject root = FileUtil.getConfigRoot();
        FileObject register = FileUtil.createData (root, "/xml/lookups/NetBeans/Test.instance");
        register.setAttribute("instanceCreate", Env.INSTANCE);
        assertTrue (register.getAttribute("instanceCreate") instanceof Environment.Provider);
        
        
        // prepare an object to ask him for cookie
        FileObject fo = FileEntityResolverDeadlock54971Test.createSettings (root, "x.settings");
        final DataObject obj = DataObject.find (fo);

        class QueryIC implements Runnable {
            public InstanceCookie ic;
            
            public void run() {
                ic = (InstanceCookie)obj.getCookie(InstanceCookie.class);
            }
        }
        
        QueryIC i1 = new QueryIC();
        QueryIC i2 = new QueryIC();
                
        RequestProcessor.Task t1 = new RequestProcessor("t1").post(i1);
        RequestProcessor.Task t2 = new RequestProcessor("t2").post(i2);
        
        t1.waitFinished();
        t2.waitFinished();
        
        assertEquals("First has cookie", Env.INSTANCE, i1.ic);
        assertEquals("Second has cookie", Env.INSTANCE, i2.ic);
    }
    
    private static final class ErrManager extends ErrorManager {
        private boolean block;
        
        public Throwable attachAnnotations(Throwable t, ErrorManager.Annotation[] arr) {
            return null;
        }

        public ErrorManager.Annotation[] findAnnotations(Throwable t) {
            return null;
        }

        public Throwable annotate(Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return null;
        }

        public void notify(int severity, Throwable t) {
        }

        public void log(int severity, String s) {
            if (block && s.indexOf("change the lookup") >= 0) {
                block = false;
                ErrorManager.getDefault().log("Going to sleep");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                ErrorManager.getDefault().log("Done sleeping");
            }
        }

        public ErrorManager getInstance(String name) {
            if (name.equals("org.netbeans.core.xml.FileEntityResolver")) {
                ErrManager e = new ErrManager();
                e.block = true;
                return e;
            }
            return this;
        }
        
    }

    private static final class Env 
    implements InstanceCookie, org.openide.loaders.Environment.Provider {
        public static int howManyTimesWeHandledRequestForEnvironmentOfOurObject;
        public static final Env INSTANCE = new Env ();
        
        private Env () {
            assertNull (INSTANCE);
        }

        public String instanceName() {
            return getClass ().getName();
        }

        public Object instanceCreate() throws IOException, ClassNotFoundException {
            return this;
        }

        public Class instanceClass() throws IOException, ClassNotFoundException {
            return getClass ();
        }

        public Lookup getEnvironment(DataObject obj) {
            return Lookups.singleton(this);
        }
        
    }
    
}
