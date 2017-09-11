/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
