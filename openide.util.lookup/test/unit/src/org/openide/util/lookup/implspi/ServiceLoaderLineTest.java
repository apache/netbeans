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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.openide.util.lookup.implspi;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;

public class ServiceLoaderLineTest extends NbTestCase {

    public ServiceLoaderLineTest(String n) {
        super(n);
    }

    public void testParseAndRewriteLines() throws Exception {
        assertEquals("", parseAndRewrite(""));
        assertEquals("a ", parseAndRewrite("a "));
        assertEquals("a b ", parseAndRewrite("b a "));
        assertEquals("a b ", parseAndRewrite("b a b "));
        assertEquals("a #position=1 c #position=2 b #position=3 ", parseAndRewrite("a #position=1 b #position=3 c #position=2 "));
        assertEquals("b #position=55 a c ", parseAndRewrite("a b #position=55 c "));
        assertEquals("a #position=1 b #position=1 ", parseAndRewrite("b #position=1 a #position=1 "));
        assertEquals("a #-b c d #-e #-f ", parseAndRewrite("d #-e #-f a #-b c "));
    }

    private static String parseAndRewrite(String input) throws IOException {
        SortedSet<ServiceLoaderLine> lines = new TreeSet<ServiceLoaderLine>();
        ServiceLoaderLine.parse(new StringReader(input.replace(' ', '\n')), lines);
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        for (ServiceLoaderLine line : lines) {
            line.write(pw);
        }
        pw.flush();
        return w.toString().replace('\n', ' ').replace("\r", "");
    }

    public static void clearLookupsForPath() {
        NamedServicesProvider.clearCache();
    }
}
