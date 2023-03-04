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
package org.netbeans.modules.html.editor.api.gsf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.html.editor.lib.api.ParseResult;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationException;
import org.netbeans.modules.html.editor.lib.api.validation.Validator;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationContext;
import org.netbeans.modules.html.editor.lib.api.validation.ValidationResult;
import org.netbeans.modules.html.editor.lib.api.validation.ValidatorService;
import org.netbeans.modules.html.editor.gsf.HtmlParserResultAccessor;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.MaskedAreas;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.foreign.MaskingChSReader;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * HTML parser result
 *
 * @author mfukala@netbeans.org
 */
public class HtmlParserResult extends ParserResult implements HtmlParsingResult {

    /**
     * Used as a key of a swing document to find a default fallback dtd.
     */
    public static final String FALLBACK_DTD_PROPERTY_NAME = "fallbackDTD";
    private final SyntaxAnalyzerResult result;
    private List<Error> errors;
    private final AtomicBoolean isValid = new AtomicBoolean(true);

    private HtmlParserResult(SyntaxAnalyzerResult result) {
        super(result.getSource().getSnapshot());
        this.result = result;
    }
    
    @Override
    public SyntaxAnalyzerResult getSyntaxAnalyzerResult() {
        return result;
    }

    /** The parser result may be invalidated by the parsing infrastructure.
     * In such case the method returns false.
     * @return true for valid result, false otherwise.
     */
    public boolean isValid() {
        return isValid.get();
    }

    /**
     * Returns an html version for the specified parser result input.
     * The return value depends on:
     * 1) doctype declaration content
     * 2) if not present, xhtml file extension
     * 3) if not xhtml extension, present of default XHTML namespace declaration
     *
     * @return instance of {@link HtmlVersion}
     */
    @Override
    public HtmlVersion getHtmlVersion() {
        return result.getHtmlVersion();
    }

    @Override
    public HtmlVersion getDetectedHtmlVersion() {
        return result.getDetectedHtmlVersion();
    }

