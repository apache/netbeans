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
package org.netbeans.modules.xml.schema.model.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.openide.util.NbBundle;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 *
 * @author Nam Nguyen
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.Validator.class)
public class SchemaXsdBasedValidator extends XsdBasedValidator {
    
    private static Schema schema;
    
    protected Schema getSchema(Model model) {
        if (! (model instanceof SchemaModel)) {
            return null;
        }
        
        // This will not be used as validate(.....) method is being overridden here.
        // So just return a schema returned by newSchema().
        if(schema == null) {
            try {
                schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema();
            } catch(SAXException ex) {
                assert false: "Error while creating compiled schema for"; //NOI18N
            }
        }
        return schema;
    }
    
    public String getName() {
        return NbBundle.getMessage(SchemaXsdBasedValidator.class, "LBL_Schema_Validator");
    }
    
    @Override
    protected void validate(Model model, Schema schema, XsdBasedValidator.Handler handler) {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            CatalogModel cm = (CatalogModel) model.getModelSource().getLookup()
		.lookup(CatalogModel.class);
	    if (cm != null) {
                sf.setResourceResolver(cm);
            }
            sf.setErrorHandler(handler);
            Source saxSource = getSource(model, handler);
            if (saxSource == null) {
                return;
            }
            sf.newSchema(saxSource);
        } catch(SAXException sax) {
            //already processed by handler
        } catch(Exception ex) {
            handler.logValidationErrors(Validator.ResultType.ERROR, ex.getMessage());
        }
    }
    
    public DocumentModel resolveResource(String systemId, Model currentModel) {
        
        try {
            CatalogModel cm = (CatalogModel) currentModel.getModelSource().getLookup()
            .lookup(CatalogModel.class);
            ModelSource ms = cm.getModelSource(new URI(systemId));
            if (ms != null) {
                return SchemaModelFactory.getDefault().getModel(ms);
            }
        } catch(URISyntaxException ex) {
            Logger.getLogger(getClass().getName()).log(Level.FINE, "resolveResource", ex); //NOI18N
        } catch(CatalogModelException ex) {
            Logger.getLogger(getClass().getName()).log(Level.FINE, "resolveResource", ex); //NOI18N
        }
        return null;
    }
    
}
