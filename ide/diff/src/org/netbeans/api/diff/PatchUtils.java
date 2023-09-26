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

package org.netbeans.api.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.netbeans.modules.diff.PatchAction;
import org.netbeans.modules.diff.builtin.Patch;

/**
 * Utility class for patch application.
 * 
 * @author Tomas Stupka
 * @since 1.19
 */
public class PatchUtils {

    private PatchUtils() {}
    
    /**
     * Applies the patch file on the context file or folder. The patch file may be
     * in a context, normal or unified format.
     * 
     * @param patch the patch file
     * @param context the file or folder to be updated with the patch
     * @throws org.netbeans.modules.diff.builtin.PatchException
     * @throws IOException - the patch is invalid or cannot be applied
     * @since 1.19
     */
    public static void applyPatch(File patch, File context) throws IOException {
        PatchAction.performPatch(patch, context);
    }

    /**
     * Returns true only if the given file is a patch
     * in a format recognizable as a
     * <ul>
     *  <li>context</li>
     *  <li>normal</li>
     *  <li>unified</li>
     * </ul>
     * @param patch patch file
     * @return true if the given input stream is a patch otherwise false
     * @throws IOException
     * @since 1.19
     */
    public static boolean isPatch(File patch) throws IOException {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(patch)));
        try {
            return (Patch.parse(reader).length > 0);
        } finally {
            reader.close();
        }
    }
}
