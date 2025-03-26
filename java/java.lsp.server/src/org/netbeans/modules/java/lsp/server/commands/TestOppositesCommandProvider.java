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
package org.netbeans.modules.java.lsp.server.commands;

import com.google.gson.JsonPrimitive;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.gototest.TestOppositesLocator;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=CommandProvider.class)
public class TestOppositesCommandProvider implements CommandProvider {

    private static final String NBLS_GO_TO_TEST = "nbls.go.to.test";
    private static final Set<String> COMMANDS = new HashSet<>(Arrays.asList(NBLS_GO_TO_TEST));

    @Override
    public Set<String> getCommands() {
        return COMMANDS;
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        switch (command) {
            case NBLS_GO_TO_TEST: {
                try {
                    String source = ((JsonPrimitive) arguments.get(0)).getAsString();
                    FileObject file;
                    file = Utils.fromUri(source);
                    return TestOppositesLocator.getDefault().findOpposites(file, -1).thenApply(locations -> locations);
                } catch (MalformedURLException ex) {
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }
            }
            default:
                throw new UnsupportedOperationException("Command not supported: " + command);
        }
    }

}
