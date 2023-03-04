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
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author marekfukala
 */
public class HtmlElementHandleTest extends TestBase {

    public HtmlElementHandleTest(String name) {
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

                assertEquals(file, handle.getFileObject());
                assertEquals("text/html", handle.getMimeType());
                assertEquals("table", handle.getName());

                OffsetRange range = handle.getOffsetRange(result);
                assertNotNull(range);
                
                assertEquals(0, range.getStart());
                assertEquals(44, range.getEnd());
                
                Node resolved = handle.resolve(result);
                assertNotNull(resolved);
                assertEquals(table, resolved);
                
                //test equality
                HtmlElementHandle handle2 = new HtmlElementHandle(table, result.getSnapshot().getSource().getFileObject());
                assertEquals(handle, handle2);
                
            }
        });
        
    }
    
}
