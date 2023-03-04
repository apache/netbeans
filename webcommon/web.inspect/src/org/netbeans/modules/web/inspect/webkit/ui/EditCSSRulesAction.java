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

package org.netbeans.modules.web.inspect.webkit.ui;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.html.editor.lib.api.SourceElementHandle;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * Edit CSS Rules action for CSS Styles view.
 *
 * @author Jan Stola
 */
public class EditCSSRulesAction extends AbstractAction {
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(EditCSSRulesAction.class);
    /** The actual implementation of Edit CSS Rules action. */
    private final org.netbeans.modules.css.visual.api.EditCSSRulesAction delegate =
            new org.netbeans.modules.css.visual.api.EditCSSRulesAction();
    /** Active node. */
    private Node activeNode;

    /**
     * Sets the active node.
     * 
     * @param activeNode active node.
     */
    void setActiveNode(final Node activeNode) {
        this.activeNode = activeNode;
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean enabled = false;
                if (activeNode != null) {
                    SourceElementHandle sourceElementHandle = activeNode.getLookup().lookup(SourceElementHandle.class);
                    enabled = sourceElementHandle.getFileObject() != null;
                }
                setEnabled(enabled);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final SourceElementHandle sourceElementHandle = activeNode.getLookup().lookup(SourceElementHandle.class);
        final FileObject file = sourceElementHandle.getFileObject();
        if (file == null) {
            return;
        }
        Source source = Source.create(file);
        if (source == null) {
            return;
        }
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/html"); // NOI18N
                    if (ri != null) {
                        Parser.Result result = ri.getParserResult();
                        delegate.setContext(file);
                        org.netbeans.modules.html.editor.lib.api.elements.Node sourceNode =
                                sourceElementHandle.resolve(result);
                        if (sourceNode != null) {
                            delegate.setHtmlSourceElementHandle((OpenTag)sourceNode, result.getSnapshot(), file);
                            RP.post(new Runnable() {
                                @Override
                                public void run() {
                                    delegate.actionPerformed(null);
                                }
                            });
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Logger.getLogger(EditCSSRulesAction.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Override
    public Object getValue(String key) {
        return delegate.getValue(key);
    }
    
}
