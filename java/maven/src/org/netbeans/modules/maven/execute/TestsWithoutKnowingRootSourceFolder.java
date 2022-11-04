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
package org.netbeans.modules.maven.execute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

class TestsWithoutKnowingRootSourceFolder {

    private static final String LAST_FOLDER_BEFORE_PACKAGE = "java/";
    private static final Pattern packageFinder = Pattern.compile("^.*package\\s*([a-z][a-z0-9_]*(\\s*\\.\\s*[a-z][a-z0-9_]*)*[0-9a-z_]*)\\s*;.*$");

    static Stream<String> find(FileObject[] fos) {
        List<String> tests = new ArrayList<>();

        for (final FileObject fo : fos) {
            if (fo.isFolder()) {
                tests.add(testBasedOnFolder(fo));

            } else if (fo.isData()) {
                tests.add(testBasedOnFile(fo));
            }

        }
        return tests.stream();
    }

    private static String testBasedOnFolder(final FileObject fo) {
        final String path = fo.getPath();

        // based on assumption of default maven folder structure
        final int startFrom = path.indexOf(LAST_FOLDER_BEFORE_PACKAGE);
        if (startFrom != -1) {
            final String packageName = path.substring(startFrom + LAST_FOLDER_BEFORE_PACKAGE.length())
                    .replace("/", ".");
            return packageName + ".**";
        }

        return "";
    }

    private static String testBasedOnFile(final FileObject fo) {
        try {
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(fo.getInputStream())
            );
            while (reader.ready()) {
                final String line = reader.readLine()
                        .trim();

                Matcher matcher = packageFinder.matcher(line);

                if (matcher.matches() && matcher.groupCount() == 2) {
                    final String packageName = matcher.group(1);
                    return packageName.replace(" ", "") + "." + fo.getName();
                }
            }

            return "**." + fo.getName();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return "";
    }
}
