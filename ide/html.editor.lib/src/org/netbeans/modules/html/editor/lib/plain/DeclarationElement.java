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
package org.netbeans.modules.html.editor.lib.plain;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.Declaration;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;

/**
 * Declaration models SGML declaration with emphasis on &lt;!DOCTYPE
 * declaration, as other declarations are not allowed inside HTML. It represents
 * unknown/broken declaration or either public or system DOCTYPE declaration.
 *
 * @author mfukala@netbeans.org
 */
public class DeclarationElement extends AbstractElement implements Declaration {

    private String root;
    private String publicID;
    private String file;
    private String doctypeName;

    /**
     * Creates a model of SGML declaration with some properties of DOCTYPE
     * declaration.
     *
     * @param doctypeRootElement the name of the root element for a DOCTYPE. Can
     * be null to express that the declaration is not DOCTYPE declaration or is
     * broken.
     * @param doctypePI public identifier for this DOCTYPE, if available. null
     * for system doctype or other/broken declaration.
     * @param doctypeFile system identifier for this DOCTYPE, if available. null
     * otherwise.
     */
    public DeclarationElement(CharSequence document, int from, short length,
            String doctypeRootElement,
            String doctypePI, String doctypeFile, String doctypeName) {
        super(document, from, length);
        root = doctypeRootElement;
        publicID = doctypePI;
        file = doctypeFile;
        this.doctypeName = doctypeName;
    }

    @Override
    public ElementType type() {
        return ElementType.DECLARATION;
    }

    /**
     * @return a public identifier of the PUBLIC DOCTYPE declaration or null for
     * SYSTEM DOCTYPE and broken or other declaration.
     */
    @Override
    public CharSequence publicId() {
        return publicID;
    }

    /**
     * @return a system identifier of both PUBLIC and SYSTEM DOCTYPE declaration
     * or null for PUBLIC declaration with system identifier not specified and
     * broken or other declaration.
     */
    @Override
    public CharSequence systemId() {
        return file;
    }

    /**
     * @return the declaration id name, e.g. DOCTYPE for <!DOCTYPE ... >
     * declaration
     */
    @Override
    public CharSequence declarationName() {
        return doctypeName;
    }

    /**
     * @return the name of the root element for a DOCTYPE declaration or null if
     * the declatarion is not DOCTYPE or is broken.
     */
    @Override
    public CharSequence rootElementName() {
        return root;
    }

   
}
