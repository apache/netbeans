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

package org.netbeans.api.java.source;

import org.netbeans.modules.parsing.api.Snapshot;

/**Binding between virtual Java source and the real source.
 * Please note that this class is needed only for clients that need to work
 * in non-Java files (eg. JSP files) or in dialogs, like code completion.
 * Most clients do not need to use this class.
 * 
 * @author Dusan Balek
 * @since 0.21
 */
public final class PositionConverter {
    
    private final Snapshot snapshot;
    
    PositionConverter (final Snapshot snapshot) {
        assert snapshot != null;
        this.snapshot = snapshot;        
    }

    PositionConverter() {
        this.snapshot = null;
    }
    
    // API of the class --------------------------------------------------------

    /**Compute position in the document for given position in the virtual
     * Java source.
     * 
     * @param javaSourcePosition position in the virtual Java Source
     * @return position in the document
     * @since 0.21
     */
    public int getOriginalPosition(int javaSourcePosition) {
        return snapshot != null ? snapshot.getOriginalOffset(javaSourcePosition) : javaSourcePosition;
    }
    
    /**Compute position in the virtual Java source for given position
     * in the document.
     *
     * @param originalPosition position in the document
     * @return position in the virtual Java source
     * @since 0.21
     */
    public int getJavaSourcePosition(int originalPosition) {
        return snapshot != null ? snapshot.getEmbeddedOffset(originalPosition) : originalPosition;
    }        

}
