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

package org.netbeans.libs.git.jgit.utils;

import java.io.File;

/**
 *
 * @author ondra
 */
public final class TestUtils {
    private TestUtils () {}

    public static String getRelativePath (File file, File repo) {
        StringBuilder relativePath = new StringBuilder("");
        if (!file.equals(repo)) {
            while (file != null && !file.equals(repo)) {
                relativePath.insert(0, "/").insert(0, file.getName());
                file  = file.getParentFile();
            }
            if (file == null) {
                throw new IllegalArgumentException(file.getAbsolutePath() + " is not under " + repo.getAbsolutePath());
            }
            relativePath.deleteCharAt(relativePath.length() - 1);
        }
        return relativePath.toString();
    }
}
