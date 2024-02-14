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
package org.netbeans.modules.html.editor.hints;

import org.netbeans.modules.html.editor.hints.other.SurroundWithTag;
import org.netbeans.modules.html.editor.hints.other.RemoveSurroundingTag;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.editor.NbEditorDocument;
import static org.netbeans.modules.html.editor.HtmlErrorFilter.*;
import org.netbeans.modules.html.editor.HtmlExtensions;
import org.netbeans.modules.html.editor.HtmlPreferences;
import org.netbeans.modules.html.editor.ProjectDefaultHtmlSourceVersionController;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.ErrorBadgingRule;
import org.netbeans.modules.html.editor.api.gsf.HtmlErrorFilterContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.hints.other.ExtractInlinedStyleHint;
import org.netbeans.modules.html.editor.hints.other.ExtractInlinedStyleRule;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.web.common.api.WebPageMetadata;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 */
public class HtmlHintsProvider implements HintsProvider {

    private static RequestProcessor RP = new RequestProcessor(HtmlHintsProvider.class);

    private static final Logger LOG = Logger.getLogger(HtmlHintsProvider.class.getName());
    /**
     * Compute hints applicable to the given compilation info and add to the
     * given result list.
     */
    @Override
    public void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
        HtmlParserResult result = (HtmlParserResult) context.parserResult;
        HtmlVersion version = result.getDetectedHtmlVersion();
        FileObject file = result.getSnapshot().getSource().getFileObject();
        if (file == null) {
            //the Hint doesn't allow the fileObject argument to be null
            return;
        }
        Project project = FileOwnerQuery.getOwner(file);
        boolean xhtml = result.getSyntaxAnalyzerResult().mayBeXhtml();
        if (version == null) {
            //the version can be determined

            if (project == null) {
                //we cannot set the default anywhere, just show a warning message

                hints.add(new Hint(getRule(Severity.INFO),
                        NbBundle.getMessage(HtmlHintsProvider.class, "MSG_CANNOT_DETERMINE_HTML_VERSION_NO_PROJECT"),
                        file,
                        new OffsetRange(0, 0),
                        Collections.<HintFix>emptyList(),
                        100) {
                });
            } else {
                //no doctype declaration found, generate the set default project html version hint
                HtmlVersion defaulted = ProjectDefaultHtmlSourceVersionController.getDefaultHtmlVersion(project, xhtml);
                String msg = defaulted == null
                        ? NbBundle.getMessage(HtmlHintsProvider.class, xhtml ? "MSG_CANNOT_DETERMINE_XHTML_VERSION" : "MSG_CANNOT_DETERMINE_HTML_VERSION")
                        : NbBundle.getMessage(HtmlHintsProvider.class, xhtml ? "MSG_CANNOT_DETERMINE_XHTML_VERSION_DEFAULTED_ALREADY" : "MSG_CANNOT_DETERMINE_HTML_VERSION_DEFAULTED_ALREADY", defaulted.getDisplayName());

                hints.add(new Hint(getRule(Severity.INFO),
                        msg,
                        file,
                        new OffsetRange(0, 0),
                        generateSetDefaultHtmlVersionHints(project, result.getSnapshot().getSource().getDocument(false), xhtml),
                        100) {
                });
            }
        }

