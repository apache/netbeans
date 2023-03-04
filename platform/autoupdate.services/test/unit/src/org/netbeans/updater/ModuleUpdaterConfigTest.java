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
package org.netbeans.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ModuleUpdaterConfigTest extends NbTestCase {
    private File props;
    private File cluster;
    private static String DRIVE = Utilities.isWindows() ? "C:" : "";
    
    public ModuleUpdaterConfigTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        Properties p = new Properties();
        p.setProperty("relativeClassPath", DRIVE + "%FS%jarda%PS%" + DRIVE + "%FS%darda%PS%" + DRIVE + "%FS%parda");
        
        props = new File(getWorkDir(), "p.properties");
        FileOutputStream os = new FileOutputStream(props);
        p.store(os, "");
        os.close();
        
        cluster = new File(getWorkDir(), "cluster");
        cluster.mkdirs();
    }
    
    public void testMainConfigParsesPathSeparator() {
        ModuleUpdater.MainConfig mc = new ModuleUpdater.MainConfig(props.getPath(), cluster);
        String exp = File.pathSeparator + DRIVE + File.separator + "jarda" + File.pathSeparator + DRIVE + File.separator + "darda" + File.pathSeparator + DRIVE + File.separator + "parda";
        if (!mc.getClasspath().equals(exp)) {
            fail("Expecting " + exp + " but was: " + mc.getClasspath());
        }
    }

    
}
