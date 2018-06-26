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
package org.netbeans.modules.php.smarty.editor.completion;

import java.util.*;
import java.util.ArrayList;
import javax.swing.text.Document;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.smarty.editor.TplDataLoader;
import org.netbeans.modules.php.smarty.editor.completion.entries.SmartyCodeCompletionOffer;

/**
 *
 * Tpl completion results finder
 *
 * @author Martin Fousek
 */
public class TplCompletionQuery extends UserTask {

    private Document document;
    private CompletionResult completionResult;

    public TplCompletionQuery(Document document) {
        this.document = document;
    }

    public CompletionResult query() throws ParseException {
        Source source = Source.create(document);
        ParserManager.parse(Collections.singleton(source), this);

        return this.completionResult;
    }

    @Override
    public void run(ResultIterator resultIterator) throws Exception {
        String resultMimeType = resultIterator.getSnapshot().getMimeType();
        if (resultMimeType.equals(TplDataLoader.MIME_TYPE)) {
            this.completionResult = query(resultIterator);
        }
    }

    private CompletionResult query(ResultIterator resultIterator) {
        return new CompletionResult(SmartyCodeCompletionOffer.getFunctions(),
                SmartyCodeCompletionOffer.getVariableModifiers(),
                SmartyCodeCompletionOffer.getFunctionParameters());
    }

    public static class CompletionResult {

        private ArrayList<TplCompletionItem> functions;
        private ArrayList<TplCompletionItem> variableModifiers;
        private HashMap<String, ArrayList<TplCompletionItem>> functionParams;

        CompletionResult(ArrayList<TplCompletionItem> functions, ArrayList<TplCompletionItem>
                variableModifiers, HashMap<String, ArrayList<TplCompletionItem>> functionParams) {
            this.functions = functions;
            this.variableModifiers = variableModifiers;
            this.functionParams = functionParams;
        }

        public ArrayList<TplCompletionItem> getFunctions() {
            return functions;
        }

        public ArrayList<TplCompletionItem> getVariableModifiers() {
            return variableModifiers;
        }

        public ArrayList<TplCompletionItem> getParamsForCommand(ArrayList<String> commands) {
            // first command contain main keyword
            ArrayList<TplCompletionItem> availableItems = new ArrayList<TplCompletionItem>(functionParams.get(commands.get(0)));
            // rest of them is just removed from codecompletion
            Iterator it = availableItems.iterator();
            while (it.hasNext()) {
                TplCompletionItem tplCompletionItem = (TplCompletionItem)it.next();
                for (String command : commands) {
                    if (tplCompletionItem.getItemText().equals(command)) {
                        it.remove();
                        break;
                    }
                }
            }
            return  availableItems;
        }



    }
}
