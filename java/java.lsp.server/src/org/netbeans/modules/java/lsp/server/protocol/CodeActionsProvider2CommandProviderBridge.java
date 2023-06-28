/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=CodeActionsProvider.class)
public class CodeActionsProvider2CommandProviderBridge extends CodeActionsProvider {

    @Override
    public Set<String> getCommands() {
        Set<String> allCommands = new HashSet<>();

        for (CommandProvider cmdProvider : Lookup.getDefault().lookupAll(CommandProvider.class)) {
            allCommands.addAll(cmdProvider.getCommands());
        }

        return allCommands;
    }

    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        for (CommandProvider cmdProvider : Lookup.getDefault().lookupAll(CommandProvider.class)) {
            if (cmdProvider.getCommands().contains(command)) {
                return cmdProvider.runCommand(command, arguments);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }

}
