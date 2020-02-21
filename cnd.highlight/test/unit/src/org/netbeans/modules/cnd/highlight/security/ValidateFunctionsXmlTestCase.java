/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
