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

package org.netbeans.modules.web.indent.api.embedding;

import javax.swing.text.Document;

/**
 * Virtual source generated for a language from a document. The purpose of virtual
 * source is to extract individual parts of given language from a document
 * and amend such a source to be syntactically correct as much as possible.
 * 
 * @since org.netbeans.modules.css.editor/1 1.3
 */
public interface VirtualSource {

    /**
     * Returns text for given start and end offset from virtual source.
     * @param startOffset start offset
     * @param endOffset end offset
     * @return text lying within given range or null if there is none
     */
    String getSource(int startOffset, int endOffset);

    /**
     * Factory creating virtual source of given mime from a document.
     */
    public interface Factory {

        /**
         * Create virtual source of specified MIME type from given document.
         * @param doc document to extract virtual source from
         * @param mimeOfInterest MIME type which should be extracted from document
         * @return instance of virtual source or null factory does not know
         *  how to extract requested MIME type from given document
         */
        VirtualSource createVirtualSource(Document doc, String mimeOfInterest);
        
    }
}
