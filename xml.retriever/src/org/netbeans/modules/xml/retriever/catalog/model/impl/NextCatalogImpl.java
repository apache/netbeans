/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * NextCatalogImpl.java
 *
 * Created on December 6, 2006, 6:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.catalog.model.impl;

import java.net.URI;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogAttributes;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author girix
 */
public class NextCatalogImpl extends CatalogComponentImpl implements
        org.netbeans.modules.xml.retriever.catalog.model.NextCatalog{
    
    public NextCatalogImpl(CatalogModelImpl model, Element e) {
        super(model, e);
    }
    
    public NextCatalogImpl(CatalogModelImpl model) {
        this(model, createElementNS(model, CatalogQNames.NEXTCATALOG));
    }
    
    public void accept(CatalogVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getCatalogAttr() {
        return getAttribute(CatalogAttributes.catalog);
    }

    public void setCatalogAttr(URI uri) {
        super.setAttribute(CATALOG_ATTR_PROP, CatalogAttributes.catalog, uri.toString());
    }
    
}
