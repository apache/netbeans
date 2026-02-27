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
        addCustomStyleRules(ss);
        
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

    /**
     * Hardcoded styling customization asa a quick fix for improving
     * dispaly of markdown previews
     * 
     * Note some css rules are not recognized in Swing
     * 
     * Existing swing html CSS limitations
     * - hr no styling
     * - code element no padding
     * - table no border collapse
     * - no nth-child selector
     * 
     * @param ss 
     */
    private void addCustomStyleRules(StyleSheet ss) {
        //body
        ss.addRule("body {padding:40px;background-color:white}");  // NOI18N
        //headings
        ss.addRule("h1 a, h2 a, h3 a, h4 a, h5 a, h6 a{color:black;}"); // NOI18N
        ss.addRule("h1 code, h2 code, h3 code, h4 code, h5 code, h6 code {font-size:inherit;}"); // NOI18N
        //list
        ss.addRule("ul, li ul {margin-left: 20px;}"); // NOI18N
        ss.addRule("li {margin-bottom: 3px;padding-left:5px;}"); // NOI18N

        //table
        ss.addRule("table {border-spacing:0;}"); // NOI18N
        ss.addRule("th, td {border:1px solid rgb(209, 217, 224); padding:10px;}"); // NOI18N

        //code related styling
        ss.addRule("code {border-radius: 6px; background-color: rgba(129, 139, 152, 0.12);}"); // NOI18N
        ss.addRule("pre {padding:16px; background-color: rgb(246, 248, 250);}"); // NOI18N
        ss.addRule("pre code {border-radius:0px; background-color:rgb(246, 248, 250);}");

        //blockquote
        ss.addRule("blockquote {padding: 0 16px; margin: 0px; border-left: 2px solid rgb(209, 217, 224);}"); // NOI18N
        ss.addRule("blockquote p { color:rgb(89, 99, 110);}"); // NOI18N
        ss.addRule("pre, blockquote {margin-bottom: 10px;}"); // NOI18N
        ss.addRule("li blockquote {margin-bottom: 0px;}"); // NOI18N
    }
}
