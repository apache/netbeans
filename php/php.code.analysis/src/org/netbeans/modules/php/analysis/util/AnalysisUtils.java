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
package org.netbeans.modules.php.analysis.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.queries.PhpVisibilityQuery;
import org.netbeans.modules.php.api.queries.Queries;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.refactoring.api.Scope;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public final class AnalysisUtils {

    private static final Logger LOGGER = Logger.getLogger(AnalysisUtils.class.getName());

    private static final String SERIALIZE_DELIMITER = "|"; // NOI18N


    private AnalysisUtils() {
    }

    public static String serialize(List<String> input) {
        return StringUtils.implode(input, SERIALIZE_DELIMITER);
    }

    public static List<String> deserialize(String input) {
        return StringUtils.explode(input, SERIALIZE_DELIMITER);
    }

    public static Map<FileObject, Integer> countPhpFiles(Scope scope) {
        Map<FileObject, Integer> counts = new HashMap<>();
        for (FileObject root : scope.getSourceRoots()) {
            counts.put(root, countPhpFiles(Queries.getVisibilityQuery(PhpModule.Factory.forFileObject(root)), root, true));
        }
        for (FileObject file : scope.getFiles()) {
            counts.put(file, countPhpFiles(Queries.getVisibilityQuery(PhpModule.Factory.forFileObject(file)), file, true));
        }
        for (NonRecursiveFolder nonRecursiveFolder : scope.getFolders()) {
            FileObject folder = nonRecursiveFolder.getFolder();
            counts.put(folder, countPhpFiles(Queries.getVisibilityQuery(PhpModule.Factory.forFileObject(folder)), folder, false));
        }
        return counts;
    }

    // XXX remove and use new api method from ErrorDescriptionFactory
    public static int[] computeLineMap(FileObject file, Charset decoder) {
        Reader in = null;
        List<Integer> lineLengthsTemp = new ArrayList<>();
        int currentOffset = 0;

        lineLengthsTemp.add(0);
        lineLengthsTemp.add(0);

        try {
            in = new InputStreamReader(file.getInputStream(), decoder);

            int read;
            boolean wascr = false;
            boolean lineStart = true;

            while ((read = in.read()) != (-1)) {
                currentOffset++;

                switch (read) {
                    case '\r':
                        wascr = true;
                        lineLengthsTemp.add(currentOffset);
                        lineLengthsTemp.add(currentOffset);
                        lineStart = true;
                        break;
                    case '\n':
                        if (wascr) {
                            wascr = false;
                            currentOffset--;
                            break;
                        }
                        lineLengthsTemp.add(currentOffset);
                        lineLengthsTemp.add(currentOffset);
                        wascr = false;
                        lineStart = true;
                        break;
                    default:
                        // noop
                }

                if (lineStart && Character.isWhitespace(read)) {
                    lineLengthsTemp.set(lineLengthsTemp.size() - 2, currentOffset);
                    lineLengthsTemp.set(lineLengthsTemp.size() - 1, currentOffset);
                } else if (!Character.isWhitespace(read)) {
                    lineLengthsTemp.set(lineLengthsTemp.size() - 1, currentOffset);
                    lineStart = false;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        int[] lineOffsets = new int[lineLengthsTemp.size()];
        int i = 0;

        for (Integer o : lineLengthsTemp) {
            lineOffsets[i++] = o;
        }

        return lineOffsets;
    }

    private static int countPhpFiles(PhpVisibilityQuery visibilityQuery, FileObject fileObject, boolean recursive) {
        if (!visibilityQuery.isVisible(fileObject)) {
            LOGGER.log(Level.FINE, "Ignoring invisible file {0}", fileObject);
            return 0;
        }
        int count = 0;
        if (FileUtils.isPhpFile(fileObject)) {
            count++;
        }
        for (FileObject child : fileObject.getChildren()) {
            if (!visibilityQuery.isVisible(child)) {
                LOGGER.log(Level.FINE, "Ignoring invisible file {0}", child);
                continue;
            }
            if (FileUtils.isPhpFile(child)) {
                count++;
            }
            if (recursive
                    && child.isFolder()) {
                count += countPhpFiles(visibilityQuery, child, recursive);
            }
        }
        return count;
    }

    public static int getValidInt(int min, int max, int target) {
        return Math.min(Math.max(min, target), max);
    }

}
