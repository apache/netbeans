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
package org.netbeans.modules.gsf.testrunner.ui.output;

import java.io.File;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras, Erno Mononen
 */
final class OutputUtils {

//    private static final Logger LOGGER = Logger.getLogger(OutputUtils.class.getName());
//
//    private OutputUtils() {
//    }
//
//    /**
//     */
//    static void openCallstackFrame(Node node, String frameInfo, int line) {
//
//                Report report = getTestsuiteNode(node).getReport();
//        ExecutionUtils.FileLocation location = ExecutionUtils.getLocation(frameInfo);
//        if (location != null) {
//            FileObject fo = findFile(location.file, report.getFileLocator());
//            if (fo != null) {
//                if (line == -1) {
//                    line = location.line;
//                }
//                OutputProcessor.open(fo, line);
//                return;
//            }
//        }
//
//        LOGGER.info("Could not open a file for " + frameInfo) ;
//    }
//
//    static TestsuiteNode getTestsuiteNode(Node node) {
//        while (!(node instanceof TestsuiteNode)) {
//            node = node.getParentNode();
//        }
//        return (TestsuiteNode) node;
//    }
//
//    // TODO: copied from OutputUtils, should introduce this as a utility method
//    // in ruby.platform
//    static FileObject findFile(final String path, FileLocator fileLocator) {
//        if (fileLocator != null) {
//            FileObject fo = fileLocator.find(path);
//            if (fo != null) {
//                return fo;
//            }
//        }
//
//        // Perhaps it's an absolute path of some sort... try to resolve those
//        // Absolute path? Happens for stack traces in JRuby libraries and such
//        File file = new File(path);
//        if (file.isFile()) {
//            return FileUtil.toFileObject(FileUtil.normalizeFile(file));
//        } else {
//            LOGGER.warning("Cannot resolve file for \"" + path + "\" path.");
//            return null;
//        }
//    }

}
