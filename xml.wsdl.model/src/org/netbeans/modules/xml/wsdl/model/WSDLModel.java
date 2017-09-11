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

package org.netbeans.modules.xml.wsdl.model;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;

/**
 *
 * @author rico
 *  This interface represents an instance of a wsdl model. A wsdl model is
 * bound to a single file.
 */
public abstract class WSDLModel extends AbstractDocumentModel<WSDLComponent> implements Referenceable {
    
    protected WSDLModel(ModelSource source) {
        super(source);
    }
    
    /**
     * @return WSDL model root component 'definitions'
     */
    public abstract Definitions getDefinitions();
    
    /**
     * @return WSDL component factory.
     */
    public abstract WSDLComponentFactory getFactory();
    
    /**
     * Search from all imported WSDL models those with specified target namespace.
     * @param namespaceURI the target namespace to search for model
     * @return list WSDL models or empty list if none found.
     */
    public abstract List<WSDLModel> findWSDLModel(String namespaceURI);
    
    /**
     * Search for all schemas visible from imported/included/redefined in the 
     * schema extensibility elements.  Schema model imported through wsdl:import
     * are also in the search.
     * @param namespaceURI the target namespace to search for model
     * @return list of schema match the give namespace.
     */
    public abstract List<Schema> findSchemas(String namespaceURI);
    
    /**
     * Find named WSDL component by name and type within current model.
     * @param name local name of target component
     * @param type type of target component
     * @return WSDL component of specified type and name; null if not found.
     */
    public abstract <T extends ReferenceableWSDLComponent> T findComponentByName(String name, Class<T> type);
    
    /**
     * Find named WSDL component by QName and type.
     * @param name QName of the target component.
     * @param type type of target component
     * @return WSDL component of specified type and name; null if not found.
     */
    public abstract <T extends ReferenceableWSDLComponent> T findComponentByName(QName name, Class<T> type);
}
