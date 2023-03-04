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
package org.netbeans.modules.html.editor.api;

import org.netbeans.modules.html.editor.HtmlTransferHandler;
import javax.swing.Action;
import javax.swing.text.TextAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.csl.api.CslActions;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.html.editor.APIAccessor;

/**
 * Editor kit implementation for HTML content type
 *
 * @author Miloslav Metelka, mfukala@netbeans.org
 *
 */
public class HtmlKit extends NbEditorKit implements org.openide.util.HelpCtx.Provider {
    
    static {
        APIAccessor.DEFAULT = new AccessorImpl();
    }

    public @Override
    org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("org.netbeans.modules.html.editor.api.HtmlKit"); //NOI18N
    }
    static final long serialVersionUID = -1381945567613910297L;
    
    public static final String HTML_MIME_TYPE = "text/html"; // NOI18N
    private String contentType;

    public HtmlKit() {
        this(HTML_MIME_TYPE);
    }

    public HtmlKit(String mimeType) {
        super();
        contentType = HTML_MIME_TYPE;
    }

    @Override
    public String getContentType() {
        return contentType;
    }
    
    void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public Object clone() {
        return new HtmlKit();
    }

    /**
     * Called after the kit is installed into JEditorPane
     */
    @Override
    public void install(javax.swing.JEditorPane c) {
        super.install(c);
        HtmlTransferHandler.install(c);
    }

    @Override
    protected Action[] createActions() {
        Action[] HtmlActions = new Action[]{
            CslActions.createSelectCodeElementAction(true),
            CslActions.createSelectCodeElementAction(false),
            CslActions.createInstantRenameAction(),
            CslActions.createToggleBlockCommentAction(),
            new ExtKit.CommentAction(""), //NOI18N
            new ExtKit.UncommentAction(""), //NOI18N
            CslActions.createGoToMarkOccurrencesAction(false),
            CslActions.createGoToMarkOccurrencesAction(true),
            CslActions.createGoToDeclarationAction()
        };
        return TextAction.augmentList(super.createActions(), HtmlActions);
    }

    
}
