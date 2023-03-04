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
package org.netbeans.lib.nbjshell;

import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Kind;
import jdk.jshell.Snippet.Status;

/**
 * Encapsulates information necessary for JShell console to work.
 * Provides access to a snippet, if it exists, or at least to the wrapper,
 * if the Snippet does not exist.
 * 
 * @author sdedic
 */
public interface SnippetWrapping {
    /**
     * @return kind of the snippet.
     */
    public Kind    getSnippetKind();
    
    /**
     * Snippet status. Wrappers prepared from String code not
     * executed by JShell return {@link Status#NONEXISTENT}.
     * @return snippet status
     */
    public Status  getStatus();
    
    /**
     * Snippet instance, if executed by JShell
     * @return snippet instance of {@code null}.
     */
    public Snippet getSnippet();
    
    /**
     * Returns the complete wrapped text. 
     * @return wrapped text
     */
    public String  getCode();
    
    /**
     * Returns the original user's source
     * @return original source before parsing/executing/wrapping
     */
    public String  getSource();
    
    /**
     * Transforms original source position into the wrapped one.
     * @param pos original position
     * @return position inside the wrapper returned from {@link #getCode}
     */
    public int     getWrappedPosition(int pos);
    
    /**
     * Class name of the shell snippet
     * @return 
     */
    public String  getClassName();
}
