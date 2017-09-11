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
package org.netbeans.modules.html.editor.gsf;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class HtmlStructureItemTest extends TestBase {

    public HtmlStructureItemTest(String name) {
        super(name);
    }

     public void testBasic() throws ParseException, IOException, org.netbeans.modules.parsing.spi.ParseException {
        String code = "<table><tr><td>1</td><td>2</td></tr></table><div>text</div>";
        //             0123456789012345678901234567890123456789012345678901234567890123456789
        //             0         1         2         3         4         5
        final FileObject file = createFile("test.html", code);
        
        Source source = Source.create(file);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                HtmlParserResult result = (HtmlParserResult)resultIterator.getParserResult();
                
                Node root = result.root();

                OpenTag table = ElementUtils.query(root, "html/body/table");
                assertNotNull(table);

                HtmlElementHandle handle = new HtmlElementHandle(table, result.getSnapshot().getSource().getFileObject());

                HtmlStructureItem si = new HtmlStructureItem(table, handle, result.getSnapshot());

                assertEquals(handle, si.getElementHandle());
                
                assertEquals("table", si.getName());

                assertEquals(0, si.getPosition());
                assertEquals(44, si.getEndPosition());

                assertFalse(si.isLeaf());

                List<? extends StructureItem> children = si.getNestedItems();
                assertNotNull(children);
                assertEquals(1, children.size());
                
                StructureItem trSi = children.iterator().next();
                assertEquals("tr", trSi.getName());
                
                //test equality
                HtmlStructureItem si2 = new HtmlStructureItem(table, handle, result.getSnapshot());
                assertEquals(si, si2);
                
            }
        });
        
    }
     
     public void testCommentFolds() throws ParseException, IOException, org.netbeans.modules.parsing.spi.ParseException {
        String code = "<div>"
                + "\n<!-- \n"
                + "comment\n"
                + " -->\n"
                + "</div>";
        final HtmlStructureScanner scanner = new HtmlStructureScanner();
        final FileObject file = createFile("test.html", code);
        Document doc = getDocument(file);
        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                HtmlParserResult result = (HtmlParserResult)resultIterator.getParserResult();
                Map<String, List<OffsetRange>> folds = scanner.folds(result);
                List<OffsetRange> comments = folds.get(HtmlStructureScanner.TYPE_COMMENT.code());
                assertNotNull(comments);
                assertEquals(1, comments.size());
                OffsetRange comment = comments.get(0);
                assertNotNull(comment);
                assertEquals(6, comment.getStart());
                assertEquals(24, comment.getEnd());
            }
        });
     }
    
    
}
