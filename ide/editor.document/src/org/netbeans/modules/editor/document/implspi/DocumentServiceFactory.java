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

package org.netbeans.modules.editor.document.implspi;

import javax.swing.text.Document;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Allows to attach a service implementation to the document. A service
 * implementation may be a {@link CharClassifier}, {@link AtomicDocument}
 * or any public service interface to be available together with a certain
 * document implementation.
 * <p/>
 * The implementation should be registered on path <code>Editors/Documents/
 * &ltimplementation-class-fqn></code> using {@link ServiceProvider} or XML
 * layer.
 * <p/>
 * DocumentServices are merged for all supertypes of the actual document's impl
 * class, services registered for more specific types take priority.
 * @author sdedic
 */
public interface DocumentServiceFactory<D extends Document> {
    public Lookup forDocument(D doc);
}
