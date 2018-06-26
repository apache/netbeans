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
package org.netbeans.modules.php.editor.nav.hierarchy;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Union2;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

/**
 * @author Radek Matous
 */
public final class ShowPhpClassHierarchyAction extends CookieAction {

    private static final Logger LOG = Logger.getLogger(ShowPhpClassHierarchyAction.class.getName());

    @Override
    protected void performAction(Node[] activatedNodes) {
        PhpHierarchyTopComponent view = PhpHierarchyTopComponent.findInstance();
        if (!view.isOpened()) {
            view.open();
        }
        view.refresh();
        view.requestActive();
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_ShowHierarchyAction"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[]{DataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    static Model getModel(Union2<Document, FileObject> document2Parse) {
        Parameters.notNull("document2Parse", document2Parse); //NOI18N
        ModelProviderTask modelProvider = new ModelProviderTask();
        if (document2Parse.hasFirst()) {
            try {
                Source source = Source.create(document2Parse.first());
                ParserManager.parseWhenScanFinished(Collections.singleton(source), modelProvider);
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }
        } else if (document2Parse.hasSecond()) {
            Source source = Source.create(document2Parse.second());
            try {
                ParserManager.parseWhenScanFinished(Collections.singletonList(source), modelProvider);
            } catch (ParseException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        return modelProvider.getModel();
    }

    public static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }

    private static class ModelProviderTask extends UserTask {

        private Model model;

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Parser.Result cc = resultIterator.getParserResult();
            if (cc instanceof PHPParseResult) {
                model = ((PHPParseResult) cc).getModel();
            }
        }

        private Model getModel() {
            return model;
        }
    }
}
