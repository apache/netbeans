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
package org.netbeans.modules.cnd.highlight.security;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 */
public class ValidateFunctionsXmlTestCase extends NbTestCase {
    
    public ValidateFunctionsXmlTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected int timeOut() {
        return 500000;
    }
    
    public void testSchema() throws Exception {
        FileObject folder = FileUtil.getConfigFile(FunctionsXmlService.ROOT_FOLDER+"/"+FunctionsXmlService.CHECKS_FOLDER);
        if (folder != null && folder.isFolder()) {
            FileObject[] files = folder.getChildren();
            for (FileObject file : files) {
                parse(file);
            }
        }
    }
    
    private void parse(final FileObject file) throws Exception {
        InputSource source = new InputSource(file.getInputStream());
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new StreamSource(ValidateFunctionsXmlTestCase.class.getResourceAsStream("/org/netbeans/modules/cnd/highlight/security/unreliablefunctions.xsd")));
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        factory.setSchema(schema);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException exception) throws SAXException {
                System.err.println(file.getNameExt()+":"+exception.getLineNumber()+":"+exception.getColumnNumber());
                throw exception;
            }
            public void error(SAXParseException exception) throws SAXException {
                if ("Document is invalid: no grammar found.".equals(exception.getMessage())) {
                    return;
                } else if ("Document root element \"unreliablefunctions\", must match DOCTYPE root \"null\".".equals(exception.getMessage())) {
                    return;
                }
                System.err.println(file.getNameExt()+":"+exception.getLineNumber()+":"+exception.getColumnNumber());
                throw exception;
            }
            public void fatalError(SAXParseException exception) throws SAXException {
                System.err.println(file.getNameExt()+":"+exception.getLineNumber()+":"+exception.getColumnNumber());
                throw exception;
            }
        });
        builder.parse(source);
    }
    
}
