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
package org.netbeans.modules.javascript2.doc;

import java.util.Set;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationReaderTest extends CslTestBase {

    public JsDocumentationReaderTest(String name) {
        super(name);
    }

    public void testGetCommentTags() throws Exception {
        String commentText = "/**\n"
                + " * Construct a new Shape object.\n"
                + " * @class This is the basic {@link Shape} class.\n"
                + " * It can be considered an abstract class, even though no such thing\n"
                + " * really existing in JavaScript\n"
                + " * @constructor\n"
                + " * @throws MemoryException if there is no more memory\n"
                + " * @throws GeneralShapeException rarely (if ever)\n"
                + " * @return {Shape|Coordinate} A new shape.\n"
                + " */";
        Set<String> commentTags = JsDocumentationReader.getCommentTags(commentText);
        assertEquals(5, commentTags.size());
        assertTrue(commentTags.contains("@class"));
        assertTrue(commentTags.contains("@link"));
        assertTrue(commentTags.contains("@constructor"));
        assertTrue(commentTags.contains("@throws"));
        assertTrue(commentTags.contains("@return"));
    }

    public void testGetAllTags() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/doc/commonDocFile.js"));
        Set<String> allTags = JsDocumentationReader.getAllTags(source.createSnapshot());
        assertEquals(25, allTags.size());
        // randomly check several tags
        assertTrue(allTags.contains("@param"));
        assertTrue(allTags.contains("@example"));
        assertTrue(allTags.contains("@author"));
        assertTrue(allTags.contains("@field"));
        assertTrue(allTags.contains("@version"));
        assertTrue(allTags.contains("@see"));
    }
}
