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

package org.netbeans.core.startup;

import org.netbeans.SetupHid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

abstract class NbInstallerTestBase extends SetupHid {

    public NbInstallerTestBase(String n) {
        super(n);
    }

    protected static String slurp(String path) throws IOException {
        Main.getModuleSystem(); // #26451
        FileObject fo = FileUtil.getConfigFile(path);
        if (fo == null) return null;
        InputStream is = fo.getInputStream();
        StringBuilder text = new StringBuilder((int)fo.getSize());
        byte[] buf = new byte[1024];
        int read;
        while ((read = is.read(buf)) != -1) {
            text.append(new String(buf, 0, read, StandardCharsets.US_ASCII));
        }
        return text.toString();
    }

}
