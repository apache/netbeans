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
package org.netbeans.modules.java.source.usages;

import java.net.URL;
import java.util.EventObject;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;

/**
 *
 * @author Tomas Zezula
 */
public class ClassIndexImplEvent extends EventObject {
    
    private final URL root;
    private final ElementHandle<ModuleElement> module;
    private final Iterable<? extends ElementHandle<TypeElement>> types;

    ClassIndexImplEvent (
            final ClassIndexImpl source,
            final URL root,
            final ElementHandle<ModuleElement> module,
            final Iterable<? extends ElementHandle<TypeElement>> types) {
        super (source);
        assert root != null;
        assert types != null;
        this.root = root;
        this.module = module;
        this.types = types;
    }

    @NonNull
    public URL getRoot() {
        return this.root;
    }

    @CheckForNull
    public ElementHandle<ModuleElement> getModule() {
        return this.module;
    }

    @NonNull
    public Iterable<? extends ElementHandle<TypeElement>> getTypes () {
        return this.types;
    }

}
