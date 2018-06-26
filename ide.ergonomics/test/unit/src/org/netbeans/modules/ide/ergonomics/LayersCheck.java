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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ide.ergonomics;

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LayersCheck extends NbTestCase {

    public LayersCheck(String name) {
        super(name);
    }

    public void testCanAllLayersBeParsed() throws Exception {
        int cnt = 0;
        for (FeatureInfo f : FeatureManager.features()) {
            URL u = f.getLayerURL();
            if (u == null) {
                continue;
            }
            cnt++;
            byte[] arr = new byte[1024 * 1024];
            int r = u.openStream().read(arr);
            if (r == -1) {
                fail("Cannot read " + u);
            }
            if (r == arr.length) {
                fail("Too big layer " + u);
            }
            String s = new String(arr, 0, r, "UTF-8");
            if (s.contains("path=\"")) {
                fail("There shall be no path attribute in " + u + ":\n" + s);
            }
        }
        if (cnt == 0) {
            fail("There are no layers! That is strange.");
        }
    }

    public void testNoWarningsAboutOrderingForLoaders() {
        FileObject root = FileUtil.getConfigFile("Loaders");
        assertNotNull("Loader's root found", root);
        CharSequence log = Log.enable("org.openide.filesystems", Level.WARNING);

        Enumeration<? extends FileObject> en = root.getChildren(true);
        int cnt = 0;
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            if (!fo.isFolder()) {
                continue;
            }
            FileUtil.getOrder(Arrays.asList(fo.getChildren()), true);
            cnt++;
        }
        if (cnt < 10) {
            fail("There shall be at least 10 files in loaders. Was: " + cnt);
        }

        String msg = log.toString();
        if (msg.contains(("Found same position"))) {
            fail("There shall be no same position loaders!\n" + msg);
        }
    }
}
