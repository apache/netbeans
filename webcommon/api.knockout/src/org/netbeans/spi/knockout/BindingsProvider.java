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
package org.netbeans.spi.knockout;

import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * A provider that knows more about structure of Knockout bindings in an HTML
 * page. Providers should be registered by {@link ServiceProvider} annotation so
 * they can be found for a particular HTML file. Their
 * {@link #findBindings(org.openide.filesystems.FileObject, org.netbeans.spi.knockout.BindingsProvider.Response) findBindings}
 * method is called when a knockout code completion is about to be shown in the
 * HTML editor and then can generate a JSON like description of the structure of
 * knockout context via {@link Bindings} methods.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public interface BindingsProvider {

    /**
     * Called when additional completion is requested for a particular file.
     *
     * @param htmlFile the HTML file opened in editor
     * @return {@code Response} instance with bindings
     */
    public Response findBindings(FileObject htmlFile);

    /**
     * Methods for {@link BindingsProvider}s to provide a response when
     * {@link BindingsProvider#findBindings(org.openide.filesystems.FileObject, org.netbeans.spi.knockout.BindingsProvider.Response) queries}
     * about a particular HTML file.
     */
    public static final class Response {

        Bindings bindings;
        String targetId;

        private Response(Bindings bindings) {
            this.bindings = bindings;
            this.targetId = null;
        }

        private Response(Bindings bindings, String targetId) {
            this.bindings = bindings;
            this.targetId = targetId;
        }

        /**
         * Used by a {@link BindingsProvider provider} to tell the editing
         * infrastructure to expose a JSON like structure in code completion.
         *
         * @param bindings Java model of JSON-like structure
         * @return new {@code Response} instance
         */
        public static Response create(Bindings bindings) {
            return new Response(bindings);
        }

        /**
         * Used by a {@link BindingsProvider provider} to tell the editing
         * infrastructure to expose a JSON like structure in code completion.
         *
         * @param bindings Java model of JSON-like structure
         * @param targetId an id of an element on the page to identify a subtree
         * where these bindings will be applied to
         * @return new {@code Response} instance
         */
        public Response create(Bindings bindings, String targetId) {
            return new Response(bindings, targetId);
        }
    }
}
