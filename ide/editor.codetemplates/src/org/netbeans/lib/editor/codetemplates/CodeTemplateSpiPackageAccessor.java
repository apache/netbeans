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

package org.netbeans.lib.editor.codetemplates;

import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;


/**
 * Accessor for the package-private functionality in org.netbeans.api.editor.fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class CodeTemplateSpiPackageAccessor {

    private static CodeTemplateSpiPackageAccessor INSTANCE;
    
    public static CodeTemplateSpiPackageAccessor get() {
        if (INSTANCE == null) {
            try {
                Class<?> clazz = Class.forName(CodeTemplateInsertRequest.class.getName());
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        
        assert INSTANCE != null : "There is no SPI package accessor available!"; //NOI18N
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(CodeTemplateSpiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }
    
    public abstract CodeTemplateInsertRequest createInsertRequest(
            CodeTemplateInsertHandler handler);

    public abstract CodeTemplateParameter createParameter(CodeTemplateParameterImpl impl);

    public abstract CodeTemplateParameterImpl getImpl(CodeTemplateParameter parameter);

}
