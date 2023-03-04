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

package org.netbeans.modules.groovy.editor.compiler.error;

import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Janicek
 */
public final class CompilerErrorResolver {

    private static final String UNABLE_TO_RESOLVE_CLASS = "unable to resolve class "; // NOI18N
    private static final String CLASS_DOES_NOT_IMPLEMENT_ALL_METHODS = "Can't have an abstract method in a non-abstract class."; // NOI18N
    
    
    private CompilerErrorResolver() {
    }
    
    /**
     * Finds correct {@link GroovyCompilerErrorID} for the given error message.
     * 
     * @param errorMessage message that is typically provided by the groovyc
     * @return corresponding {@link GroovyCompilerErrorID}
     */
    public static CompilerErrorID getId(@NonNull String errorMessage) {
        Parameters.notNull("errorMessage", errorMessage);
        
        if (errorMessage.startsWith(UNABLE_TO_RESOLVE_CLASS)) {
            return CompilerErrorID.CLASS_NOT_FOUND;
        }
        
        if (errorMessage.startsWith(CLASS_DOES_NOT_IMPLEMENT_ALL_METHODS)) {
            return CompilerErrorID.CLASS_DOES_NOT_IMPLEMENT_ALL_METHODS;
        }

        return CompilerErrorID.UNDEFINED;
    }
}
