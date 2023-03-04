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
package org.netbeans.modules.csl.editor.completion;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;


/**
 * Produce completion-popup help for elements
 * 
 * @author Tor Norbye
 */
public class GsfCompletionDoc implements CompletionDocumentation {
    private String content = null;
    private URL docURL = null;
    private AbstractAction goToSource = null;
    private ElementHandle elementHandle;
    private Language language;
    private ParserResult controller;
    private Callable<Boolean> cancel;

    private GsfCompletionDoc(final ParserResult controller, final ElementHandle elementHandle, URL url, Callable<Boolean> cancel) {
        this.controller = controller;
        this.language = LanguageRegistry.getInstance().getLanguageByMimeType(controller.getSnapshot().getMimeType());
        this.cancel = cancel;
        if (elementHandle != null && elementHandle.getMimeType() != null) {
            Language embeddedLanguage = LanguageRegistry.getInstance().getLanguageByMimeType(elementHandle.getMimeType());
            if (embeddedLanguage != null && embeddedLanguage.getParser(Collections.singleton(controller.getSnapshot())) != null) {
                language = embeddedLanguage;
            }
        }

        CodeCompletionHandler completer = language.getCompletionProvider();

        this.elementHandle = elementHandle;

        if (elementHandle != null) {
            goToSource =
                new AbstractAction() {
                        public void actionPerformed(ActionEvent evt) {
                            Completion.get().hideAll();
                            UiUtils.open(controller.getSnapshot().getSource(), elementHandle);
                        }
                    };
            if (url != null) {
                docURL = url;
            } else {
                docURL = null;
            }
        }

        if (completer != null) {
            if (completer instanceof CodeCompletionHandler2) {
                Documentation doc = ((CodeCompletionHandler2) completer).documentElement(controller, elementHandle, cancel);
                if (doc != null) {
                    this.content = doc.getContent();
                    if (docURL == null) {
                        docURL = doc.getUrl();
                    }
                } else {
                    this.content = completer.document(controller, elementHandle);
                }
            } else {
                this.content = completer.document(controller, elementHandle);
            }
        }
    }

    public static final GsfCompletionDoc create(ParserResult controller,
        ElementHandle elementHandle, Callable<Boolean> cancel) {
        GsfCompletionDoc doc = new GsfCompletionDoc(controller, elementHandle, null, cancel);
        return doc.content != null ? doc : null;
    }

    public String getText() {
        return content;
    }

    public URL getURL() {
        return docURL;
    }

    @Override
    public CompletionDocumentation resolveLink(String link) {
        if (link.startsWith("www.")) {
            link = "http://" + link;
        }
        if (link.matches("[a-z]+://.*")) { // NOI18N
            try {
                URL url = new URL(link);
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                return null;
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);
            }
        }
        
        ElementHandle handle = language.getCompletionProvider().resolveLink(link, elementHandle);
        if (handle != null) {
            URL url = null;
            if(handle instanceof ElementHandle.UrlHandle) {
                String url_text = ((ElementHandle.UrlHandle)handle).getUrl();
                try {
                    url = new URL(url_text);
                } catch (MalformedURLException mue) {
                    Logger.getLogger("global").log(Level.INFO, null, mue);
                }
            }
            
            return new GsfCompletionDoc(controller, handle, url, cancel);
        }
        return null;
    }

    public Action getGotoSourceAction() {
        return goToSource;
    }
}
