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

package org.netbeans.modules.ide.ergonomics.fod;

import org.netbeans.modules.ide.Factory;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AdditionalProjectFactoryTest extends NbTestCase {
    private Logger LOG;
    
    public AdditionalProjectFactoryTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(AdditionalProjectFactoryTest.class).
            gui(false).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules("ide[0-9]*", ".*")
        );
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
        URL u = AdditionalProjectFactoryTest.class.getResource("default.xml");
        assertNotNull("Default layer found", u);
        XMLFileSystem xml = new XMLFileSystem(u);
        FileObject fo = xml.findResource("Menu/Edit_hidden");
        assertNotNull("File found", fo);
    }

    public void testIfProjectFactoryInstalled() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Menu/Edit");
        assertNull("Default layer is on and Edit is hidden", fo);

        LOG.info("about to create config data");
        FileUtil.createData(FileUtil.getConfigRoot(), 
            "Services/" + Factory.class.getName().replace('.', '-') + ".instance"
        );
        LOG.info("looking up Factory.class");
        Factory f = Lookup.getDefault().lookup(Factory.class);
        assertNotNull("Factory found", f);
        LOG.info("Factory found");
        FoDLayersProvider.getInstance().waitFinished();
        LOG.info("Refresh finished");
        
        for (int i = 0; i < 100; i++) {
            fo = FileUtil.getConfigFile("Menu/Edit");
            if (fo != null) {
                break;
            }
            LOG.log(Level.INFO, "No Menu/Edit found, in round {0}", i);
            Thread.sleep(500);
        }
        assertNotNull("Default layer is off and Edit is visible", fo);
    }
}
