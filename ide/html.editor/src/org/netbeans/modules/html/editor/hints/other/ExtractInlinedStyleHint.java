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
package org.netbeans.modules.html.editor.hints.other;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.refactoring.HtmlSpecificRefactoringsProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mfukala@netbeans.org
 */
public class ExtractInlinedStyleHint extends Hint {

    public ExtractInlinedStyleHint(RuleContext context, OffsetRange range) {
        super(ExtractInlinedStyleRule.SINGLETON,
                ExtractInlinedStyleRule.SINGLETON.getDisplayName(),
                context.parserResult.getSnapshot().getSource().getFileObject(),
                range,
                Collections.<HintFix>singletonList(new ExtractInlinedStyleHintFix(context)),
                10);
    }

    private static class ExtractInlinedStyleHintFix implements HintFix {

        RuleContext context;

        public ExtractInlinedStyleHintFix(RuleContext context) {
            this.context = context;
        }

        @Override
        public String getDescription() {
            return ExtractInlinedStyleRule.SINGLETON.getDisplayName();
        }

        @Override
        public void implement() throws Exception {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        HtmlSpecificRefactoringsProvider provider = Lookup.getDefault().lookup(HtmlSpecificRefactoringsProvider.class);
                        if (provider == null) {
                            return;
                        }
                        
                        FileObject fileObject = context.parserResult.getSnapshot().getSource().getFileObject();
                        if (fileObject == null) {
                            return;
                        }
                        
                        EditorCookie cookie = DataLoadersBridge.getDefault().getCookie(fileObject, EditorCookie.class);
                        if (cookie == null) {
                            return;
                        }
                        
                        provider.doExtractInlineStyle(Lookups.fixed(cookie));
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

            });

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

}
