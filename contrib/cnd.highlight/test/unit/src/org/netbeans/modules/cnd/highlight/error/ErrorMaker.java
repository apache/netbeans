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

package org.netbeans.modules.cnd.highlight.error;

import java.util.Collection;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;

/**
 * Makes an error in a document that contain source code.
 * It can be called several times on a document:
 * for example, each time it finds a ";" and deletes it
 */
public interface ErrorMaker {
    
    /**
     * Performs initialization.
     * Is called before any other method.
     * @param document a document
     * @param csmFile the corresponding source file. 
     * I believe most error makers will just ignore it;
     * but some more smart one may need it
     */
    void init(BaseDocument document, CsmFile csmFile) throws Exception;
    
//    /**
//     * Is called after each iteration to determine
//     * whether to stop or proceed.
//     * @return true to proceed, false to stop
//     */
//    boolean hasMore();
//    
    
    /**
     * If it can make next change (e.g., if next ";" is found),
     * it makes the change and returns true.
     * In this case change() will be called once more.
     * 
     * If there are no more places to change,
     * does nothing and returns false -
     * this means that the cycle is over.
     * 
     * @return true if a change has been done, otherwise false.
     */
    boolean change() throws Exception;

    /**
     * Analyzes the errors that were reported after last change
     * Is called as soon as a change has been done 
     * and errors has been got from provider.
     * @param errors
     */
    void analyze(Collection<CsmErrorInfo> errors) throws Exception;
    
    /**
     * Notifies that the last change was undone.
     * One of the typical scenarios is to isolate changes one from another.
     * For example, we delete semicolon, see what error highlighting says in this respect,
     * then undo deletion, delete next semicolon, etc.
     * In this case error maker should be notified that its last change was undone
     */
    void undone() throws Exception;
    
}
