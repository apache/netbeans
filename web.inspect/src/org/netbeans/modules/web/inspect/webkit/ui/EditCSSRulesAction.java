/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
