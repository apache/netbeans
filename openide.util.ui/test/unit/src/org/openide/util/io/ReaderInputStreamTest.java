/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.util.io;

import java.io.IOException;
import java.io.StringReader;
import org.netbeans.junit.NbTestCase;

public class ReaderInputStreamTest extends NbTestCase {

    public ReaderInputStreamTest(String name) {
        super(name);
    }

    public void testZeroLengthRead() throws Exception { // #137881
        assertEquals(0, new ReaderInputStream(new StringReader(("abc"))).read(new byte[256], 0, 0));
    }

    public void testTextDataRead() throws IOException {
        ReaderInputStream ris = new ReaderInputStream(new StringReader("0123456789"), "UTF-8");
        assertEquals("Wrong number of bytes read.", 10, ris.read(new byte[10], 0, 10));
    }

    /** Tests stream doesn't hang with invalid input data (see #153987). */
    public void testBinaryDataRead() throws IOException {
        ReaderInputStream ris = new ReaderInputStream(new StringReader(""+((char)0xD8FF)), "UTF-8");
        try {
            ris.read(new byte[10], 0, 10);
            fail("ReaderInputStream should refuse input characteres between \\uD800 and \\uDBFF (see OutputStreamWriter javadoc).");
        } catch (IOException e) {
            // OK
        }
    }
}
