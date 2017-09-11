/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
