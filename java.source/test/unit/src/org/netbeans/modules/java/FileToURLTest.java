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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java;

import org.netbeans.junit.NbTestCase;

import java.io.File;
import java.net.URI;
import java.net.MalformedURLException;
import org.openide.util.Utilities;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
 * @todo documentation
 */
public class FileToURLTest extends NbTestCase {
    /**
     * Constructs a test case with the given name.
     *
     * @param name name of the testcase
     */
    public FileToURLTest() {
        super(FileToURLTest.class.getSimpleName());
    }

    public void testFileToURL() throws Exception{
        File file = new File("C:/DataReporter/lib/javahelp/xearhelp.jar!/Command bar.html");
        URI uri1;
        try {
            uri1 = URI.create(file.toURL().toExternalForm());
        } catch (Exception e) {
//            fail(e.getMessage());
            e.printStackTrace(getLog());
            uri1 = null;
        }
        URI uri2;
        try {
            uri2 = URI.create(Utilities.toURI(file).toURL().toExternalForm());
        } catch (Exception e) {
//            fail(e.getMessage());
            e.printStackTrace(getLog());
            uri2 = null;
        }
        URI uri3;
        try {
            uri3 = Utilities.toURI(file);
        } catch (Exception e) {
            //            fail(e.getMessage());
            e.printStackTrace(getLog());
            uri3 = null;
        }
        print("File to URL to external form: "); println(file.toURL().toExternalForm());
        print("URI1: "); println(uri1);
        print("URI2: "); println(uri2);
        print("URI3: "); println(uri3);
//        assertEquals("URI2 and URI3 aren't same.", uri2, uri3);
//        assertEquals("URI1 and URI3 aren't same.", uri1, uri3);
//        assertEquals("URI1 and URI2 aren't same.", uri1, uri2);

    }

    private void print(Object s) {
        System.out.print(s);
    }

    private void println(Object s) {
        System.out.println(s);
    }
}
