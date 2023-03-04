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

package org.netbeans.modules.autoupdate.services;

import java.awt.Image;
import java.net.URL;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogFactory;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;

public class AutoupdateCatalogFactoryTest extends NbTestCase {

    public AutoupdateCatalogFactoryTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
    }

    public void testCreateUpdateProviderWithOwnIcon() throws Exception {
        FileObject f = FileUtil.getConfigRoot().createData("whatever.instance");
        f.setAttribute("url", "file:/wherever.xml");
        f.setAttribute("displayName", "Whatever");
        f.setAttribute("category", "Jarda's Updates");
        f.setAttribute("iconBase", "org/netbeans/modules/autoupdate/services/resources/icon-standard.png");
        UpdateProvider up = AutoupdateCatalogFactory.createUpdateProvider(f);
        UpdateUnitProvider uup = Trampoline.API.createUpdateUnitProvider (new UpdateUnitProviderImpl (up));
        assertEquals("whatever", uup.getName());
        assertEquals("Whatever", uup.getDisplayName());
        assertEquals(new URL("file:/wherever.xml"), uup.getProviderURL());
        Image img = ImageUtilities.loadImage("org/netbeans/modules/autoupdate/services/resources/icon-standard.png");
        assertEquals("Icons are the same", img, uup.getSourceIcon());
    }

    public void testFactoryMethodsAndIcons() throws Exception {
        Image img = ImageUtilities.loadImage("org/netbeans/modules/autoupdate/services/resources/icon-standard.png");
        UpdateUnitProvider res = UpdateUnitProviderFactory.getDefault().create(
           "code-name", "Whatever", new URL("file:/whereever.xml"), 
           "org/netbeans/modules/autoupdate/services/resources/icon-standard.png", "my category"
        );
        assertEquals("code-name", res.getName());
        assertEquals("Whatever", res.getDisplayName());
        assertEquals("Good image", img, res.getSourceIcon());
    }
}
