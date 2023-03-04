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
            Iterator<TplCompletionItem> it = availableItems.iterator();
            while (it.hasNext()) {
                TplCompletionItem tplCompletionItem = it.next();
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
