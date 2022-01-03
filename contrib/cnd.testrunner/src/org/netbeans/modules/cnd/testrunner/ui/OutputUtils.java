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
package org.netbeans.modules.cnd.testrunner.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 */
final class OutputUtils {

    private static final Logger LOGGER = Logger.getLogger(OutputUtils.class.getName());

    private OutputUtils() {
    }

    /**
     */
    static void openCallstackFrame(Node node, String frameInfo, int line) {
//        Report report = getTestsuiteNode(node).getReport();
//
//        FileLocation location = null;
//        // The stacktrace format is defined in nb_test_runner.py
//        Pattern STACK_FRAME = Pattern.compile("\\S+ in (\\S+):(\\d+)"); // NOI18N
//        Matcher matcher = STACK_FRAME.matcher(frameInfo);
//        if (matcher.matches()) {
//            String file = matcher.group(1);
//            String lineStr = matcher.group(2);
//            int lineNo = Integer.parseInt(lineStr);
//            location = new FileLocation(file, lineNo);
//        }
//
//        if (location == null) {
//            location = FileLocation.getLocation(frameInfo);
//        }
//
//        if (location != null) {
//            FileObject fo = findFile(location.file, report.getFileLocator());
//            if (fo != null) {
//                if (line == -1) {
//                    line = location.line;
//                }
//
//                BaseDocument doc = GsfUtilities.getDocument(fo, true);
//                if (doc != null) {
//                    int offset = org.netbeans.editor.Utilities.getRowStartFromLineOffset(doc, line-1);
//                    if (offset == -1) {
//                        // Invalid line number - just go to the end of the file
//                        offset = doc.getLength();
//                    }
//                    GsfUtilities.open(fo, offset, null);
//                }
//
//                return;
//            }
//        }
//
//        LOGGER.info("Could not open a file for " + frameInfo) ;
    }

    static TestsuiteNode getTestsuiteNode(Node node) {
        while (!(node instanceof TestsuiteNode)) {
            node = node.getParentNode();
        }
        return (TestsuiteNode) node;
    }

    // TODO: copied from OutputUtils, should introduce this as a utility method
    // in python.platform
    static FileObject findFile(final String path, FileLocator fileLocator) {
        if (fileLocator != null) {
            FileObject fo = fileLocator.find(path);
            if (fo != null) {
                return fo;
            }
        }

        // Perhaps it's an absolute path of some sort... try to resolve those
        // Absolute path? Happens for stack traces in Jython libraries and such
        FileObject fo = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(path));
        if (fo != null /*paranoia*/ && fo.isValid() && fo.isData()) {
            return fo;
        } else {
            LOGGER.log(Level.WARNING, "Cannot resolve file for \"{0}\" path.", path);
            return null;
        }
    }

}
