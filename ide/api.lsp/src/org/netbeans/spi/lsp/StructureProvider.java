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
package org.netbeans.spi.lsp;

import org.netbeans.api.lsp.StructureElement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Interface for building structure of symbols at a given document.
 * Implementations of the interface should be registered in MimeLookup.
 * <pre>
 *
 *  {@code @MimeRegistration(mimeType = "text/foo", service = StructureProvider.class)
 *   public class FooStructureProvider implements StructureProvider {
 *     ...
 *   }
 *  }
 * </pre>
 *
 * @author Petr Pisl
 * @since 1.8
 */

//@MimeLocation(subfolderName = "StructureProviders")
public interface StructureProvider {
    
    /**
     * Resolves a structure tree of symbols at the given document.
     *
     * @param doc document on which to operate.
     * @return a list of top level structure elements
     * 
     * @since 1.8
     */
    @NonNull
    CompletableFuture<List<? extends StructureElement>> getStructure(@NonNull Document doc);
}
