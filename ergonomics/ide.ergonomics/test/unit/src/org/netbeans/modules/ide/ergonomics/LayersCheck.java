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
package org.netbeans.modules.ide.ergonomics;

import java.net.URL;
import java.nio.charset.StandardCharsets;
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
            String s = new String(arr, 0, r, StandardCharsets.UTF_8);
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
