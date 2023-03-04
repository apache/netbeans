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
package org.netbeans.modules.web.clientproject.api.util;

import java.io.File;

/**
 * Miscellaneous utility methods for validation.
 */
public final class ValidationUtilities {

    private static final char[] INVALID_FILENAME_CHARS = new char[] {'/', '\\', '|', ':', '*', '?', '"', '<', '>'}; // NOI18N


    private ValidationUtilities() {
    }

    /**
     * Check whether the provided filename is valid. An empty string is considered to be invalid.
     * @param filename file name to be validated
     * @return {@code true} if the provided filename is valid
     */
    public static boolean isValidFilename(String filename) {
        assert filename != null;
        if (filename.trim().length() == 0) {
            return false;
        }
        for (char ch : INVALID_FILENAME_CHARS) {
            if (filename.indexOf(ch) != -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether the provided file has a valid filename. Only the non-existing filenames in the file path are checked.
     * It means that if you pass existing directory, no check is done.
     * <p>
     * For example for <em>C:\Documents And Settings\ExistingDir\NonExistingDir\NonExistingDir2\Newdir</em> the last free filenames
     * are checked.
     * @param file file to be checked
     * @return {@code true} if the provided file has valid filename
     * @see #isValidFilename(String)
     */
    public static boolean isValidFilename(File file) {
        assert file != null;
        File tmp = file;
        while (tmp != null && !tmp.exists()) {
            if (tmp.isAbsolute() && tmp.getParentFile() == null) {
                return true;
            } else if (!isValidFilename(tmp.getName())) {
                return false;
            }
            tmp = tmp.getParentFile();
        }
        return true;
    }

}
