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

import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.AntBasedType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AdditionalAntBasedTest extends NbTestCase
implements LookupListener {
    Logger LOG;

    public AdditionalAntBasedTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(AdditionalAntBasedTest.class).
            gui(false).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules("ide[0-9]*", ".*")
        );
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("org.netbeans.modules.ide.ergonomics.fod." + getName());

        URL u = AdditionalAntBasedTest.class.getResource("default.xml");
        assertNotNull("Default layer found", u);
        XMLFileSystem xml = new XMLFileSystem(u);
        FileObject fo = xml.findResource("Menu/Edit_hidden");
        assertNotNull("File found", fo);
    }

    public void testIfAntBasedProjectInstalled() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Menu/Edit");
        assertNull("Default layer is on and Edit is hidden", fo);

        Result<AntBasedType> res = Lookup.getDefault().lookupResult(AntBasedType.class);
        assertEquals("no ant project types: " + res.allInstances(), 0, res.allInstances().size());
        res.addLookupListener(this);

        LOG.info("creating AntBasedType registration on disk");
        FileUtil.createData(FileUtil.getConfigRoot(), 
            "Services/" + AntBasedType.class.getName().replace('.', '-') + ".instance"
        );
        LOG.info("registration created");
        AntBasedType f = Lookup.getDefault().lookup(AntBasedType.class);
        LOG.info("looking up the result " + f);
        synchronized (this) {
            while (!delivered) {
                wait();
            }
        }

        assertNotNull("Ant found", f);
        LOG.info("waiting for FoDFileSystem to be updated");
        FoDLayersProvider.getInstance().waitFinished();
        LOG.info("waiting for FoDFileSystem to be waitFinished is over");

        for (int cnt = 0; cnt < 5; cnt++) {
            fo = FileUtil.getConfigFile("Menu/Edit");
            if (fo != null) {
                break;
            }
            Thread.sleep(500);
        }
        LOG.info("Edit found: " + fo);
        LOG.info("Menu items: " + Arrays.toString(FileUtil.getConfigFile("Menu").getChildren()));
        assertNotNull("Default layer is off and Edit is visible", fo);
    }

    private boolean delivered;
    public synchronized void resultChanged(LookupEvent ev) {
        delivered = true;
        notifyAll();
    }
}
