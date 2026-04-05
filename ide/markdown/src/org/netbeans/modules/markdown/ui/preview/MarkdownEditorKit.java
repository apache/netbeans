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
package org.netbeans.modules.markdown.ui.preview;

import javax.swing.text.Document;
import org.netbeans.modules.markdown.ui.preview.views.MarkdownViewFactory;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.modules.markdown.utils.StyleUtils;

/**
 *
 * @author moacirrf
 */
public class MarkdownEditorKit extends HTMLEditorKit {

    private final transient ViewFactory viewFactory;
 
    public MarkdownEditorKit() {
        super();
        this.viewFactory = new MarkdownViewFactory();
    }

    /**
     * copy/paste from the original HTMLEditorKit method but with custom styling overriding
     * @see HTMLEditorKit.createDefaultDocument
     * 
     * @return 
     */
    @Override
    public Document createDefaultDocument() {
        StyleSheet styles = getStyleSheet();
        StyleSheet ss = new StyleSheet();

        //default style override
        ss.addStyleSheet(styles);
        StyleUtils.addNbSyles(ss);
        
        HTMLDocument doc = new HTMLDocument(ss);
        doc.setParser(getParser());
        doc.setAsynchronousLoadPriority(4);
        doc.setTokenThreshold(100);
        return doc;
    }
    
    @Override
    public ViewFactory getViewFactory() {
        return viewFactory;
    }

}
