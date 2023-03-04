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
package org.netbeans.modules.groovy.editor.java;


import javax.lang.model.element.Element;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;

/** Utility class for opening elements in editor.
 *  XXX Basic code copied from org.netbeans.api.java.source.ui.ElementOpen
 *
 * @author Jan Lahoda
 */
public final class ElementDeclaration {
    private static Logger log = Logger.getLogger(ElementDeclaration.class.getName());

    private ElementDeclaration() {
        super();
    }

    public static CompletableFuture<DeclarationLocation> getDeclarationLocation(final ClasspathInfo cpInfo, final Element el) {
        ElementHandle<Element> handle = ElementHandle.create(el);
        return ElementOpen.getLocation(cpInfo, handle, handle.getQualifiedName().replace('.', '/') + ".class").thenApply(location -> {
            return location != null ? new DeclarationLocation(location.getFileObject(), location.getStartOffset()) : DeclarationLocation.NONE;
        });
    }
}
