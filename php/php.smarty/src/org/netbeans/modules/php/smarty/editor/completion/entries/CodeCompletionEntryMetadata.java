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

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.php.smarty.editor.completion.TplCompletionItem;
import org.netbeans.modules.php.smarty.editor.completion.TplCompletionItem.FunctionParametersCompletionItem;

/**
 *
 * @author Martin Fousek
 */
public class CodeCompletionEntryMetadata {

    private String keyword;
    private String help;
    private String helpUrl;
    private ArrayList<TplCompletionItem> params = new ArrayList<TplCompletionItem>();

    public CodeCompletionEntryMetadata(String keyword, String help, String helpUrl,
            Collection<CodeCompletionParamMetadata> params) {
        this.keyword = keyword;
        this.help = help;
        this.helpUrl = helpUrl;
        if (params != null) {
            for (CodeCompletionParamMetadata codeCompletionParamMetadata : params) {
                this.params.add(new FunctionParametersCompletionItem(
                        codeCompletionParamMetadata.getKeyword(),
                        codeCompletionParamMetadata.getHelp()));
            }
        }
    }

    public String getKeyword() {
        return keyword;
    }

    public String getHelp() {
        return help;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public ArrayList<TplCompletionItem> getParameters() {
        return params;
    }
}
