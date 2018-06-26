/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
