/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.wsdl.validator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.Validator.class)
public class WSDLSchemaValidator extends XsdBasedValidator {
    
    /*
     * Uses the WSDL Basic Profile 1.1 schema from 
     * http://www.ws-i.org/Profiles/BasicProfile-1.1.html#WSDLDOCSTRUCT
     */
    static final String wsdlXSDUrl = "/org/netbeans/modules/xml/wsdl/validator/resources/external/wsdl-2004-08-24.xsd";
    
    public String getName() {
        return "WSDLSchemaValidator"; //NO I18N
    }
    
    @Override
    protected Schema getSchema(Model model) {
        if (! (model instanceof WSDLModel)) {
            return null;
        }
        
        InputStream wsdlSchemaInputStream = WSDLSchemaValidator.class.getResourceAsStream(wsdlXSDUrl);
        Source wsdlSource = new StreamSource(wsdlSchemaInputStream);
        wsdlSource.setSystemId(WSDLSchemaValidator.class.getResource(wsdlXSDUrl).toString());
        
        //combine all possible schemas through ElementFactoryProvider mechanism
        Collection<ValidatorSchemaFactory> extSchemaFactories = ValidatorSchemaFactoryRegistry.getDefault().getAllValidatorSchemaFactories();
        
        ArrayList<Source> isList = new ArrayList<Source>();
        isList.add(wsdlSource);
        for (ValidatorSchemaFactory factory : extSchemaFactories) {
            Source is = factory.getSchemaSource();
            if(is != null) {
                isList.add(is);
            } else {
                //any validator should not return a null input stream
                Logger.getLogger(getClass().getName()).severe("getSchema: " + factory.getClass() +" returned null input stream for its schema");
            }
        }

        Schema schema = getCompiledSchema(isList.toArray(new Source[0]), new CentralLSResourceResolver(extSchemaFactories), new SchemaErrorHandler());
        
        return schema;
    }

    class SchemaErrorHandler implements ErrorHandler {
    	public void error(SAXParseException exception) throws SAXException {
    		Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SchemaErrorHandler: " + exception.getMessage(), exception);
    	}
    	
    	public void fatalError(SAXParseException exception) throws SAXException {
    		Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SchemaErrorHandler: " + exception.getMessage(), exception);
    	}
    	
    	public void warning(SAXParseException exception) throws SAXException {
    		
    	}
    }
    
    class CentralLSResourceResolver implements LSResourceResolver {
        
        private Collection<ValidatorSchemaFactory> mExtSchemaFactories;
                
        CentralLSResourceResolver(Collection<ValidatorSchemaFactory> extSchemaFactories) {
            mExtSchemaFactories = extSchemaFactories;
        }
        
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            LSInput input = null;
            
            Iterator<ValidatorSchemaFactory> it = mExtSchemaFactories.iterator();
            while(it.hasNext()) {
                ValidatorSchemaFactory fac = it.next();
                LSResourceResolver resolver = fac.getLSResourceResolver();
                if(resolver != null) {
                    input = resolver.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
                    if(input != null) {
                       break;
                    }
                }
            }
            
            return input;
        }
        
    }
}
