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
package org.netbeans.modules.css.model.impl;

import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.CharSet;
import org.netbeans.modules.css.model.api.Imports;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Namespaces;
import org.netbeans.modules.css.model.api.StyleSheet;

/**
 *
 * @author marekfukala
 */
public class StyleSheetI extends ModelElement implements StyleSheet {

    private CharSet charSet;
    private Imports imports;
    private Namespaces namespaces;
    private Body body;
    
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(CharSet value) {
            charSet = value;
        }

        @Override
        public void elementAdded(Imports value) {
            imports = value;
        }

        @Override
        public void elementAdded(Namespaces value) {
            namespaces = value;
        }

        @Override
        public void elementAdded(Body value) {
            body = value;
        }
    };

    public StyleSheetI(Model model) {
        super(model);
    }

    public StyleSheetI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public void setBody(Body body) {
        setElement(body);
    }

    @Override
    public CharSet getCharSet() {
        return charSet;
    }

    @Override
    public void setCharSet(CharSet charSet) {
        setElement(charSet);
    }

    @Override
    public Imports getImports() {
        return imports;
    }

    @Override
    public void setImports(Imports imports) {
        setElement(imports);
    }

    @Override
    public Namespaces getNamespaces() {
        return namespaces;
    }

    @Override
    public void setNamespaces(Namespaces namespaces) {
        setElement(namespaces);
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    protected Class getModelClass() {
        return StyleSheet.class;
    }
    
}
