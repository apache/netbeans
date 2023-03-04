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

import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Jan Becicka
 * @author Tomas Zezula
 */
public class JavaTypeSearchProvider implements SearchProvider {

    private final AtomicReference<GoToTypeWorker> workerRef = new AtomicReference<GoToTypeWorker>();

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        String text = removeNonJavaChars(request.getText());
        if(text.length() == 0) {
            return;
        }

        final GoToTypeWorker newWorker = new GoToTypeWorker(text);
        final GoToTypeWorker toCancel = workerRef.getAndSet(newWorker);
        if (toCancel != null) {
            toCancel.cancel();
        }
        try {
            newWorker.run();
        } finally {
            workerRef.compareAndSet(newWorker, null);
        }
        
        for (TypeDescriptor td : newWorker.getTypes()) {
            String displayHint = td.getFileDisplayPath();
            String htmlDisplayName = td.getSimpleName() + td.getContextName();
            final String projectName = td.getProjectName();
            if (projectName != null && !projectName.isEmpty()) {
                htmlDisplayName = String.format(
                    "%s [%s]",  //NOI18N
                    htmlDisplayName,
                    projectName);
            }
            if (!response.addResult(new GoToTypeCommand(td),
                                    htmlDisplayName,
                                    displayHint,
                                    null)) {
                break;
            }
        }
    }
    
    private static class GoToTypeCommand implements Runnable {
        private TypeDescriptor command;
        
        public GoToTypeCommand(TypeDescriptor command) {
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
