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

package org.netbeans.spi.editor.codegen;

import java.util.List;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.util.Lookup;

/**
 * Interface to be implemented by all generators inserting their code snippets 
 * into documents using the Insert Code editor action.
 *
 * @author Dusan Balek
 * @since 1.8
 */
public interface CodeGenerator {

    /**
     * Gets the generator's name to be displayed in the popup that appears on
     * the Insert Code action invocation.
     * @return non-null name
     * @since 1.8
     */
    public String getDisplayName();

    /**
     * Invokes the generator to create the code snippet and insert it into a
     * document.
     * @since 1.8
     */
    public void invoke();
    
    /**
     * Factory creating code generators.<br> The factory instances are looked up
     * by the {@link org.netbeans.api.editor.mimelookup.MimeLookup} so they
     * should be registered in an xml-layer in
     * <i>Editors/&lt;mime-type&gt;/CodeGenerators</i> directory.
     * @since 1.8
     */
    @MimeLocation(subfolderName="CodeGenerators")
    public interface Factory {
        
        /**
         * Creates code generators valid for the particular context.
         * @param context Contains an instance of
         * {@link javax.swing.text.JTextComponent} by default. Additional content
         * could be added by the {@link CodeGeneratorContextProvider} registered
         * for the mime type.
         * @return the list of created code generators. An empty list should be
         * returned if no generator could be created in the particular context.
         * @since 1.8
         */
        public List<? extends CodeGenerator> create(Lookup context);
    }
}
