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

package org.netbeans.api.java.source;

import java.net.URL;
import java.util.EventObject;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Event used to notify the {@link ClassIndexListener} about
 * a change of declared types in the {@link ClassIndex}
 * @author Tomas Zezula
 */
public final class TypesEvent extends EventObject {

    private final URL root;
    private final Iterable<? extends ElementHandle<TypeElement>> types;
    private final ElementHandle<ModuleElement> module;

    TypesEvent (
            final ClassIndex source,
            final URL root,
            final ElementHandle<ModuleElement> module,
            final Iterable<? extends ElementHandle<TypeElement>> types) {
        super (source);
        Parameters.notNull("root", root);   //NOI18N
        Parameters.notNull("types", types); //NOI18N
        this.root = root;
        this.module = module;
        this.types = types;
    }

    /**
     * Returns an {@link URL} of the affected root.
     * @return the affected root
     * @since 2.23
     */
    @NonNull
    public URL getRoot() {
        return root;
    }

    /**
     * Returns the affected declared types.
     * @return an {@link Iterable} of {@link TypeElement} handles
     */
    @NonNull
    public Iterable<? extends ElementHandle<TypeElement>> getTypes () {
        return this.types;
    }

    /**
     * Returns the affected module in case of module-info change.
     * @return a {@link ModuleElement} handles
     * @since 2.23
     */
    @CheckForNull
    public ElementHandle<ModuleElement> getModule() {
        return this.module;
    }

    @NonNull
    @Override
    public String toString () {
        return String.format(
                "TypesEvent for root: %s changed module: %s, changed types: %s",    //NOI18N
                root,
                module,
                types);
    }

}
