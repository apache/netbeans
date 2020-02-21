/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
