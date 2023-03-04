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
package org.netbeans.modules.html.editor.lib.api.dtd;

import java.io.Reader;
import java.util.Collection;
import org.openide.filesystems.FileObject;

/**
 * DTDReaderProvider is interface used as a source of Readers used to parse DTD
 * by DTDParser. One DTDReaderProvider shall offer all Readers for a given DTD,
 * i.e. the provider for "-//W3C//DTD HTML 4.01//EN" shall also provide Readers
 * for proper "-//W3C//ENTITIES Latin1//EN/HTML", as this public entity is
 * referred from HTML 4.01 DTD and the file provided with 4.01 DTD differs
 * from the file provided with 4.0 DTD although they have the same
 * public identifier (They differ only in comments, though).
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public interface ReaderProvider {

    /* Asks for Reader providing content of DTD file identified by 
     * given identifier, and possibly by given fileName.
     * These parameters are typically obtained from invocation DTD directive
     * like &lt;!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES Latin1//EN//HTML" "HTMLlat1.ent">,
     * in this case, the string -//W3C//....//HTML" is identifier
     * and "HTMLlat1.ent" is name of file in which it is probably stored
     * @param identifier the public identifier of required DTD
     * @param fileName the probable name of file with DTD data, may be
     *      <CODE>null</CODE>. It is used only as helper to identifier.
     * @return Reader from which to read out the DTD content.
     */
    public Reader getReaderForIdentifier( String identifier, String fileName );
    
    /** Asks for all the identifiers available from this ReaderProvider.
     * @returns a Collection of all identifiers for which this ReaderProvider
     * is able to provide Readers for.
     */
    public Collection<String> getIdentifiers();

    /**
     *
     * @param publicId
     * @return an internall system ID resource as FileObject for the given public ID
     */
    public FileObject getSystemId(String publicId);

    /**
     * @param public identifiers
     * @return true if the content to parse is xml content, false if sgml content
     */
    public boolean isXMLContent(String identifier);
}
