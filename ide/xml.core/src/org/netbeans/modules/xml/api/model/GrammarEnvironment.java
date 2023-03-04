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

package org.netbeans.modules.xml.api.model;

import java.util.Enumeration;
import org.openide.filesystems.FileObject;
import org.xml.sax.InputSource;

/**
 * Grammar environment provides grammar factory with a context.
 * All returned object must be treated as read-only. Grammar
 * should do the best it can do in passed (possibly partial)
 * environment.
 *
 * @author  Petr Kuzel
 */
public final class GrammarEnvironment {

    private final FileObject fileObject;
    private final InputSource inputSource;
    private final Enumeration documentChildren;
        
    /** 
     * Creates a new instance of GrammarEnvironment.
     *
     * @param documentChildren Enumeration of document level DOM nodes.
     * @param inputSource Supported document input source.
     * @param fileObject  Supported document fileObject or <code<null</code>.
     */
    public GrammarEnvironment(Enumeration documentChildren, InputSource inputSource, FileObject fileObject) {
        if (inputSource == null) throw new NullPointerException();
        if (documentChildren == null) throw new NullPointerException();
        this.inputSource = inputSource;
        this.fileObject = fileObject;
        this.documentChildren = documentChildren;
    }
    
    /**
     * There is always input source of supported document reflecting
     * current in-memory state.
     *
     * @return InputSource
     */
    public InputSource getInputSource() {
        return inputSource;
    }
    
    /**
     * If supported document exists in a form of FileObject it can
     * be retrieved. Note that data provided from file object
     * input stream may be different than current in-memory state.
     *
     * @return FileObject or null.
     */
    public FileObject getFileObject() {
        return fileObject;
    }

    /**
     * Preparsed document children for fast decision based on
     * document structure.
     *
     * @return Enumeration of DOM Nodes representing document
     * children. Enumeration elements are valid only during
     * {@link GrammarQueryManager#enabled} method invocation.
     */
    public Enumeration getDocumentChildren() {
        return documentChildren;
    }
}
