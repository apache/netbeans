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

import java.nio.charset.Charset;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class AbstractMatcherTest extends NbTestCase {

    private SearchPattern searchPattern;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        searchPattern = SearchPattern.create("a", false, false, false);
    }

    @Override
    protected void tearDown() throws Exception {
        searchPattern = null;
        super.tearDown();
    }

    public AbstractMatcherTest(String name) {
        super(name);
    }

    /**
     * Check that an exception is thrown if a decoding error occurs.
     */
    public void testStrictModeOn() {
        testStrinctMode(true, true);
    }

    /**
     * Check that no expception is thrown if a decoding error occurs.
     */
    public void testStrictModeOff() {
        testStrinctMode(false, false);
    }

    private void testStrinctMode(boolean strict, boolean expectException) {
        MockServices.setServices(Utf8FileEncodingQueryImpl.class);
        FileObject dir = FileUtil.toFileObject(getDataDir());
        assertNotNull(dir);
        FileObject file = dir.getFileObject("textFiles/latin2file.txt");
        AbstractMatcher am = new DefaultMatcher(searchPattern);
        am.setStrict(strict);
        assertTrue(file.isValid());
        ExceptionListener el = new ExceptionListener();
        am.check(file, el);
        assertEquals(expectException, el.exception);
    }

    /**
     * Listener storing information about decoding problems.
     */
    private class ExceptionListener extends SearchListener {

        private boolean exception = false;

        @Override
        public void fileContentMatchingError(String fileName,
                Throwable throwable) {
            exception = true;
        }
    }

    /**
     * Set incorrect encoding for ISO-8859-2 files.
     */
    public static class Utf8FileEncodingQueryImpl
            extends FileEncodingQueryImplementation {

        @Override
        public Charset getEncoding(FileObject file) {
            if (file.getName().equals("latin2file")) {
                return Charset.forName("UTF-8");
            } else {
                return null;
            }
        }
    }
}
