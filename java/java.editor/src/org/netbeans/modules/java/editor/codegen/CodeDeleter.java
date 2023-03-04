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

package org.netbeans.modules.java.editor.codegen;

import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

/**
 * Interface to be implemented by all deleters removing their code snippets 
 * from documents using the Remove Surrounding Code editor action.
 *
 * @author Dusan Balek
 */
public interface CodeDeleter {

    /**
     * Gets the deleter's name to be displayed in the popup that appears on
     * the Remove Surrounding Code action invocation.
     * @return non-null name
     */
    public String getDisplayName();

    /**
     * Invokes the deleter to remove the code snippet from a document.
     */
    public void invoke();
    
    /**
     * Gets a bag highlighting the code snippet to be removed.
     * @return non-null highlights bag.
     */
    public OffsetsBag getHighlight();
}
