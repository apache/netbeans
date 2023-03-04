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
package org.netbeans.modules.javafx2.editor.completion.beans;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;

/**
 * Resolves name to a {@link FxBean} instance. The Provider is bound to a certain
 * {@link CompilationInfo}; when the controlling ParserTask ends, the Provider
 * becomes invalid, and may throw exceptions.
 * 
 * @author sdedic
 */
public interface FxBeanProvider {
    /**
     * Constructs and returns FxBean definition of the Javafx bean.
     * May return {@code null}, if the definition cannot be created, e.g.
     * when the requested class is missing. Also returns {@code null}, if 
     * the passed classname is {@code null}.
     * 
     * @param fullClassName fully qualified name of the class, whose FxBean definition
     * should be created. {@code null} is accepted, and results in {@code null} result.
     * 
     * @return FxBean instance that describes the 'fullClassName' class JavaFX features,
     * or {@code null}, if the information could not be obtained.
     */
    @CheckForNull
    public FxBean   getBeanInfo(@NullAllowed String fullClassName);
    @NonNull
    public CompilationInfo getCompilationInfo();
}
