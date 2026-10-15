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

package org.netbeans.modules.csl.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * This represents an error registered for the current java source,
 * possibly with associated fix proposals.
 *
 * @todo Add a getArgs() method etc. such that error messages can be parameterized; see javax.tools.DiagnosticMessage
 *
 * @author Tor Norbye
 */
public interface Error  {
    /**
     * Provide a short user-visible (and therefore localized) description of this error
     */
    @NonNull
    String getDisplayName();

    /**
     * Provide a full sentence description of this item, suitable for display in a tooltip
     * for example
     */
    @CheckForNull
    String getDescription();

    /**
     * Return a unique id/key for this error, such as "compiler.err.abstract.cant.be.instantiated".
     * This key is used for error hints providers.
     */
    @CheckForNull
    String getKey();
    
    // /** 
    // * Get the fixes associated with this error 
    // */
    //Collection<Fix> getFixes();
    //
    // /** 
    // * Register a fix proposal for this error 
    // */
    //void addFix(Fix fix);
    
    /**
     * Get the file object associated with this error, if any
     */
    @CheckForNull
    FileObject getFile();

    /**
     * Get the position of the error in the parsing input source (in other words,
     * this is the AST-based offset and may need translation to obtain the document
     * offset in documents with an embedding model.)
     */
    int getStartPosition();
    
    /**
     * Get the end offset of the error in the parsing input source (in other words,
     * this is the AST-based offset and may need translation to obtain the document
     * offset in documents with an embedding model.).
     * 
     * @return The end position, or -1 if unknown.
     */
    int getEndPosition();
    
    /**
     * Defines the way how an error annotation for this error behaves in the editor.
     * 
     * @return true if the error annotation should span over the whole line, false if 
     * the annotation is restricted exactly by the range defined by getStart/EndPostion() 
     */
    boolean isLineError();

    /**
     *  Get the severity of this error
     */
    @NonNull
    Severity getSeverity();
    
    /**
     * Return optional parameters for this message. The parameters may
     * provide the specific unknown symbol name for an unknown symbol error,
     * etc.
     */
    @CheckForNull
    Object[] getParameters();

    /**Error that may be used to show error badge in the projects tab.
     * @since 1.18
     */
    public interface Badging extends Error {
        /**Whether or not the error should be used to show error badge in the projects tab.
         *
         * @return true if this error should be used to show error badge in the projects tab.
         */
        public boolean showExplorerBadge();
    }
}
