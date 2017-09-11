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
package org.netbeans.modules.html.editor.indexing;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.common.api.FileReference;

/**
 *
 * @author marekfukala
 */
public class HtmlFileModelTest extends TestBase {

    public HtmlFileModelTest(String name) {
        super(name);
    }

    //Bug 211073 - NullPointerException at org.netbeans.modules.html.editor.indexing.HtmlFileModel$ReferencesSearch.visit
    public void testIssue211073() throws Exception {
        Document doc = getDocument(getTestFile("testfiles/model/test1.html"));
        Source source = Source.create(doc);
        HtmlFileModel model = new HtmlFileModel(source);

        //the erroneous references are not used
        assertEquals(0, model.getReferences().size());
    }

    public void testReferences() throws Exception {
        Document doc = getDocument(getTestFile("testfiles/model/test2.html"));
        Source source = Source.create(doc);
        HtmlFileModel model = new HtmlFileModel(source);

        List<HtmlLinkEntry> entries = model.getReferences();
        assertEquals(1, entries.size());
        HtmlLinkEntry entry = entries.get(0);
        
        assertEquals("link", entry.getTagName());
        FileReference ref = entry.getFileReference();
        assertNotNull(ref);
        
        assertEquals("test1.html", ref.linkPath());
        
    }
    
    public void testEmbeddedCSSSections() throws Exception {
        Document doc = getDocument(getTestFile("testfiles/model/test3.html"));
        Source source = Source.create(doc);
        HtmlFileModel model = new HtmlFileModel(source);

        List<OffsetRange> entries = model.getEmbeddedCssSections();
        assertEquals(2, entries.size());
        OffsetRange entry = entries.get(0);
        
        //first section
        assertNotNull(entry);
        assertEquals(221, entry.getStart());
        assertEquals(295, entry.getEnd());

        //second section
        entry = entries.get(1);
        assertNotNull(entry);
        assertEquals(335, entry.getStart());
        assertEquals(411, entry.getEnd());
        
    }
}
