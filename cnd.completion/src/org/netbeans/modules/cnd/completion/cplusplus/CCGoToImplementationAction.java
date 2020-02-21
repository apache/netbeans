/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.completion.cplusplus;

import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import org.netbeans.modules.cnd.utils.MIMENames;

/**
 *
 */
@EditorActionRegistrations({
    @EditorActionRegistration(
    name = "goto-implementation",// NOI18N
    popupPath = "goto", // NOI18N
    popupText = "#goto-implementation-text", // NOI18N
    mimeType = MIMENames.CPLUSPLUS_MIME_TYPE
    ),
    @EditorActionRegistration(
    name = "goto-implementation",// NOI18N
    popupPath = "goto", // NOI18N
    popupText = "#goto-implementation-text", // NOI18N
    mimeType = MIMENames.C_MIME_TYPE
    ),
    @EditorActionRegistration(
    name = "goto-implementation",// NOI18N
    popupPath = "goto", // NOI18N
    popupText = "#goto-implementation-text", // NOI18N
    mimeType = MIMENames.HEADER_MIME_TYPE
    )
})
public class CCGoToImplementationAction extends BaseAction {

    static final long serialVersionUID = 1L;

    public CCGoToImplementationAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        final JTextComponent target = getFocusedComponent();
        if (target != null && (target.getDocument() instanceof BaseDocument)) {
            BaseDocument doc = (BaseDocument) target.getDocument();
            int offset = target.getSelectionStart();
            // first try include provider
            if (new CsmHyperlinkProvider().isHyperlinkPoint(doc, offset, HyperlinkType.GO_TO_DECLARATION)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean asynchonous() {
        return false;
    }

    public boolean gotoImplementation(final JTextComponent target) {
        final String taskName = "Go to implementation"; //NOI18N
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (target != null && (target.getDocument() instanceof BaseDocument)) {
                    BaseDocument doc = (BaseDocument) target.getDocument();
                    int offset = target.getSelectionStart();
                    new CsmHyperlinkProvider().goToDeclaration(doc, target, offset, HyperlinkType.ALT_HYPERLINK);
                }
            }
        };
        CsmModelAccessor.getModel().enqueue(run, taskName);
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        gotoImplementation(target);
    }
}
