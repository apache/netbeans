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

package org.netbeans.modules.keyring.utils;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.modules.Places;

public class Utils {

    private Utils() {}

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    public static byte[] chars2Bytes(char[] chars) {
        byte[] bytes = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++) {
            bytes[i * 2] = (byte) ((int)chars[i] / 256);
            bytes[i * 2 + 1] = (byte) (chars[i] % 256);
        }
        return bytes;
    }

    public static char[] bytes2Chars(byte[] bytes) {
        char[] result = new char[bytes.length / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (char) (((bytes[i * 2] & 0x00ff) * 256) + (bytes[i * 2 + 1] & 0x00ff));
        }
        return result;
    }

    /** Tries to set permissions on preferences storage file to -rw------- */
    public static void goMinusR(Preferences p) {
        File props = new File(Places.getUserDirectory(), ("config/Preferences" + p.absolutePath()).replace('/', File.separatorChar) + ".properties");
        if (props.isFile()) {
            props.setReadable(false, false); // seems to be necessary, not sure why
            props.setReadable(true, true);
            LOG.log(Level.FINE, "chmod go-r {0}", props);
        } else {
            LOG.log(Level.FINE, "no such file to chmod: {0}", props);
        }
    }

}
