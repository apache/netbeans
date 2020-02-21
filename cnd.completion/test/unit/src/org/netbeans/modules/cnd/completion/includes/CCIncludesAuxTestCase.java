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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.includes;

import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 *
 *
 */
public class CCIncludesAuxTestCase extends CndBaseTestCase {

    private static final boolean TRACE = false;

    /**
     * Creates a new instance of CCIncludesAuxTestCase
     */
    public CCIncludesAuxTestCase(String testName) {
        super(testName);
    }

    public void testTextShrinking() throws Exception {
        String text = "/very/long/path/to/include/dir";
        CsmIncludeCompletionItem item = new CsmIncludeCompletionItem(0, 0, 0, text, "on/Unix/system", "", false, true, false);
        String shrinked = item.getRightText(true, "/");
        if (TRACE) {
            System.err.println("shrinked is " + shrinked);
        }
        assertEquals("/very/long.../Unix/system", shrinked);
        text = "C:\\very\\long\\path\\to\\include\\dir";
        item = new CsmIncludeCompletionItem(0, 0, 0, text, "on\\Windows\\system", "", false, true, false);
        shrinked = item.getRightText(true, "\\");
        if (TRACE) {
            System.err.println("shrinked is " + shrinked);
        }
        assertEquals("C:\\very\\long...\\Windows\\system", shrinked);
        text = "C:\\very\\long\\path\\to\\mixed/include/dir";
        item = new CsmIncludeCompletionItem(0, 0, 0, text, "on/Windows//mixed", "", false, true, false);
        shrinked = item.getRightText(true, "\\");
        if (TRACE) {
            System.err.println("shrinked is " + shrinked);
        }
        assertEquals("C:\\very\\long...\\\\mixed", shrinked);
        text = "/very/long/path/to\\include\\mixed\\dir";
        item = new CsmIncludeCompletionItem(0, 0, 0, text, "on/unix/mixed", "", false, true, false);
        shrinked = item.getRightText(true, "/");
        if (TRACE) {
            System.err.println("shrinked is " + shrinked);
        }
        assertEquals("/very/long.../unix/mixed", shrinked);
    }
}
