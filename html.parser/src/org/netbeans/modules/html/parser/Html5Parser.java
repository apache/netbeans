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
package org.netbeans.modules.html.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.validator.htmlparser.impl.ErrorReportingTokenizer;
import nu.validator.htmlparser.impl.Tokenizer;
import nu.validator.htmlparser.io.Driver;
import org.netbeans.modules.html.editor.lib.api.*;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.foreign.MaskingChSReader;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service = HtmlParser.class, position = 100)
public class Html5Parser implements HtmlParser {

    @Override
    public HtmlParseResult parse(HtmlSource source, HtmlVersion preferedVersion, Lookup lookup) throws ParseException {
        try {
            CharSequence sourceCode = source.getSourceCode();
            MaskedAreas maskedAreas = lookup.lookup(MaskedAreas.class);

            InputSource is;
            //backward compatibility
            if(maskedAreas == null) {
                //pre html.editor.lib/3.2
                is = new InputSource(new StringReader(sourceCode.toString()));
            } else {
                is = new InputSource(new MaskingChSReader(sourceCode, maskedAreas.positions(), maskedAreas.lens()));
            }
            
            final ParseTreeBuilder treeBuilder = new ParseTreeBuilder(sourceCode, lookup);
            final Tokenizer tokenizer = new ErrorReportingTokenizer(treeBuilder);

            Driver driver = new Driver(tokenizer);
            driver.setTransitionHandler(treeBuilder);

            final Collection<ProblemDescription> problems = new ArrayList<ProblemDescription>();
            driver.setErrorHandler(new ErrorHandler() {
                
                @Override
                public void warning(SAXParseException saxpe) throws SAXException {
                    reportProblem(saxpe.getLocalizedMessage(), ProblemDescription.WARNING);

                }

                @Override
                public void error(SAXParseException saxpe) throws SAXException {
                    reportProblem(saxpe.getLocalizedMessage(), ProblemDescription.ERROR);
                }

                @Override
                public void fatalError(SAXParseException saxpe) throws SAXException {
                    reportProblem(saxpe.getLocalizedMessage(), ProblemDescription.FATAL);
                }

                private void reportProblem(String message, int type) {
                    problems.add(ProblemDescription.create(
                            "html5.parser", //NOI18N
                            message,
                            type,
                            treeBuilder.getOffset(),
                            treeBuilder.getOffset()));
                }
            });
            driver.tokenize(is);
            Node root = treeBuilder.getRoot();

            return new Html5ParserResult(source, root, problems, preferedVersion);

        } catch (SAXException ex) {
            throw new ParseException(ex);
        } catch (IOException ex) {
            throw new ParseException(ex);
        } catch (AssertionError e) {
            //issue #194037 handling - under some circumstances the parser may throw assertion error
            //in these cases the problem is more likely in the parser itself than in netbeans
            //To minimalize the user impact on non-fcs version (assertions enabled), 
            //just log the assertion error and return fake parser result

            StringBuilder msg = new StringBuilder();
            msg.append("An internal parser error occured"); //NOI18N
            if (source.getSourceFileObject() != null) {
                msg.append(" when parsing "); //NOI18N
                msg.append(source.getSourceFileObject().getPath());
            }

            Logger.getAnonymousLogger().log(Level.INFO, msg.toString(), e);

            ElementsFactory factory = new ElementsFactory(source.getSourceCode());
            return new Html5ParserResult(source, factory.createRoot(),
                    Collections.<ProblemDescription>emptyList(), preferedVersion);
        }
    }

    @Override
    public boolean canParse(HtmlVersion version) {
        return version == HtmlVersion.HTML5
                || version == HtmlVersion.HTML32
                || version == HtmlVersion.HTML41_STRICT
                || version == HtmlVersion.HTML41_TRANSATIONAL
                || version == HtmlVersion.HTML41_FRAMESET
                || version == HtmlVersion.HTML40_STRICT
                || version == HtmlVersion.HTML40_TRANSATIONAL
                || version == HtmlVersion.HTML40_FRAMESET;
    }

    /**
     * 
     * @deprecated 
     */
    @Override
    public HtmlModel getModel(HtmlVersion version) {
        return null;
    }

    /**
     * @deprecated 
     */
    @Override
    public String getName() {
        return null;
    }
    
    private static class Html5ParserResult extends DefaultHtmlParseResult {

        public Html5ParserResult(HtmlSource source, Node root, Collection<ProblemDescription> problems, HtmlVersion version) {
            super(source, root, problems, version);
        }

        @Override
        public HtmlModel model() {
            return HtmlModelFactory.getModel(version());
        }

        @Override
        public Collection<HtmlTag> getPossibleOpenTags(Element afterNode) {
            return ElementUtils.getPossibleOpenTags(model(), afterNode);
        }

        @Override
        public Map<HtmlTag, OpenTag> getPossibleCloseTags(Element node) {
           return ElementUtils.getPossibleCloseTags(model(), node);
        }
        
    }
    
}
