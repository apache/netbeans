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
package org.netbeans.modules.java.lsp.server.files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.swing.text.Document;

/**
 * Gathers documents opened by the LSP client.
 *
 * @author Martin Entlicher
 */
public final class OpenedDocuments {

    private final Map<String, Document> openedDocuments = new ConcurrentHashMap<>();
    private final List<Consumer<String>> openedConsumers = new ArrayList<>();

    /**
     * Get URIs of opened documents.
     * @return a collection of URIs of opened documents.
     */
    public Collection<String> getUris() {
        return Collections.unmodifiableSet(openedDocuments.keySet());
    }

    /**
     * Get an opened document from an URI.
     * @param uri an URI
     * @return an opened document from the provided URI, or <code>null</code>
     * when no document was opened from the URI.
     */
    public Document getDocument(String uri) {
        return openedDocuments.get(uri);
    }

    /**
     * Notify that a document was opened from an URI.
     */
    public void notifyOpened(String uri, Document doc) {
        openedDocuments.put(uri, doc);
        synchronized (openedConsumers) {
            for (Consumer<String> c : openedConsumers) {
                c.accept(uri);
            }
        }
    }

    /**
     * Notify that a document was closed.
     */
    public Document notifyClosed(String uri) {
        return openedDocuments.remove(uri);
    }

    /**
     * Add a consumer to be notified with URIs of opened documents.
     * The added consumer is notified with the already opened documents immediately.
     */
    public void addOpenedConsumer(Consumer<String> openedConsumer) {
        synchronized (openedConsumers) {
            openedConsumers.add(openedConsumer);
        }
        for (String uri : getUris()) {
            openedConsumer.accept(uri);
        }
    }

    /**
     * Remove a consumer that was previously added.
     */
    public void removeOpenedConsumer(Consumer<String> openedConsumer) {
        synchronized (openedConsumers) {
            openedConsumers.remove(openedConsumer);
        }
    }
}
