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
