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

package org.netbeans.modules.web.core.syntax;

import javax.swing.text.Document;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 *
 * @author Petr Pisl
 */
/** Until NetBeans 5.5 the code completion usually offered
 * only tags of libraries, which are already imported in
 * the page. More user-friendly is when all possible tags
 * from libraries, which are on the classpath, are offered
 * with the  code completion. Similar to the java code
 * completion. Implementation of this class provides
 * the functionality that makes auto tag library definition
 * in the document. The way, how the tag library definition
 * is done, depends on the type of document.
 *
 * The implementation has to be registered in the default
 * filesystem (in layer file) in the folder
 * Editors/${mime-types}/AutoTagImportProviders
 */
@MimeLocation(subfolderName="AutoTagImportProviders")
public interface AutoTagImporterProvider {

    /** The method is called, when user select a tag in
     * the code completion window and the tag is inserted into
     * the document. The implementation has to decide,
     * whether  the tag library is already defined, whether
     * the tag library has to be imported and if necessary
     * write the tag library declaration into the document.
     *
     * @param doc document on which the declaration should be written.
     * @param prefix prefix of the library
     * @param uri uri of the library
     */
    public void importLibrary(Document doc, String prefix, String uri);

    /** The implementation has to returns a prefix for an library defined
     * in a document. It returns null if the prefix is not defined in the
     * document.
     * <p>This is useful for example for palette items. When user drops some
     * user tag from palette, then the client code should ask for the prefix, which
     * is defined for the document. If there is the library definition
     * in the document then this method returns the defined prefix and the user
     * tag will be completed with the prefix. If this method returns null
     * (there is not the library definition in the document), then the client code
     * should cold importLibrary method to  add the library definition
     * to the document. </p>
     *
     * @param doc document where the declaration should be found.
     * @param uri uri of the library
     * @return the prefix defined for the library from library definition in the document\
     * or null if there is not the library definition in the document.
     */
    public String getDefinedPrefix(Document doc, String uri);
}
