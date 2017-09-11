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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.openide.util;

import java.util.HashMap;
import junit.framework.*;
import org.openide.util.MapFormat;

/**
 *
 * @author Jaroslav Tulach
 */
public class MapFormatTest extends TestCase {

    public MapFormatTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    public void testFormatIssue67238() {

        HashMap args = new HashMap();
        args.put("NAME", "Jaroslav");

        MapFormat f = new MapFormat(args);
        f.setLeftBrace("__");
        f.setRightBrace("__");
        f.setExactMatch(false);
        String result = f.format("/*_____________________*/\n/*__NAME__*/");
        
        assertEquals("Should be ok: " + result, "/*_____________________*/\n/*Jaroslav*/", result);
    }
    
    public void testExectLineWithTheProblemFromFormatIssue67238 () {
        String s = "/*___________________________________________________________________________*/";
        
        HashMap args = new HashMap();
        args.put("NAME", "Jaroslav");

        MapFormat f = new MapFormat(args);
        f.setLeftBrace("__");
        f.setRightBrace("__");
        f.setExactMatch(false);
        String result = f.format(s);
        
        assertEquals("Should be ok: " + result, s, result);
        
    }
    
    public void testIssue67238() {
        final String s = "/*___________________________________________________________________________*/";
        HashMap args = new HashMap();
        args.put("NAME", "Jaroslav");
        MapFormat f = new MapFormat(args) {
            protected Object processKey(String key) {
                fail("There is no key in \"" + s + "\", processKey() should not be called with key:" + key);
                return "not defined";
            }
        };
        f.setLeftBrace("__");
        f.setRightBrace("__");
        f.setExactMatch(false);
        f.format(s);
    }
}
