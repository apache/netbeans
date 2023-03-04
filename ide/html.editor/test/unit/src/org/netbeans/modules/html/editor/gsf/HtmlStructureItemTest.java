/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
