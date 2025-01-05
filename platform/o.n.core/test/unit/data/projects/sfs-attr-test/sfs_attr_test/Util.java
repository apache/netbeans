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

package sfs_attr_test;

import java.awt.Image;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.awt.Toolkit;
import java.net.URL;
import org.openide.util.Utilities;

public abstract class Util {
    private Util() {}

    // Called by reflection via registration in platform/o.n.core/test/unit/data/projects/sfs-attr-test/sfs_attr_test/layer.xml
    private static Image mergeIcons(FileObject fo) throws IOException {
        int count = ((Integer)fo.getAttribute("iconCount")).intValue();
        if (count < 2) throw new IOException();
        URL icon1 = (URL)fo.getAttribute("icon1");
        System.out.println("Loading " + icon1 + " just to be sure...");
        // Make sure it is really loadable:
        icon1.openConnection().getInputStream().close();
        Image img = Toolkit.getDefaultToolkit().getImage(icon1);
        for (int i = 2; i <= count; i++) {
            URL iconn = (URL)fo.getAttribute("icon" + i);
            System.out.println("Loading " + iconn + " just to be sure...");
            iconn.openConnection().getInputStream().close();
            Image added = Toolkit.getDefaultToolkit().getImage(iconn);
            int x = ((Integer)fo.getAttribute("iconx" + count)).intValue();
            int y = ((Integer)fo.getAttribute("icony" + count)).intValue();
            img = Utilities.mergeImages(img, added, x, y);
        }
        return img;
    }
    
}
