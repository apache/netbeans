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

/*
 * DocumentTypeParser.java
 *
 * Created on January 8, 2006, 2:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever;

import java.io.File;
import java.util.List;
import org.netbeans.modules.xml.retriever.impl.*;
import org.openide.filesystems.FileObject;

/**
 *
 * @author girix
 */
public interface DocumentTypeParser {
    
    /**
     * will be called by factory to check if the mimeType is accepted by this parser.
     * @param mimeType MIME type of the current document
     */
    boolean accept(String mimeType);
    /**
     * this method will be called by the client to get all the external references found in this fileObject.
     * @param fob FileObject of the file that needs to be pased.
     */
    List<String> getAllLocationOfReferencedEntities(FileObject fob) throws Exception;
    
    List<String> getAllLocationOfReferencedEntities(File fileToBeParsed) throws Exception;
}
