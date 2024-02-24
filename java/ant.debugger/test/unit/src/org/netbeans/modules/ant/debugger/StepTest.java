/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.ant.debugger;

import java.io.File;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;



/**
 * Tests Ant debugger stepping actions: step in, step out and step over.
 *
 * @author Jan Jancura
 */
public class StepTest extends NbTestCase {
    
    static {
        System.setProperty ("org.openide.util.Lookup", Lkp.class.getName ());
    }

    private String          sourceRoot = System.getProperty ("debuggerant.dir");

    public StepTest (String s) {
        super (s);
    }
    
    public void testStepOver () throws Exception {
        File file = new File (sourceRoot, "build.xml");
        file = FileUtil.normalizeFile(file);
        DebuggerAntLogger.getDefault ().debugFile (file);
        FileObject fileObject = FileUtil.toFileObject (file);
        ActionUtils.runTarget (
            fileObject, 
            new String[] {"run"},
            null
        );
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() throws ReflectiveOperationException {
            setLookups(new Lookup[] {
                Lookups.fixed(new Object[] {
                    new IFL(),
                    Class.forName("org.netbeans.modules.masterfs.MasterURLMapper").getDeclaredConstructor().newInstance(),
                    new DebuggerAntLogger ()
                }),
            });
        }
    }

    private static final class IFL extends InstalledFileLocator {
        public IFL() {}
        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ant/nblib/bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                assertNotNull("must set test.bridge.jar", path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path);
            } else if (relativePath.startsWith("ant/")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            } else {
                return null;
            }
        }
    }
}
