/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search.matcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class DefaultMatcherTest extends NbTestCase {

    private static final Logger LOG = Logger.getLogger(
            DefaultMatcherTest.class.getName());

    public DefaultMatcherTest(String name) {
        super(name);
    }

    public void testHasTextContent() {
        createAndCheckTextContent("UTF-8");
        createAndCheckTextContent("UTF-16");
        createAndCheckTextContent("x-UTF-32BE-BOM");
        createAndCheckTextContent("x-UTF-32LE-BOM");
    }

    private void createAndCheckTextContent(String encoding) {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        try {
            OutputStream os = root.createAndOpen("file");
            try {
                OutputStreamWriter osw = new OutputStreamWriter(os,
                        encoding);
                try {
                    osw.append("Test Text");
                    osw.flush();
                } finally {
                    osw.close();
                }
            } finally {
                os.close();
            }
            assertTrue("File with encoding " + encoding
                    + " was detected as binary file",
                    DefaultMatcher.hasTextContent(root.getFileObject("file")));
        } catch (UnsupportedEncodingException eee) {
            LOG.log(Level.INFO, "Unknown encoding {0}", encoding);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void testHasTextContentWithBinaryContent() {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        try {
            OutputStream os = root.createAndOpen("file");
            try {
                os.write(new byte[]{90, 98, 88, 97, 94, 0, 1, 2, 4, 5, 4, 6});
            } finally {
                os.close();
            }
            assertFalse("Binary file was detected as textual file",
                    DefaultMatcher.hasTextContent(root.getFileObject("file")));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