    /** @return a root node of the hierarchical parse tree of the document.
     * basically the tree structure is done by postprocessing the flat parse tree
     * you can get by calling elementsList() method.
     * Use the flat parse tree results if you do not need the tree structure since
     * the postprocessing takes some time and is done lazily.
     */
    @Override
    public Node root() {
        try {
            return result.parseHtml().root();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public Node rootOfUndeclaredTagsParseTree() {
        try {
            return result.parseUndeclaredEmbeddedCode().root();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /** 
     * @param namespace
     * @return  a parse tree for non-html content 
     */
    @Override
    public Node root(String namespace) {
        try {
            ParseResult pr = result.parseEmbeddedCode(namespace);
            assert pr != null : "Cannot get ParseResult for " + namespace; //NOI18N
            return pr.root();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /** 
     * @return a map of all namespaces to astnode roots
     */
    @Override
    public Map<String, Node> roots() {
        Map<String, Node> roots = new HashMap<>();
        for (String uri : getNamespaces().keySet()) {
            roots.put(uri, root(uri));
        }

        //non xhtml workaround, add the default namespaces if missing
        if (!roots.containsValue(root())) {
            roots.put(null, root());
        }

        return roots;

    }

    /**
     * @return a map of declared uri to prefix map 
     */
    @Override
    public Map<String, String> getNamespaces() {
        return result.getDeclaredNamespaces();
    }

    private Collection<Node> getAllRoots() {
        Collection<Node> allRoots = new ArrayList<>();
        allRoots.add(root());
        allRoots.addAll(roots().values());
        allRoots.add(root(SyntaxAnalyzerResult.FILTERED_CODE_NAMESPACE));
        allRoots.add(rootOfUndeclaredTagsParseTree());
        
        return allRoots;
    }
    
    public Node findBySemanticRange(int offset, boolean forward) {
        Node mostLeaf = null;
        for (Node root : getAllRoots()) {
            Node leaf = ElementUtils.findBySemanticRange(root, offset, forward);
            if (leaf == null) {
                continue;
            }
            if (mostLeaf == null) {
                mostLeaf = leaf;
            } else {
                //they cannot overlap, just be nested, at least I think
                if (leaf.from() > mostLeaf.from()) {
                    mostLeaf = leaf;
                }
            }
        }
        return mostLeaf;
    }
    
    public Element findByPhysicalRange(int offset, boolean forward) {
        Element mostLeaf = null;
        for (Node root : getAllRoots()) {
            Element leaf = ElementUtils.findByPhysicalRange(root, offset, forward);
            if (leaf == null) {
                continue;
            }
            if (mostLeaf == null) {
                mostLeaf = leaf;
            } else {
                //they cannot overlap, just be nested, at least I think
                if (leaf.from() > mostLeaf.from()) {
                    mostLeaf = leaf;
                }
            }
        }
        return mostLeaf;
    }
    
    @Override
    public List<? extends Error> getDiagnostics() {
        //provide the validator errors to the parser results' diagnostic only 
        //if they really are severe and *real* errors, e.g. only fatal errors and
        //not in embedded html
        return getSnapshot().getMimePath().size() == 1 
                ? getDiagnostics(EnumSet.of(Severity.FATAL))
                : Collections.<Error>emptyList();
    }

    @Override
    protected void invalidate() {
        isValid.set(false);
    }

    public List<Error> getDiagnostics(Set<Severity> severities) {
        List<Error> filtered = new ArrayList<>();
        for(Error e : getValidationResults()) {
            if(severities.contains(e.getSeverity())) {
                filtered.add(e);
            }
        }
        return filtered;
    }
    
    private synchronized List<Error> getValidationResults() {
        if(errors == null) {
            errors = new ArrayList<>();
            errors.addAll(getErrorsFromValidatorService());
        }
        return errors;
    }
    
    private List<Error> getErrorsFromValidatorService() {
        FileObject file = getSnapshot().getSource().getFileObject();
        try {
            //use the filtered snapshot or use the namespaces filtering facility in the nu.validator
            Validator validator = ValidatorService.getValidator(getHtmlVersion());
            if(validator == null) {
                return Collections.emptyList();
            }
            HtmlSource source = new HtmlSource(getSnapshot());
            MaskedAreas maskedAreas = result.getMaskedAreas(source, SyntaxAnalyzerResult.FilteredContent.CUSTOM_TAGS);
            CharSequence original = getSnapshot().getText().toString();
            MaskingChSReader masker = new MaskingChSReader(original, maskedAreas.positions(), maskedAreas.lens());
            
            ValidationContext context = new ValidationContext(masker, getHtmlVersion(), file, result);

            //XXX possibly make it configurable via hints
            context.enableFeature("filter.foreign.namespaces", true); //NOI18N
            
            ValidationResult res = validator.validate(context);

            //Filter out errors from elements which contains masked areas.
            //
            //For example <img ng-src="{{mainImageUrl}}"> causes the validator
            //to complain about missing src attribute, which is in fact generated
            //in runtime by AngularJS
            List<Error> errs = new ArrayList<>();
            int[] positions = maskedAreas.positions();
            int[] lens = maskedAreas.lens();
            for (ProblemDescription pd : res.getProblems()) {
                int from = pd.getFrom();
                int to = pd.getTo();
                
                int idx = Arrays.binarySearch(positions, from);
                if(idx < 0) {
                    idx = -idx - 1; //points to the index of the closest higher position
                }
                //if the index is higher than the arr len it means there's no higher position
                if(idx < positions.length) {
                    int pos = positions[idx];
                    if(pos + lens[idx] <= to) {
                        //match - filter out
                        continue;
                    }
                }
                
                DefaultError error = new DefaultError(pd.getKey(),
                        pd.getText(), //NOI18N
                        pd.getText(),
                        res.getContext().getFile(),
                        pd.getFrom(),
                        pd.getTo(),
                        false,
                        forProblemType(pd.getType()));

                errs.add(error);
            }
            return errs;

        } catch (ValidationException ex) {
            Logger.getAnonymousLogger().log(Level.INFO, "An error occured during html code validation", ex);

            DefaultError error = new DefaultError("validator.error",
                    "validator.error",
                    "An internal error occured during validating the code: " + ex.getLocalizedMessage(),
                    file, 0,0, true, Severity.ERROR);

            return Collections.<Error>singletonList(error);
        }

    }

    private static Severity forProblemType(int problemtype) {
        switch (problemtype) {
            case ProblemDescription.INFORMATION:
                return Severity.INFO;
            case ProblemDescription.WARNING:
                return Severity.WARNING;
            case ProblemDescription.ERROR:
                return Severity.ERROR;
            case ProblemDescription.FATAL:
                return Severity.FATAL;
            case ProblemDescription.INTERNAL_ERROR:
                return Severity.INFO;
            default:
                throw new IllegalArgumentException("Invalid ProblemDescription type: " + problemtype); //NOI18N
        }

    }

    public static Node getBoundNode(Error e) {
        if (e instanceof DefaultError) {
            Object[] parameters = e.getParameters();
            if (parameters != null && parameters.length > 0 && parameters[0] instanceof Node) {
                return (Node) e.getParameters()[0];
            }
        }

        return null;
    }

    static {
        HtmlParserResultAccessor.set(new Accessor());
    }
    
    /**
     * This class is actually returned as Parser.Result instance for clients.
     * It implements Lookup.Provider, so that web.common module may pass the 
     * SyntaxAnalyzerResult on to the metadata providers. The only current user
     * of the metadata API attempts to look up SyntaxAnalyzerResult directly.
     * The addition of Lookup.Provider need not to be visible in APIs.
     */
    private static final class Lkp extends HtmlParserResult implements Lookup.Provider {
        private Lookup lkp;
        
        private Lkp(SyntaxAnalyzerResult result) {
            super(result);
        }

        @Override
        public Lookup getLookup() {
            if (lkp == null) {
                lkp = Lookups.fixed(getSyntaxAnalyzerResult());
            }
            return lkp;
        }
    }

    private static class Accessor extends HtmlParserResultAccessor {

        @Override
        public HtmlParserResult createInstance(SyntaxAnalyzerResult result) {
            return new Lkp(result);
        }
    }
}
