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
package org.netbeans.modules.php.smarty.editor.completion.entries;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.netbeans.modules.php.smarty.editor.completion.TplCompletionItem;
import org.netbeans.modules.php.smarty.editor.completion.TplCompletionItem.BuiltInFunctionsCompletionItem;
import org.netbeans.modules.php.smarty.editor.completion.TplCompletionItem.CustomFunctionsCompletionItem;
import org.netbeans.modules.php.smarty.editor.completion.TplCompletionItem.VariableModifiersCompletionItem;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Fousek
 */
public class SmartyCodeCompletionOffer {

    private static final ArrayList<TplCompletionItem> completionItemsFunctions = new ArrayList<TplCompletionItem>();
    private static final ArrayList<TplCompletionItem> completionItemsModifiers = new ArrayList<TplCompletionItem>();
    private static final HashMap<String, ArrayList<TplCompletionItem>> completionItemsFunctionParams = new HashMap<String, ArrayList<TplCompletionItem>>();

    static {
        //loadFunctions(new String[]{"built-in-functions", "custom-functions"});
        //loadModifiers("variable-modifiers");
    }

    public static ArrayList<TplCompletionItem> getFunctions() {
        return completionItemsFunctions;
    }

    public static ArrayList<TplCompletionItem> getVariableModifiers() {
        return completionItemsModifiers;
    }

    public static HashMap<String, ArrayList<TplCompletionItem>> getFunctionParameters() {
        return completionItemsFunctionParams;
    }

    private static void loadFunctions(String[] types) {
        for (String completionType : types) {
            Collection<CodeCompletionEntryMetadata> ccList = parseCCData(completionType);
//            if (completionType.equals("built-in-functions")) {
//                for (CodeCompletionEntryMetadata entryMetadata : ccList) {
//                    completionItemsFunctions.add(new BuiltInFunctionsCompletionItem(entryMetadata.getKeyword(), entryMetadata.getHelp(), entryMetadata.getHelpUrl()));
//                    completionItemsFunctionParams.put(entryMetadata.getKeyword(), entryMetadata.getParameters());
//                }
//            } else if (completionType.equals("custom-functions")) {
//                for (CodeCompletionEntryMetadata entryMetadata : ccList) {
//                    completionItemsFunctions.add(new CustomFunctionsCompletionItem(entryMetadata.getKeyword(), entryMetadata.getHelp(), entryMetadata.getHelpUrl()));
//                    completionItemsFunctionParams.put(entryMetadata.getKeyword(), entryMetadata.getParameters());
//                }
//
//            }
        }
    }

    private static void loadModifiers(String functionsFile) {
        Collection<CodeCompletionEntryMetadata> ccList = parseCCData(functionsFile);

        for (CodeCompletionEntryMetadata entryMetadata : ccList) {
            completionItemsModifiers.add(new VariableModifiersCompletionItem(entryMetadata.getKeyword(), entryMetadata.getHelp(), entryMetadata.getHelpUrl()));
        }
    }

    private static Collection<CodeCompletionEntryMetadata> parseCCData(String filePath) {
        Collection<CodeCompletionEntryMetadata> ccList = new ArrayList<CodeCompletionEntryMetadata>();
        InputStream inputStream = SmartyCodeCompletionOffer.class.getResourceAsStream("defs/" + filePath + ".xml"); //NOI18N

        try {
            Collection<CodeCompletionEntryMetadata> ccData = CodeCompletionEntries.readAllCodeCompletionEntriesFromXML(inputStream, filePath);
            ccList.addAll(ccData);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return ccList;
    }
}
