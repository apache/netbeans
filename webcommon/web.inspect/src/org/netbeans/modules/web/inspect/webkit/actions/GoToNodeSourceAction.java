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
package org.netbeans.modules.web.inspect.webkit.actions;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Go to source action for DOM nodes.
 *
 * @author Jan Stola
 */
public class GoToNodeSourceAction extends NodeAction  {
    /** {@code RequestProcessor} for this class. */
    private static final RequestProcessor RP = new RequestProcessor(GoToNodeSourceAction.class);

    @Override
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        org.openide.nodes.Node selection = activatedNodes[0];
        org.netbeans.modules.web.webkit.debugging.api.dom.Node node = selection
                .getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
        Resource resource = getNodeOrigin(selection);
        FileObject fob = resource.toFileObject();
        if (fob == null) {
            return;
        }
        try {
            Source source = Source.create(fob);
            if (source != null) {
                ParserManager.parse(Collections.singleton(source), new GoToNodeTask(node, fob));
            }
        } catch (ParseException ex) {
            Logger.getLogger(GoToNodeSourceAction.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Override
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            org.openide.nodes.Node selection = activatedNodes[0];
            org.netbeans.modules.web.webkit.debugging.api.dom.Node node = selection
                    .getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
            if (node == null) {
                return false;
            }
            final Resource resource = getNodeOrigin(selection);
            // Avoid invocation of Resource.toFileObject() under Children.MUTEX
            RP.post(new Runnable() {
                @Override
                public void run() {
                    FileObject fob = resource.toFileObject();
                    if (fob == null || !fob.isData()) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setEnabled(false);
                            }
                        });
                    }
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        // The default popup presenter does not observe property changes.
        // We need a presenter that observes the changes because of our
        // lazy disabling of the action. The default menu presenter observes
        // the changes.
        return super.getMenuPresenter();
    }

    /**
     * Returns the origin of the specified node.
     * 
     * @param node node whose origin should be returned.
     * @return origin of the specified node.
     */
    private Resource getNodeOrigin(org.openide.nodes.Node node) {
        Resource resource = node.getLookup().lookup(Resource.class);
        if (resource == null) {
            org.openide.nodes.Node parent = node.getParentNode();
            if (parent != null) {
                resource = getNodeOrigin(parent);
            }
        }
        return resource;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToNodeSourceAction.class, "GoToNodeSourceAction.displayName"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Task that jumps to the source of the specified node in the document
     * in the given file.
     */
    static class GoToNodeTask extends UserTask {
        /** Node to jump to. */
        private final org.netbeans.modules.web.webkit.debugging.api.dom.Node node;
        /** File to jump into. */
        private final FileObject fob;

        /**
         * Creates a new {@code GoToNodeTask} for the specified file and node.
         *
         * @param node node to jump to.
         * @param fob file to jump into.
         */
        GoToNodeTask(org.netbeans.modules.web.webkit.debugging.api.dom.Node node, FileObject fob) {
            this.node = node;
            this.fob = fob;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            ResultIterator htmlResultIterator = WebUtils.getResultIterator(resultIterator, "text/html"); // NOI18N
            final int offsetToShow;
            if (htmlResultIterator == null) {
                offsetToShow = 0;
            } else {
                HtmlParsingResult result = (HtmlParsingResult)htmlResultIterator.getParserResult();
                Node nodeToShow = Utilities.findNode(result, node);
                Snapshot snapshot = htmlResultIterator.getSnapshot();
                int snapshotOffset = nodeToShow.from();
                offsetToShow = snapshot.getOriginalOffset(snapshotOffset);
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    CSSUtils.openAtOffset(fob, offsetToShow);
                }
            });
        }
        
    }

}