        //add html-css related hints
//        HtmlCssHints.computeHints(manager, context, hints);

    }

    private static List<HintFix> generateSetDefaultHtmlVersionHints(Project project, Document doc, boolean xhtml) {
        List<HintFix> fixes = new LinkedList<>();
        if (project != null) {
            for (HtmlVersion v : HtmlVersion.values()) {
                if (xhtml == v.isXhtml()) {
                    fixes.add(new SetDefaultHtmlVersionHintFix(v, project, doc, xhtml));
                }
            }
        }

        return fixes;
    }

    /**
     * Compute any suggestions applicable to the given caret offset, and add to
     * the given suggestion list.
     */
    @Override
    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {
        int errorType = 0;
        if (context instanceof HtmlErrorFilterContext) {
            errorType = ((HtmlErrorFilterContext) context).isOnlyBadging() ? 2 : 1;
        }
        HtmlParserResult result = (HtmlParserResult) context.parserResult;
        SyntaxAnalyzerResult saresult = result.getSyntaxAnalyzerResult();
        
        if (isErrorCheckingEnabled(result)) {
            HtmlRuleContext htmlRuleContext = new HtmlRuleContext(result, saresult, Collections.<HintFix>emptyList());

            for (org.netbeans.modules.html.editor.hints.HtmlRule rule : getSortedRules(manager, context, true)) { //line hints
                //skip the rule if we are called from the tasklist,
                //the rule is not supposed to show in tasklist and is not badging
                if(errorType > 0 && !rule.showInTasklist() && !(rule instanceof ErrorBadgingRule)) {
                    continue;
                }
                // do not run regular rules when only error badging, or vice versa
                if ((errorType == 2) != (rule instanceof ErrorBadgingRule)) {
                    continue;
                }
                if(manager.isEnabled(rule)) {
                    rule.run(htmlRuleContext, suggestions);
                }
            }
        }
        
        //TODO remove the hardcoding - put the rules to the layer
        if(ExtractInlinedStyleRule.SINGLETON.appliesTo(context)) {
            suggestions.add(new ExtractInlinedStyleHint(context, new OffsetRange(context.caretOffset, context.caretOffset)));
        }
        if(RemoveSurroundingTag.RULE.appliesTo(context)) {
            suggestions.add(new RemoveSurroundingTag(context, new OffsetRange(context.caretOffset, context.caretOffset)));
        }
        
        //html extensions
        String sourceMimetype = WebPageMetadata.getContentMimeType(result, true);
        for (HtmlExtension ext : HtmlExtensions.getRegisteredExtensions(sourceMimetype)) {
            ext.computeSuggestions(manager, context, suggestions, caretOffset);
        }
        
    }

    /**
     * Compute any suggestions applicable to the given caret offset, and add to
     * the given suggestion list.
     */
    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> suggestions, int start, int end) {
        //html extensions
        HtmlParserResult result = (HtmlParserResult)context.parserResult;
        String sourceMimetype = WebPageMetadata.getContentMimeType(result, true);
        for (HtmlExtension ext : HtmlExtensions.getRegisteredExtensions(sourceMimetype)) {
            ext.computeSelectionHints(manager, context, suggestions, start, end);
        }

        suggestions.add(new SurroundWithTag(context, new OffsetRange(start, end)));
        suggestions.add(new RemoveSurroundingTag(context, new OffsetRange(start, end)));
    }

    /**
     * Process the errors for the given compilation info, and add errors and
     * warning descriptions into the provided hint list. Return any errors that
     * were not added as error descriptions (e.g. had no applicable error rule)
     */
    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        int errorType = 0;
        // in the case the context is a regular one, not for indexing, all enabled hints should run.
        if (context instanceof HtmlErrorFilterContext) {
            errorType = ((HtmlErrorFilterContext) context).isOnlyBadging() ? 2 : 1;
        }
        LOG.log(Level.FINE, "computing errors (errorType:{0}) for source {1}", new Object[]{errorType, context.parserResult.getSnapshot().getSource()});
        LOG.log(Level.FINER, null, new Exception());
        
        HtmlParserResult result = (HtmlParserResult) context.parserResult;
        SyntaxAnalyzerResult saresult = result.getSyntaxAnalyzerResult();
        Snapshot snapshot = result.getSnapshot();
        FileObject fo = snapshot.getSource().getFileObject();

        if (fo == null) {
            //the Hint doesn't allow the fileObject argument to be null
            return;
        }

        //add default fixes
        List<HintFix> defaultFixes = new ArrayList<>(3);
        if (!isErrorCheckingDisabledForFile(result)) {
            defaultFixes.add(new DisableErrorChecksFix(snapshot));
        }
        if (isErrorCheckingEnabledForMimetype(result)) {
            defaultFixes.add(new DisableErrorChecksForMimetypeFix(saresult));
        }

        HtmlRuleContext htmlRuleContext = new HtmlRuleContext(result, saresult, defaultFixes);

        //filter out fatal errors and remove them from the html validator hints processing
        Collection<Error> fatalErrors = new ArrayList<>();
        for (Error e : htmlRuleContext.getLeftDiagnostics()) {
            if (e.getSeverity() == Severity.FATAL) {
                fatalErrors.add(e);
            }
        }
        //To resolve following 
        //Bug 200801 - Fatal error hint for mixed php/html code
        //but keep the behavior described in 
        //Bug 199104 - No error for unmatched <div> tag 
        //I need to keep the fatal errors enabled only for something which is xml-like
        //
        //Really proper solution would be to introduce a facility which would filter
        //out the error messages selectively and keep just those whose cannot be 
        //false errors caused by a templating language. The tags pairing in facelets
        //is nice example as described in the issue above.
        if (isXmlBasedMimetype(saresult)) {
            for (Error e : fatalErrors) {
                //remove the fatal error from the list of errors for further processing
                htmlRuleContext.getLeftDiagnostics().remove(e);

                // FatalHtmlRule does not implement ErrorBadgingRule - will not produce error badge.
                if (errorType > 1) {
                    continue;
                }

                String message = new StringBuilder().append(e.getDescription()).append('\n').append(NbBundle.getMessage(HtmlValidatorRule.class, "MSG_FatalHtmlErrorAddendum")).toString();
                //add a special hint for the fatal error
                // TODO - should FatalHtmlRule implement ErrorBadginRule 
                Hint fatalErrorHint = new Hint(new FatalHtmlRule(),
                        message,
                        fo,
                        EmbeddingUtil.getErrorOffsetRange(e, snapshot),
                        Collections.<HintFix>emptyList(),
                        5);//looks like lower number o the priority means higher priority

                hints.add(fatalErrorHint);
            }
        }

        //now process the non-fatal errors
        if (isErrorCheckingEnabled(result)) {

            for (org.netbeans.modules.html.editor.hints.HtmlRule rule : getSortedRules(manager, context, false)) {
                LOG.log(Level.FINE, "checking rule {0}", rule.getDisplayName());
                //skip the rule if we are called from the tasklist,
                //the rule is not supposed to show in tasklist and is not badging
                if(errorType > 0 && !rule.showInTasklist() && !(rule instanceof ErrorBadgingRule)) {
                    continue;
                }
                // do not run regular rules when only error badging, or vice versa
                if ((errorType == 2) != (rule instanceof ErrorBadgingRule)) {
                    continue;
                }
                boolean enabled = manager.isEnabled(rule);
                //html validator error categories workaround. 
                //See the HtmlValidatorRule.isSpecialHtmlValidatorRule() documentation
                LOG.log(Level.FINE, "rule runs");
                if (rule.isSpecialHtmlValidatorRule()) {
                    //run the special rules even if they are disabled
                    rule.setEnabled(enabled);
                    rule.run(htmlRuleContext, hints);
                } else if (enabled) {
                    rule.run(htmlRuleContext, hints);
                }
            }

        } else if (errorType < 2) {
            //add a special hint for reenabling disabled error checks
            List<HintFix> fixes = new ArrayList<>(3);
            if (isErrorCheckingDisabledForFile(result)) {
                fixes.add(new EnableErrorChecksFix(snapshot));
            }
            if (!isErrorCheckingEnabledForMimetype(result)) {
                fixes.add(new EnableErrorChecksForMimetypeFix(saresult));
            }

            Hint h = new Hint(new HtmlRule(HintSeverity.INFO, false),
                    NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_ENABLE_ERROR_CHECKS_FILE_DESCR"), //NOI18N
                    fo,
                    new OffsetRange(0, 0),
                    fixes,
                    50);

            hints.add(h);
        }

        //html extensions
        String sourceMimetype = WebPageMetadata.getContentMimeType(result, true);
        for (HtmlExtension ext : HtmlExtensions.getRegisteredExtensions(sourceMimetype)) {
            ext.computeErrors(manager, context, hints, unhandled);
        }

    }

    /* test */ static List<? extends org.netbeans.modules.html.editor.hints.HtmlRule> getSortedRules(HintsManager manager, RuleContext context, boolean lineContext) {
        Map<?, List<? extends AstRule>> allHints = manager.getHints(lineContext, context);
        List<? extends org.netbeans.modules.html.editor.hints.HtmlRule> ids
                = (List<? extends org.netbeans.modules.html.editor.hints.HtmlRule>) allHints.get(org.netbeans.modules.html.editor.hints.HtmlRule.Kinds.DEFAULT);
        if (ids == null) {
            return Collections.<org.netbeans.modules.html.editor.hints.HtmlRule>emptyList();
        }
        ids.sort(HTML_RULES_COMPARATOR);
        return ids;
    }

    //possibly reenable later once hint fixes are implementd for validator.nu errors
