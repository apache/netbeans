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
