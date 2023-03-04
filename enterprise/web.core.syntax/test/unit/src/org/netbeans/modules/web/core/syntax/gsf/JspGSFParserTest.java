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
package org.netbeans.modules.web.core.syntax.gsf;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import static org.junit.Assert.*;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.web.core.syntax.parser.JspSyntaxElement;
import org.netbeans.test.web.core.syntax.TestBase;

/**
 *
 * @author marekfukala
 */
public class JspGSFParserTest extends TestBase {

    public JspGSFParserTest(String name) {
        super(name);
    }

    public void testParser() throws ParseException {
        String content = "<%@page import=\"java.util.List\" %><%-- comment --%><jsp:useBean class=\"java.util.List\" >content</jsp:useBean>";
        Document doc = getDocument(content);
        Source source = Source.create(doc);
        final AtomicReference<Result> resultRef = new AtomicReference<Result>();
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                resultRef.set(resultIterator.getParserResult());
            }
        });

        Result result = resultRef.get();
        assertNotNull(result);
        assertTrue(result instanceof JspParserResult);

        JspParserResult jspresult = (JspParserResult) result;
        List<JspSyntaxElement> elements = jspresult.elements();
        assertNotNull(elements);
        assertEquals(5, elements.size());

        Iterator<JspSyntaxElement> els = elements.iterator();
        
        JspSyntaxElement el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.DIRECTIVE, el.kind());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.COMMENT, el.kind());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.OPENTAG, el.kind());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.TEXT, el.kind());

        el = els.next();
        assertNotNull(el);
        assertEquals(JspSyntaxElement.Kind.ENDTAG, el.kind());
            
    }
}