//    private static Collection<HintFix> getCustomHintFixesForError(final RuleContext context, final Error e) {
//        List<HintFix> fixes = new ArrayList<HintFix>();
//        if(e.getKey().equals(SyntaxTreeBuilder.MISSING_REQUIRED_ATTRIBUTES)) {
//            fixes.add(new HintFix() {
//                
//                @Override
//                public String getDescription() {
//                    return NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_GENERATE_REQUIRED_ATTRIBUTES"); //NOI18N
//                }
//
//                @Override
//                public void implement() throws Exception {
//                    AstNode node = HtmlParserResult.getBoundAstNode(e);
//                    Collection<String> missingAttrs = (Collection<String>)node.getProperty(SyntaxTreeBuilder.MISSING_REQUIRED_ATTRIBUTES);
//                    assert missingAttrs != null;
//                    int astOffset = node.startOffset() + 1 + node.name().length();
//                    int insertOffset = context.parserResult.getSnapshot().getOriginalOffset(astOffset);
//                    if(insertOffset == -1) {
//                        return ;
//                    }
//                    StringBuilder templateText = new StringBuilder();
//                    templateText.append(' ');
//
//                    for(String attr : missingAttrs) {
//                        templateText.append(attr);
//                        templateText.append('=');
//                        templateText.append('"');
//                        templateText.append("${");
//                        templateText.append(attr);
//                        templateText.append(" default=\"\"}"); //NOI18N
//                        templateText.append('"');
//                        templateText.append(' ');
//                    }
//                    templateText.append("${cursor}"); //NOI18N
//
//                    CodeTemplate ct = CodeTemplateManager.get(context.doc).createTemporary(templateText.toString());
//                    JTextComponent pane = EditorRegistry.focusedComponent();
//                    if(pane != null) {
//                        pane.setCaretPosition(insertOffset);
//                        ct.insert(pane);
//                    }
//
//                    //reformat the line?
//
//                }
//
//                @Override
//                public boolean isSafe() {
//                    return true;
//                }
//
//                @Override
//                public boolean isInteractive() {
//                    return false;
//                }
//                
//            });
//
//        } else {
//            fixes = Collections.emptyList();
//        }
//
//        return fixes;
//    }
    /**
     * Cancel in-progress processing of hints.
     */
    @Override
    public void cancel() {
    }

    /**
     * <p>Optional builtin Rules. Typically you don't use this; you register
     * your rules in your filesystem layer in the gsf-hints/mimetype1/mimetype2
     * folder, for example gsf-hints/text/x-ruby/. Error hints should go in the
     * "errors" folder, selection hints should go in the "selection" folder, and
     * all other hints should go in the "hints" folder (but note that you can
     * create localized folders and organize them under hints; these categories
     * are shown in the hints options panel. Hints returned from this method
     * will be placed in the "general" folder. </p> <p> This method is primarily
     * intended for rules that should be added dynamically, for example for
     * Rules that have a many different flavors yet a single implementation
     * class (such as JavaScript's StrictWarning rule which wraps a number of
     * builtin parser warnings.)
     *
     * @return A list of rules that are builtin, or null or an empty list when
     * there are no builtins
     */
    @Override
    public List<Rule> getBuiltinRules() {
        return null;
    }

    /**
     * Create a RuleContext object specific to this HintsProvider. This lets
     * implementations of this interface created subclasses of the RuleContext
     * that can be passed around to all the executed rules.
     *
     * @return A new instance of a RuleContext object
     */
    @Override
    public RuleContext createRuleContext() {
        return new RuleContext();
    }
    private static final HtmlRule ERROR_RULE = new HtmlRule(HintSeverity.ERROR, true);
    private static final HtmlRule WARNING_RULE = new HtmlRule(HintSeverity.WARNING, true);
    private static final HtmlRule INFO_RULE = new HtmlRule(HintSeverity.INFO, true);

    private static HtmlRule getRule(Severity s) {
        switch (s) {
            case INFO:
                return INFO_RULE;
            case WARNING:
                return WARNING_RULE;
            case ERROR:
                return ERROR_RULE;
            default:
                throw new AssertionError("Unexpected severity level"); //NOI18N
        }
    }

    private boolean isXmlBasedMimetype(SyntaxAnalyzerResult saresult) {
        String mimeType = Utils.getWebPageMimeType(saresult);
        //return true for something like text/xml, text/xhtml or text/facelets+xhtml
        return mimeType.indexOf("xml") != -1 || mimeType.indexOf("xhtml") != -1;
    }

    private static final class HtmlRule implements ErrorRule {

        private HintSeverity severity;
        private boolean showInTasklist;

        private HtmlRule(HintSeverity severity, boolean showInTaskList) {
            this.severity = severity;
            this.showInTasklist = showInTaskList;
        }

        @Override
        public Set<?> getCodes() {
            return Collections.emptySet();
        }

        @Override
        public boolean appliesTo(RuleContext context) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "html"; //NOI18N //does this show up anywhere????
        }

        @Override
        public boolean showInTasklist() {
            return showInTasklist;
        }

        @Override
        public HintSeverity getDefaultSeverity() {
            return severity;
        }
    }

    private static final class DisableErrorChecksFix implements HintFix {

        private Document doc;
        private FileObject fo;

        public DisableErrorChecksFix(Snapshot snapshot) {
            doc = snapshot.getSource().getDocument(false);
            fo = snapshot.getSource().getFileObject();
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_DISABLE_ERROR_CHECKS_FILE"); //NOI18N
        }

        @Override
        public void implement() throws Exception {
            if (fo == null) {
                return;
            }

            fo.setAttribute(DISABLE_ERROR_CHECKS_KEY, Boolean.TRUE);

            //refresh Action Items for this file
            reindexFile(fo);
            refreshDocument(fo);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static final class EnableErrorChecksFix implements HintFix {

        private Document doc;
        private FileObject fo;

        public EnableErrorChecksFix(Snapshot snapshot) {
            doc = snapshot.getSource().getDocument(false);
            fo = snapshot.getSource().getFileObject();

        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_ENABLE_ERROR_CHECKS_FILE"); //NOI18N
        }

        @Override
        public void implement() throws Exception {
            if (fo == null) {
                return;
            }

            fo.setAttribute(DISABLE_ERROR_CHECKS_KEY, null);

            //refresh Action Items for this file
            reindexFile(fo);
            
            refreshDocument(fo);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static void reindexFile(final FileObject fo) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                //refresh Action Items for this file
                IndexingManager.getDefault().refreshIndexAndWait(fo.getParent().toURL(),
                        Collections.singleton(fo.toURL()), true, false);
            }
        });
    }

    private static void reindexActionItems() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                //refresh all Action Items 
                IndexingManager.getDefault().refreshAllIndices("TLIndexer"); //NOI18N
            }
        });

    }

    private static void refreshDocument(final FileObject fo) throws IOException {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie editorCookie = dobj.getLookup().lookup(EditorCookie.class);
                    StyledDocument document = editorCookie.openDocument();
                    forceReparse(document);
                } catch (IOException  ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

    }

    private abstract static class AbstractErrorChecksForMimetypeFix implements HintFix {

        protected Document doc;
        protected String mimeType;
        protected FileObject file;

        public AbstractErrorChecksForMimetypeFix(SyntaxAnalyzerResult result) {
            this.doc = result.getSource().getSnapshot().getSource().getDocument(false);
            this.file = result.getSource().getSourceFileObject();
            this.mimeType = Utils.getWebPageMimeType(result);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static final class DisableErrorChecksForMimetypeFix extends AbstractErrorChecksForMimetypeFix {

        public DisableErrorChecksForMimetypeFix(SyntaxAnalyzerResult result) {
            super(result);
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_DISABLE_ERROR_CHECKS_MIMETYPE", mimeType); //NOI18N
        }

        @Override
        public void implement() throws Exception {
            HtmlPreferences.setHtmlErrorChecking(mimeType, false);
            reindexActionItems();
            reindexFile(file);
            refreshDocument(file);
        }
    }

    private static final class EnableErrorChecksForMimetypeFix extends AbstractErrorChecksForMimetypeFix {

        public EnableErrorChecksForMimetypeFix(SyntaxAnalyzerResult result) {
            super(result);
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_ENABLE_ERROR_CHECKS_MIMETYPE", mimeType); //NOI18N
        }

        @Override
        public void implement() throws Exception {
            HtmlPreferences.setHtmlErrorChecking(mimeType, true);
            reindexActionItems();
            reindexFile(file);
            refreshDocument(file);
        }
    }

    private static class SetDefaultHtmlVersionHintFix implements HintFix {

        private HtmlVersion version;
        private Document doc;
        private Project project;
        private boolean xhtml;

        public SetDefaultHtmlVersionHintFix(HtmlVersion version, Project project, Document doc, boolean xhtml) {
            this.version = version;
            this.project = project;
            this.doc = doc; //to be able to force reparse the hinted document
            this.xhtml = xhtml;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HtmlHintsProvider.class, "MSG_SET_DEFAULT_HTML_VERSION", version.getDisplayName());
        }

        @Override
        public void implement() throws Exception {
            ProjectDefaultHtmlSourceVersionController.setDefaultHtmlVersion(project, version, xhtml);
            forceReparse(doc);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static void forceReparse(final Document doc) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NbEditorDocument nbdoc = (NbEditorDocument) doc;
                nbdoc.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                        if (mti != null) {
                            mti.tokenHierarchyControl().rebuild();
                        }
                    }
                });
            }
        });
    }
    private static Comparator<org.netbeans.modules.html.editor.hints.HtmlRule> HTML_RULES_COMPARATOR = new Comparator<org.netbeans.modules.html.editor.hints.HtmlRule>() {
        @Override
        public int compare(org.netbeans.modules.html.editor.hints.HtmlRule o1, org.netbeans.modules.html.editor.hints.HtmlRule o2) {
            int prio_diff = o1.getPriority() - o2.getPriority();
            return prio_diff != 0 ? prio_diff : o1.getDisplayName().compareTo(o2.getDisplayName());
        }
    };
}
