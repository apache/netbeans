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
package org.netbeans.modules.web.el;

import com.sun.el.parser.Node;
import java.util.logging.Logger;
import javax.el.ELException;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.web.common.api.Constants;

/**
 * Parser for Expression Language, uses {@link com.sun.el.parser.ELParser} underneath.
 *
 * @author Erno Mononen
 */
public final class ELParser extends Parser {
    
    //XXX hack
    private static final String ATTRIBUTE_EL_MARKER = "A"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(ELParser.class.getName());
    private final Document document;
    private ELParserResult result;

    private ELParser(Document document) {
        this.document = document;
    }

    public ELParser() {
        this(null);
    }

    public static ELParser create(final Document document) {
        return new ELParser(document);
    }
    
    /**
     * Parses the given EL expression and returns the root AST node for it.
     *
     * @param expr the expression to parse.
     * @return the root AST node
     * @throws {@link javax.el.ELException} if the given expression is not valid EL.
     */
    public static Node parse(ELPreprocessor expr) {
        return com.sun.el.parser.ELParser.parse(expr.getPreprocessedExpression());
    }
    
    //for unit tests
    static Node parse(String expr) {
        return com.sun.el.parser.ELParser.parse(expr);
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        this.result = new ELParserResult(snapshot);

       final String expressionSeparator = Constants.LANGUAGE_SNIPPET_SEPARATOR; //NOI18N
       String[] sources = snapshot.getText().toString().split(expressionSeparator); //NOI18N
       int embeddedOffset = 0;
       for (String expression : sources) {
           int startOffset = embeddedOffset;
           int endOffset = startOffset + expression.length();
           embeddedOffset += (expression.length() + expressionSeparator.length());
           
           ELPreprocessor elPreprocessor;
           //hack - we need to distinguish EL inside and outside of attribute values
           //since there's no API in parsing.api how to set some metadata to the virtual source
           //it is done this ugly way
           if(expression.endsWith(ATTRIBUTE_EL_MARKER)) {
               //inside attribute
               endOffset--;
               expression = expression.substring(0, expression.length() - 1);
               elPreprocessor = new ELPreprocessor(expression, 
                       ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE, 
                       ELPreprocessor.ESCAPED_CHARACTERS);
           } else {
               elPreprocessor = new ELPreprocessor(expression, 
                       ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);               
           }
           OffsetRange embeddedRange = new OffsetRange(startOffset, endOffset);
           try {
               Node node = parse(elPreprocessor);
               result.addValidElement(node, elPreprocessor, embeddedRange);
           } catch (ELException ex) {
               result.addErrorElement(ex, elPreprocessor, embeddedRange);
           }
       }

    }

    @Override
    public Result getResult(Task task) throws ParseException {
        assert result != null;
        return result;
    }

    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }
}
