/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
