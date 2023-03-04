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

package org.netbeans.modules.html.editor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ErrorFilter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.ErrorBadgingRule;
import org.netbeans.modules.html.editor.api.gsf.HtmlErrorFilterContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.hints.HtmlHintsProvider;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.WebPageMetadata;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
public class HtmlErrorFilter implements ErrorFilter {

    public static final String DISABLE_ERROR_CHECKS_KEY = "disable_error_checking"; //NOI18N
    
    private static final ErrorFilter INSTANCE = new HtmlErrorFilter(false);
    private static final ErrorFilter INSTANCE_BADGING = new HtmlErrorFilter(true);
    private HintsProvider htmlHintsProvider;
    private HintsManager htmlHintsManager;
    private boolean onlyBadges;
    
    private HtmlErrorFilter(boolean badging) {
        this();
        this.onlyBadges = badging;
    }

    public HtmlErrorFilter() {
        htmlHintsProvider = new HtmlHintsProvider();
        htmlHintsManager = HintsProvider.HintsManager.getManagerForMimeType(HtmlKit.HTML_MIME_TYPE);
    }
    
    @Override
    public List<? extends Error> filter(ParserResult parserResult) {
        if(!(parserResult instanceof HtmlParserResult)) {
            return null; //not ours
        }
        
        //use hints setting to filter out the errors and set their severity 
        HtmlErrorFilterContext context = new HtmlErrorFilterContext();
        context.setOnlyBadging(onlyBadges);
        context.parserResult = parserResult;
        context.manager = htmlHintsManager;
        context.doc = (BaseDocument)parserResult.getSnapshot().getSource().getDocument(false); //should not load the document if not loaded already
        List<Hint> hints = new ArrayList<>();
        htmlHintsProvider.computeErrors(htmlHintsManager, context, hints, new ArrayList<Error>());
        
        List<Error> filtered = new ArrayList<>(hints.size());
        for(Hint h : hints) {
            

            Rule rule = h.getRule();
            if(!rule.showInTasklist()) {
                continue;
            }
            
            //use the severity defined in the hints settings
            //HintSeverity hseverity = HintsSettings.getSeverity((GsfHintsManager)htmlHintsManager, (UserConfigurableRule)h);
            //TODO fix the severity somehow - now it seems there's no away how to get 
            //the severity set to a particular hint by the hint options.
            
            //use at least the default severity
            HintSeverity hs = rule.getDefaultSeverity();
            Severity severity;
            switch(hs) {
                case ERROR:
                    severity = Severity.ERROR;
                    break;
                case WARNING:
                    severity = Severity.WARNING;
                    break;
                case INFO:
                    severity = Severity.INFO;
                    break;
                default:
                    //ignore
                    continue;
            }
            
            //convert the offsets back to the embedded ones as we are producing Error-s which
            //are supposed to point to the embedded source.
            int ast_from = parserResult.getSnapshot().getEmbeddedOffset(h.getRange().getStart());
            int ast_to = parserResult.getSnapshot().getEmbeddedOffset(h.getRange().getEnd());
            if(ast_from != -1 && ast_to != -1) {
                DefaultError e = new BadgingDefaultError("error", //NOI18N
                        h.getDescription(), 
                        h.getDescription(), 
                        h.getFile(),
                        h.getRange().getStart(), 
                        h.getRange().getEnd(), 
                        severity,
                        rule instanceof ErrorBadgingRule);

                filtered.add(e);
            }
        }
        
        return filtered;
    }
    
    public static boolean isErrorCheckingEnabled(Parser.Result result) {
        return !isErrorCheckingDisabledForFile(result) && isErrorCheckingEnabledForMimetype(result);
    }

    public static boolean isErrorCheckingDisabledForFile(Parser.Result result) {
        FileObject fo = result.getSnapshot().getSource().getFileObject();
        return fo != null && fo.getAttribute(DISABLE_ERROR_CHECKS_KEY) != null;
    }

    public static boolean isErrorCheckingEnabledForMimetype(Parser.Result result) {
        return HtmlPreferences.isHtmlErrorCheckingEnabledForMimetype(WebPageMetadata.getContentMimeType(result, true));
    }
    
    @ServiceProvider(service=ErrorFilter.Factory.class)
    public static class Factory implements ErrorFilter.Factory {

        @Override
        public ErrorFilter createErrorFilter(String featureName) {
            switch (featureName) {
                case ErrorFilter.FEATURE_TASKLIST:
                    return INSTANCE;
                case "errorBadges": // NOI18N
                    return INSTANCE_BADGING;
                default:
                    return null;
            }
        }
        
    }
    
    private class BadgingDefaultError extends DefaultError implements Error.Badging {

        private boolean badging;
        
        public BadgingDefaultError(String key, String displayName, String description, FileObject file, int start, int end, Severity severity, boolean badging) {
            super(key, displayName, description, file, start, end, severity);
            this.badging = badging;
        }

        @Override
        public boolean showExplorerBadge() {
            return badging;
        }
        
        
    }
    
}
