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
package org.netbeans.modules.csl.editor.codetemplates;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.text.JTextComponent;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.lib.editor.codetemplates.spi.*;
import org.netbeans.modules.csl.editor.completion.GsfCompletionProvider;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Code template processor for GSF: Delegates mostly to the plugin
 * to resolve parameters.
 *
 * @author Tor Norbye
 */
public class GsfCodeTemplateProcessor implements CodeTemplateProcessor {
    private CodeTemplateInsertRequest   request;
    private volatile ParserResult                cInfo = null;
    private volatile Snapshot                    snapshot = null;
    
    private static final RequestProcessor RP = new RequestProcessor(GsfCodeTemplateProcessor.class.getName(), 1, false, false);

    private GsfCodeTemplateProcessor(CodeTemplateInsertRequest request) {
        this.request = request;
    }

    public synchronized void updateDefaultValues() {
        boolean cont = true;

        while (cont) {
            cont = false;

            for (Object p : request.getMasterParameters()) {
                CodeTemplateParameter param = (CodeTemplateParameter)p;
                String value = getProposedValue(param);

                if ((value != null) && !value.equals(param.getValue())) {
                    param.setValue(value);
                    cont = true;
                }
            }
        }
    }

    public void parameterValueChanged(CodeTemplateParameter masterParameter, boolean typingChange) {
        if (typingChange) {
            for (Object p : request.getMasterParameters()) {
                CodeTemplateParameter param = (CodeTemplateParameter)p;

                if (!param.isUserModified()) {
                    String value = getProposedValue(param);

                    if ((value != null) && !value.equals(param.getValue())) {
                        param.setValue(value);
                    }
                }
            }
        }
    }

    public void release() {
    }

    private String getProposedValue(CodeTemplateParameter param) {
        String variable = param.getName();
        JTextComponent c = request.getComponent();
        int caretOffset = c.getCaret().getDot();

        String resolved =
            delegatedResolve(caretOffset, param.getName(), variable, param.getHints());

        return resolved;
    }

    /** Delegate to the language plugin to indicate if it cares about this variable or not */
    private String delegatedResolve(int caretOffset, String name, String variable, Map params) {
        try {
            if (initParsing()) {

                CodeCompletionHandler completer = GsfCompletionProvider.getCompletable(snapshot.getSource ().getDocument (true), caretOffset);

                if (completer == null) {
                    return null;
                }

                return completer.resolveTemplateVariable(variable, cInfo, caretOffset, name, params);
            }
        } catch (Exception e) {
        }

        return null;
    }

    private boolean initParsing() {
        if (cInfo == null) {
            final JTextComponent c = request.getComponent();

            //final int caretOffset = c.getCaret().getDot();
            final Source js = Source.create (c.getDocument());

            if (c.getDocument() instanceof BaseDocument) {
                BaseDocument doc = (BaseDocument) c.getDocument();
                if (doc.isAtomicLock()) {
                    return false;
                }
            }
            if (js != null) {
                final RequestProcessor.Task newTask = RP.create(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final AtomicBoolean done = new AtomicBoolean();
                            final Thread me = Thread.currentThread();
                            ParserManager.parseWhenScanFinished(
                                    Collections.<Source>singleton(js),
                                    new UserTask() {
                                public void run(ResultIterator resultIterator) throws IOException, ParseException {
                                    if (!Thread.currentThread().equals(me)) {
                                        return;
                                    }
                                    Parser.Result parserResult = resultIterator.getParserResult(c.getCaretPosition());
                                    if (!(parserResult instanceof ParserResult)) {
                                        return;
                                    }
                                    cInfo = (ParserResult) parserResult;
                                    snapshot = parserResult.getSnapshot();
                                    done.set(true);
                                }
                            }
                            );
                            if (!done.get()) {
                                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GsfCodeTemplateFilter.class, "JCT-scanning-in-progress")); //NOI18N
                            }
                        } catch (ParseException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                });
                newTask.schedule(0);
            }
        }
        return cInfo != null;
    }

    public static final class Factory implements CodeTemplateProcessorFactory {
        public CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request) {
            return new GsfCodeTemplateProcessor(request);
        }
    }
}
