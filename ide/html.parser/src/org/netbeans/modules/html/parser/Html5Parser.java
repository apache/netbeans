/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import nu.validator.htmlparser.common.XmlViolationPolicy;
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
            // Needed for HTML5 parsing - without this, parser mangles
            // names to comply with XML rules
            treeBuilder.setNamePolicy(XmlViolationPolicy.ALLOW);
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
    @Deprecated
    public HtmlModel getModel(HtmlVersion version) {
        return null;
    }

    /**
     * @deprecated 
     */
    @Override
    @Deprecated
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
