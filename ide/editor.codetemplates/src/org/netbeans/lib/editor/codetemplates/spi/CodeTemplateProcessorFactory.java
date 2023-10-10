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

package org.netbeans.lib.editor.codetemplates.spi;

import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Factory constructs code template processor for a given insert request.
 * <br>
 * The factory instances are looked up
 * by {@link org.netbeans.api.editor.mimelookup.MimeLookup}
 * so they should be registered in an xml-layer in
 * <i>Editors/&lt;mime-type&gt;/CodeTemplateProcessorFactories</i> directory.
 *
 * @author Miloslav Metelka
 */
@MimeLocation(subfolderName="CodeTemplateProcessorFactories")
public interface CodeTemplateProcessorFactory {

    /**
     * Create an instance of code template processor for a given insert request.
     *
     * @param request non-null code template insert request to be processed
     *  by the given processor instance.
     * @return non-null instance of the processor. The constructed instance
     *  should be given the insert request so that it can operate on it.
     */
    CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request);
    
}
