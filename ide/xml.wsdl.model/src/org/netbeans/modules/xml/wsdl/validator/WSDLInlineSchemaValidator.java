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
package org.netbeans.modules.xml.wsdl.validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.Validator.class)
public class WSDLInlineSchemaValidator extends XsdBasedValidator {
    
    @SuppressWarnings("unchecked")
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);

    public String getName() {
       return "WSDLInlineSchemaValidator";//NOI18N
    }

    @Override
    public ValidationResult validate(Model model, Validation validation,
            ValidationType validationType) {
        if (model instanceof WSDLModel) {
            WSDLModel wsdlModel = (WSDLModel) model;

            List<Model> validatedModels = new ArrayList<Model>();
            Vector<ResultItem> resultItems = new Vector<ResultItem>();
            if (validationType.equals(ValidationType.COMPLETE) ||
                    validationType.equals(ValidationType.PARTIAL)) {
                
                if (wsdlModel.getState() == State.NOT_WELL_FORMED) {
                    return EMPTY_RESULT;
                }
                
                Definitions def = wsdlModel.getDefinitions();
                Map<String, String> prefixes = ((AbstractDocumentComponent)def).getPrefixes();
                String systemId = getSystemId(wsdlModel);
                Types types = def.getTypes();
                if (types != null) {
                    Collection<WSDLSchema> schemas = types.getExtensibilityElements(WSDLSchema.class);
                    
                    String text = getWSDLText(wsdlModel);
                    
                    assert text != null : "there is no content in the wsdl document or couldnt be read";
                    
                    List<Integer> linePositions = setupLinePositions(text); 
                    
                    WSDLLSResourceResolver resolver = new WSDLLSResourceResolver(wsdlModel, 
                                                                                 systemId, 
                                                                                 text, 
                                                                                 prefixes, 
                                                                                 linePositions);
           
                    for (WSDLSchema schema : schemas) {
                        Reader in = createInlineSchemaSource(text, prefixes, linePositions, schema);
                        if(in != null) {
                            SAXSource source = new SAXSource(new InputSource(in));
                            source.setSystemId(systemId);
                            int start = schema.findPosition();
                            int lineNumber = getLineNumber(start, linePositions); //where the schema starts in the wsdl document
          
                            //validate the source
                            Handler handler = new InlineSchemaValidatorHandler(wsdlModel, lineNumber);
                            validate(wsdlModel, source, handler, resolver);
                            resultItems.addAll(handler.getResultItems());
                        }
                    }
                    
                    
                    validatedModels.add(model);
                    return new ValidationResult(resultItems, validatedModels);
                }
                return EMPTY_RESULT;
            }
        }
        return null;
    }
    
    private String getSystemId(WSDLModel model) {
        // # 170429
        File file = model.getModelSource().getLookup().lookup(File.class);
         
        if (file != null) {
            return file.toURI().toString();
        }
        Source source = model.getModelSource().getLookup().lookup(Source.class);

        if (source != null) {
            return source.getSystemId();
        }
        return null;
    }

    private void validate(WSDLModel model, 
                          Source saxSource, 
                          XsdBasedValidator.Handler handler,
                          LSResourceResolver resolver) {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            if (resolver != null) {
                sf.setResourceResolver(resolver);
            }
            sf.setErrorHandler(handler);

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
    
    /**
     * {<one or more space}{anyNonSpace + schema}{zero or more space}
     * it can be <xsd:schema> or <xsd:schema blah-blah>
     */
    Pattern pattern = Pattern.compile("(<\\s*)(\\S*schema)(\\s*)");
    
    private String getEndTag(String startTag) {
        Matcher matcher = pattern.matcher(startTag);
        if (matcher.find()) {
            return "</" + matcher.group(2) + ">"; //get the schema with prefix and create a end tag
        }
        return null;
    }
    
    private List<Integer> setupLinePositions(String str) {
        List<Integer> linePositions = new ArrayList<Integer>();
        String[] lines = str.split("\n"); //NOI18N
        linePositions.add(Integer.valueOf(-1));
        int pos = 0;
        for (String line : lines) {
            linePositions.add(pos);
            pos += line.length() + 1; // make sure we also count the \n
        }
        return linePositions;
    }
    
    private int getLineNumber(int position, List<Integer> linePositions) {
        for (int i = 0; i < linePositions.size(); i++) {
            if (position < linePositions.get(i).intValue()) {
                return i - 1;
            }
        }
        return -1;
        
    }

    class InlineSchemaValidatorHandler extends XsdBasedValidator.Handler {
        int startingLineNumber;
        public InlineSchemaValidatorHandler(Model model, int lineNumber) {
            super(model);
            startingLineNumber = lineNumber - 1;
        }

        @Override
        public void logValidationErrors(ResultType resultType, String errorDescription, int lineNumber, int columnNumber) {
            super.logValidationErrors(resultType, errorDescription, startingLineNumber + lineNumber,
                    columnNumber);
        }
    }

    @Override
    protected Schema getSchema(Model model) {
        //not used.
        return null;
    }
    
    private String getWSDLText(WSDLModel model) {
        javax.swing.text.Document d = model.getModelSource().getLookup().lookup(Document.class);
        try {
            return d.getText(0, d.getLength());
        } catch (BadLocationException e) {
            //log this..
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "getWSDLText", e); //NOI18N
        }
        return null;
    }
    
    @Override
    public DocumentModel resolveResource(String systemId, Model currentModel) {
        if (currentModel instanceof WSDLModel && systemId.equals(getSystemId((WSDLModel)currentModel))) {
            return (WSDLModel) currentModel;
        }
        try {
            CatalogModel cm = currentModel.getModelSource().getLookup()
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
 
    private Reader createInlineSchemaSource(String wsdlText,
                                            Map<String, String> wsdlPrefixes,
                                            List<Integer> wsdlLinePositions,
                                            WSDLSchema oneInlineSchema) {
        Reader source = null;
        int start = oneInlineSchema.findPosition();
        int lineNumber = getLineNumber(start, wsdlLinePositions); //where the schema starts in the wsdl document
        String schemaString = oneInlineSchema.getContentFragment(); // get inner text content of schema.

        if (wsdlText == null || schemaString == null) {
          return null;
        }
        int index = wsdlText.indexOf(schemaString, start);
        if (schemaString != null && schemaString.trim().length() > 0) { //else if its schema with no contents
            assert index != -1 : "the text content under schema couldnt be found in the wsdl document";
            String schemaTop = wsdlText.substring(start, index); //get the schema definition.
            String[] splits = schemaTop.split(">");
            StringBuffer strBuf = new StringBuffer();
            if (splits.length > 0) {
                strBuf.append(splits[0]);
                Map<String, String> schemaPrefixes = ((AbstractDocumentComponent)oneInlineSchema.getSchemaModel().getSchema()).getPrefixes();
                for (String prefix : wsdlPrefixes.keySet()) {
                    if (!(prefix == null || prefix.length() == 0 || schemaPrefixes.containsKey(prefix))) {
                        strBuf.append(" ").append("xmlns:").append(prefix).append("=\"").append(wsdlPrefixes.get(prefix)).append("\"");
                    }
                }

                for (int i = 1; i < splits.length; i++) {
                    strBuf.append(">").append(splits[i]);
                }
                strBuf.append(">");
                strBuf.append(schemaString).append(getEndTag(splits[0]));
                schemaString = null;
                splits = null;
                schemaTop = null;
            }
            source = new StringReader(strBuf.toString());
//            source = new SAXSource(new InputSource(new StringReader(strBuf.toString())));
            strBuf = null;
        }
        
        return source;
    }
                        
    class WSDLLSResourceResolver implements LSResourceResolver {
        
        private WSDLModel mModel;
        private LSResourceResolver mDelegate;
        private String mWsdlSystemId;
        private String mWsdlText;
        private Map<String, String> mWsdlPrefixes;
        private List<Integer> mWsdlLinePositions;
                
        public WSDLLSResourceResolver(WSDLModel wsdlmodel,
                                      String wsdlSystemId,
                                      String wsdlText,
                                      Map<String, String> wsdlPrefixes,
                                      List<Integer> wsdlLinePositions) {
             mModel = wsdlmodel;
             mWsdlSystemId = wsdlSystemId;
             mWsdlText  = wsdlText;   
             mWsdlPrefixes = wsdlPrefixes;
             mWsdlLinePositions = wsdlLinePositions;

             mDelegate = CatalogModelFactory.getDefault().getLSResourceResolver();
           
        }

        public LSInput resolveResource(String type, 
                                       String namespaceURI, 
                                       String publicId, 
                                       String systemId, 
                                       String baseURI) {
            
             LSInput lsi = mDelegate.resolveResource(type, namespaceURI, publicId, systemId, baseURI);

             if (lsi == null) {
                 //if we can not get an input from catalog, then it could that
                 //there as a schema in types section which refer to schema compoment from other inline schema
                 //so we try to check in other inline schema.
                 WSDLSchema schema = findSchema(namespaceURI);
                 if(schema != null) {
                     Reader in = createInlineSchemaSource(mWsdlText, mWsdlPrefixes, mWsdlLinePositions, schema);
                     if(in != null) {
                         //create LSInput object
                         DOMImplementation domImpl = null;
                         try {
                             domImpl =  DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
                         } catch (ParserConfigurationException ex) {
                             Logger.getLogger(getClass().getName()).log(Level.SEVERE, "resolveResource", ex); //NOI18N
                             return null;
                         }

                         DOMImplementationLS dols = (DOMImplementationLS) domImpl.getFeature("LS","3.0");
                         lsi = dols.createLSInput();
                         lsi.setCharacterStream(in);

                         if(mWsdlSystemId != null) {
                             lsi.setSystemId(mWsdlSystemId);
                         }
                         return lsi;  
                     }
                 }
             }
             return lsi;                      
        }

        private WSDLSchema findSchema(String namespaceURI) {
                WSDLSchema schema = null;
                Definitions def = mModel.getDefinitions(); 
                Types types = def.getTypes();
                if (types != null) {
                    Collection<WSDLSchema> schemas = types.getExtensibilityElements(WSDLSchema.class);
                    Iterator<WSDLSchema> it = schemas.iterator();
                    while(it.hasNext()) {
                        WSDLSchema sch = it.next();
                        SchemaModel model = sch.getSchemaModel();
                        if(model != null) {
                            org.netbeans.modules.xml.schema.model.Schema s = model.getSchema();
                            if(s != null && s.getTargetNamespace() != null && s.getTargetNamespace().equals(namespaceURI)) {
                                schema = sch;
                                break;
                            }
                        }
                    }
                }
                return schema;
        }
    }
}
