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



package org.netbeans.modules.jumpto.quicksearch;

import org.netbeans.modules.jumpto.symbol.GoToSymbolAction;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 */
public class GoToSymbolProvider implements SearchProvider {

    private GoToSymbolWorker worker;

    public void evaluate(SearchRequest request, SearchResponse response) {
        String text = removeNonJavaChars(request.getText());
        if(text.length() == 0) {
            return;
        }
        GoToSymbolWorker local;
        synchronized(this) {
            if (worker!=null) {
                worker.cancel();
            }
            worker = new GoToSymbolWorker(text);
            local = worker;
        }
        local.run();
        
        for (SymbolDescriptor td : local.getTypes()) {
            String displayHint = td.getFileDisplayPath();
            String htmlDisplayName = escapeLtGt(td.getSymbolName()) + " " + NbBundle.getMessage(GoToSymbolAction.class, "MSG_DeclaredIn",escapeLtGt(td.getOwnerName()));
            final String projectName = td.getProjectName();
            if (projectName != null && !projectName.isEmpty()) {
                htmlDisplayName = String.format(
                    "%s [%s]",  //NOI18N
                    htmlDisplayName,
                    projectName);
            }
            if (!response.addResult(new GoToSymbolCommand(td),
                                    htmlDisplayName,
                                    displayHint,
                                    null)) {
                break;
            }
        }
    }
     
    private static String escapeLtGt(String input) {
        return input.replace("<", "&lt;").replace(">", "&gt;"); // NOI18N
    }
     
    private static class GoToSymbolCommand implements Runnable {
        private SymbolDescriptor command;
        
        public GoToSymbolCommand(SymbolDescriptor command) {
            this.command = command;
        }

        public void run() {
            command.open();
        }
    }

    private static String removeNonJavaChars(String text) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
