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

package org.netbeans.modules.project.ui;

import java.net.URL;
import java.util.logging.Level;
import junit.framework.TestCase;
import org.netbeans.junit.Log;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class LazyProjectTest extends TestCase {
    
    public LazyProjectTest(String testName) {
        super(testName);
    }            

    public void testGetProjectDirectoryAlwaysNotNull() throws Exception {
        CharSequence log = Log.enable("org.netbeans.modules.project.ui", Level.WARNING);
        LazyProject instance = new LazyProject(new URL("http://www.netbeans.org"), "name", null);
        FileObject result = instance.getProjectDirectory();
        assertNotNull(result);
        
        if (log.toString().indexOf("www.netbeans.org") == -1) {
            fail("warn about wrong URL:\n" + log);
        }
    }

}
