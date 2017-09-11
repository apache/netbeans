/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.xml.wsdl.validator.visitor.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class SchemaSemanticsVisitor extends DefaultSchemaVisitor {

     /**Import does not have imported document object */
    public static final String VAL_MISSING_IMPORTED_DOCUMENT = "VAL_MISSING_IMPORTED_DOCUMENT";
    public static final String FIX_MISSING_IMPORTED_DOCUMENT = "FIX_MISSING_IMPORTED_DOCUMENT";


    /** Creates a new instance of SchemaSemanticsVisitor */
    public List<ResultItem> mResultItems = new ArrayList<ResultItem>();
    private WSDLModel mParentModel;
    private Validator mValidator;
    private Validation mValidation;
    private List<Model> mValidatedModels;

    /** Creates a new instance of SchemaSemanticsVisitor */
    public SchemaSemanticsVisitor(WSDLModel parentModel,
    							  Validator validator,
                                  Validation validation, 
                                  List<Model> validatedModels) {
    	mParentModel = parentModel;
        mValidation = validation;
        mValidatedModels = validatedModels;
    }
    
    public List<ResultItem> getResultItems() {
        return mResultItems;
    }
    
    public void visit(Schema s) {
        if(s != null) {
            visitChildren(s);
        }
    }
    
    public void visit(Import im) {
        
        //verify if imported document is available
        Collection<Schema> schemas = im.getModel().findSchemas(im.getNamespace());
        
        if(schemas == null || schemas.isEmpty()) {
                // it can be an inline xsd in wsdl types section
        		WSDLSchema schema = findInlineSchema(im.getNamespace());
                if(schema == null) {
                	logValidation
                        (Validator.ResultType.ERROR, im,
                        NbBundle.getMessage(SchemaSemanticsVisitor.class, 
                                            VAL_MISSING_IMPORTED_DOCUMENT, 
                                             im.getNamespace(),
                                             im.getSchemaLocation()),
                        NbBundle.getMessage(SchemaSemanticsVisitor.class, FIX_MISSING_IMPORTED_DOCUMENT)
                        );
                }
            
        }
        
//        for (Schema s : schemas) {
//            mValidation.validate(s.getModel(), ValidationType.COMPLETE);
//        }
    }

    private void visitChildren(SchemaComponent w) {
        Collection coll = w.getChildren();
        if (coll != null) {
            Iterator iter = coll.iterator();
            while (iter.hasNext()) {
                SchemaComponent component = (SchemaComponent) iter.next();
                component.accept(this);
            }
        }
    }
    
    private void logValidation(Validator.ResultType type, 
                                 Component component,  
                                 String desc, 
                                 String correction) {
        String message = desc;
        if (correction != null) {
            message = desc + " : " + correction;
        }
        ResultItem item = new Validator.ResultItem(mValidator, type, component, message);
        mResultItems.add(item);
        
    }
    
    private WSDLSchema findInlineSchema(String namespace) {
    	if(namespace == null) {
            return null;
        }

        WSDLSchema matchingSchema = null;
    	
    	Definitions def = mParentModel.getDefinitions();
        if(def != null) {
        	Types types = def.getTypes();
        	if(types != null) {
        		Collection<WSDLSchema> schemas = types.getExtensibilityElements(WSDLSchema.class);
        		if(schemas != null) {
        			Iterator<WSDLSchema> sIt = schemas.iterator();
        			while(sIt.hasNext()) {
        				WSDLSchema wSchema = sIt.next();
        				SchemaModel sModel = wSchema.getSchemaModel();
        				if(sModel != null && sModel.getSchema() != null) {
        					String targetNamespace = sModel.getSchema().getTargetNamespace();
        					if(namespace.equals(targetNamespace)) {
        						matchingSchema = wSchema;
        						break;
        					}
        				}
        			}
        		}
        	}
        }
        
        return matchingSchema;
    }
}
