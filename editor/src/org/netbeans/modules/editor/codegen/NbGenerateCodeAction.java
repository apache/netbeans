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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.editor.codegen;

import java.awt.Frame;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 */
@EditorActionRegistration(
        name = "generate-code",
        shortDescription = "#desc-generate-code",
        popupText = "#popup-generate-code"
) // NOI18N
public class NbGenerateCodeAction extends BaseAction {

    public static final String generateCode = "generate-code"; //NOI18N
    
    public NbGenerateCodeAction(){
        putValue(ExtKit.TRIMMED_TEXT, NbBundle.getBundle(NbGenerateCodeAction.class).getString("generate-code-trimmed")); //NOI18N
    }
    
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        final Task task = new Task(getFullMimePath(target.getDocument(), target.getCaretPosition()));
        final AtomicBoolean cancel = new AtomicBoolean();
        ProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                if (cancel != null && cancel.get())
                    return ;
                task.run(Lookups.singleton(target));
                if (cancel != null && cancel.get())
                    return ;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (task.codeGenerators.size() > 0) {
                            int altHeight = -1;
                            Point where = null;
                            try {
                                Rectangle carretRectangle = target.modelToView(target.getCaretPosition());
                                altHeight = carretRectangle.height;
                                where = new Point(carretRectangle.x, carretRectangle.y + carretRectangle.height);
                                SwingUtilities.convertPointToScreen(where, target);
                            } catch (BadLocationException ble) {
                            }
                            if (where == null) {
                                where = new Point(-1, -1);
                                
                            }
                            PopupUtil.showPopup(new GenerateCodePanel(target, task.codeGenerators), (Frame)SwingUtilities.getAncestorOfClass(Frame.class, target), where.x, where.y, true, altHeight);
                        } else {
                            target.getToolkit().beep();
                        }
                    }
                });
            }
        }, NbBundle.getBundle(NbGenerateCodeAction.class).getString("generate-code-trimmed"), cancel, false);
    }
    
    static String[] test(Document doc, int pos) {
        Task task = new Task(getFullMimePath(doc, pos));
        task.run(Lookups.fixed());
        String[] ret = new String[task.codeGenerators.size()];
        int i = 0;
        for (CodeGenerator codeGenerator : task.codeGenerators)
            ret[i++] = codeGenerator.getDisplayName();
        return ret;
    }
    
    private static MimePath getFullMimePath(Document document, int offset) {
        String langPath = null;

        if (document instanceof AbstractDocument) {
            AbstractDocument adoc = (AbstractDocument)document;
            adoc.readLock();
            try {
                List<TokenSequence<?>> list = TokenHierarchy.get(document).embeddedTokenSequences(offset, true);
                if (list.size() > 1) {
                    langPath = list.get(list.size() - 1).languagePath().mimePath();
                }
            } finally {
                adoc.readUnlock();
            }
        }

        if (langPath == null) {
            langPath = NbEditorUtilities.getMimeType(document);
        }

        if (langPath != null) {
            return MimePath.parse(langPath);
        } else {
            return null;
        }
    }

    private static class Task implements CodeGeneratorContextProvider.Task {
        private MimePath mimePath;
        private Iterator<? extends CodeGeneratorContextProvider> contextProviders;
        private List<CodeGenerator> codeGenerators = new ArrayList<CodeGenerator>(); 

        private Task(MimePath mimePath) {
            this.mimePath = mimePath;
            contextProviders = MimeLookup.getLookup(mimePath).lookupAll(CodeGeneratorContextProvider.class).iterator();
        }

        public void run(Lookup context) {
            if (contextProviders.hasNext()) {
                contextProviders.next().runTaskWithinContext(context, this);
            } else {
                for (CodeGenerator.Factory factory : MimeLookup.getLookup(mimePath).lookupAll(CodeGenerator.Factory.class))
                    codeGenerators.addAll(factory.create(context));
            }
        }
    }
    
    public static final class GlobalAction extends MainMenuAction {
        public GlobalAction() {
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText() {
            return NbBundle.getBundle(GlobalAction.class).getString("generate-code-main-menu-source-item"); //NOI18N
        }

        protected String getActionName() {
            return generateCode;
        }
    } // End of GlobalAction class
}
