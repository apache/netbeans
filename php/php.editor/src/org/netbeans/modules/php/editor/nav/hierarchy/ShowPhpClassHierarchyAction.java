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
