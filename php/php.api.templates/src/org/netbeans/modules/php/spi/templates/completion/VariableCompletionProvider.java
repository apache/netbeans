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
package org.netbeans.modules.php.spi.templates.completion;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * SPI for extending PHP templates code completion.
 * <p>
 * <i>All the methods are called only for the {@link FileObject}
 * that is currently opened in the editor and where the code completion is
 * invoked.</i>
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 * @deprecated Use {@link CompletionProvider} instead.
 */
@Deprecated
public interface VariableCompletionProvider {

    /**
     * Gets the set of {@link String variable names} which should be displayed in the code completion.
     *
     * @param templateFile {@link FileObject template file} in which the code completion was invoked
     * @return Set of {@link String variable names} which should be displayed in the code completion.
     */
    @NonNull
    Set<String> getVariables(@NonNull FileObject templateFile);

}
