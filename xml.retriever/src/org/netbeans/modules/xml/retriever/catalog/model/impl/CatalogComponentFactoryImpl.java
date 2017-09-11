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
package org.netbeans.modules.xml.retriever.catalog.model.impl;

import java.util.List;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.NextCatalog;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponent;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponentFactory;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.w3c.dom.Element;

public class CatalogComponentFactoryImpl implements CatalogComponentFactory {
    private CatalogModelImpl model;
    
    public CatalogComponentFactoryImpl(CatalogModelImpl model) {
        this.model = model;
    }
    
    public CatalogComponent create(Element element, CatalogComponent context) {
        if (context == null) {
            if (areSameQName(CatalogQNames.CATALOG, element)) {
                return new CatalogImpl(model, element);
            } else {
                return null;
            }
        } else {
            return new CreateVisitor().create(element, context);
        }
    }
    
    
    public NextCatalog createNextCatalog() {
        return new NextCatalogImpl(model);
    }
    
    public org.netbeans.modules.xml.retriever.catalog.model.System createSystem() {
        return new SystemImpl(model);
    }
    
    public Catalog createCatalog() {
        return new CatalogImpl(model);
    }
    
    public static boolean areSameQName(CatalogQNames q, Element e) {
        return q.getQName().equals(AbstractDocumentComponent.getQName(e));
    }
    
    public static class CreateVisitor extends CatalogVisitor.Default {
        Element element;
        CatalogComponent created;
        
        CatalogComponent create(Element element, CatalogComponent context) {
            this.element = element;
            context.accept(this);
            return created;
        }
        
        private boolean isElementQName(CatalogQNames q) {
            return areSameQName(q, element);
        }
        
        public void visit(Catalog context) {
            if (isElementQName(CatalogQNames.SYSTEM)) {
                created = new SystemImpl((CatalogModelImpl)context.getModel(), element);
            }
            if (isElementQName(CatalogQNames.NEXTCATALOG)) {
                created = new NextCatalogImpl((CatalogModelImpl)context.getModel(), element);
            }
        }
        
        public void visit(org.netbeans.modules.xml.retriever.catalog.model.System context) {
            
        }
        
        public void visit(NextCatalog context) {
            
        }
        
    }
}
