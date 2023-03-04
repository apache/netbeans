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
package org.netbeans.modules.html.editor.lib.api;

import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;

/**
 * Allows to resolve the handle to an instance of {@link Node} from 
 * the given {@link Parser.Result}.
 *
 * @since 3.10
 * @author marekfukala
 */
public interface SourceElementHandle {
    
    /**
     * Returns the enclosing {@link FileObject}.
     */
    public FileObject getFileObject();
    
    /**
     * Resolve the handle to an instance of {@link Node} from 
     * the given {@link Parser.Result}.
     * 
     * @param result 
     * @return instance of {@link Node} or null if the source handle cannot be resolved.
     */
    public Node resolve(Parser.Result result);
    
}
