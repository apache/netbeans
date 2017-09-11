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

package org.netbeans.modules.dbschema.migration.archiver.deserializer;

import java.lang.*;
import javax.xml.parsers.*;

import org.xml.sax.helpers.*;
import org.xml.sax.*;

public  class BaseXMLDeserializer extends java.lang.Object
    implements XMLDeserializer,  org.xml.sax.DocumentHandler, org.xml.sax.DTDHandler, org.xml.sax.ErrorHandler
{

    // Fields
    protected  java.lang.Object InitialObject;
    protected  org.xml.sax.Parser Parser;
    public org.xml.sax.Locator TheLocator;
    protected  org.xml.sax.InputSource TheSource;
    public java.lang.StringBuffer TheCharacters;

    // Constructors
    public  BaseXMLDeserializer()
    {
        this.TheCharacters = new StringBuffer();
    } /*Constructor-End*/

    // Methods
    public   void notationDecl(String name, String publicId, String systemId) throws org.xml.sax.SAXException
    {
        
    } /*Method-END*/
    public   void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws org.xml.sax.SAXException
    {
        
    } /*Method-End*/
    public   void processingInstruction(String target, String data) throws org.xml.sax.SAXException
    {
        
    } /*Method-Enc*/
    public   void setDocumentLocator(org.xml.sax.Locator locator)
    {
        this.TheLocator = locator;
    } /*Method-End*/
    public   void ignorableWhitespace(char[] ch, int start, int length) throws org.xml.sax.SAXException
    {
        
    } /*Method-End*/
    public   void endElement(java.lang.String name) throws org.xml.sax.SAXException
    {
        this.TheCharacters.delete(0,this.TheCharacters.length());
    } /*Method-End*/

    public   void endDocument()    throws org.xml.sax.SAXException
    {
        this.freeResources();
    } /*Method-End*/
    public   void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException
    {
        this.TheCharacters.append(ch,start,length);
    } /*Method-End*/

    public   void startElement(java.lang.String name, org.xml.sax.AttributeList atts) throws org.xml.sax.SAXException
    {
        this.TheCharacters.delete(0,this.TheCharacters.length());
    } /*Method-End*/

    public   void startDocument() throws org.xml.sax.SAXException
    {
        this.freeResources();
    } /*Method-End*/

    public   void fatalError(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException
    {
        this.commonErrorProcessor(exception);
    } /*Method-End*/

    public   void warning(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException
    {
        
    } /*Method-End*/

    public   void error(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException
    {
        this.commonErrorProcessor(exception);
    } /*Method-End*/

    public   void setInitialObject(java.lang.Object obj)
    {
        this.InitialObject = obj;
    } /*Method-End*/

    public   void freeResources()
    {
        this.TheCharacters.delete(0,this.TheCharacters.length());
    } /*Method-End*/

    public  java.lang.String  getCharacters()
    {
        // trim escaped newline character and newline characters
        
        int lOffset = 0;
                
        while (lOffset < this.TheCharacters.length())
        {
            if (lOffset + 2 < this.TheCharacters.length() &&
                this.TheCharacters.substring(lOffset, lOffset + 2).equals("\\n"))
            {
               this.TheCharacters.delete(lOffset, lOffset + 2);    
            }
            else if (this.TheCharacters.charAt(lOffset) == '\n')
            {
               this.TheCharacters.deleteCharAt(lOffset);
            }
            lOffset++;
        }
             
        return this.TheCharacters.toString();
    } /*Method-End*/

    public  int  Begin() throws org.xml.sax.SAXException {
        try {
            if (this.Parser == null) {
                org.xml.sax.Parser parser;
                SAXParserFactory factory;

                factory = SAXParserFactory.newInstance();
                factory.setValidating(false); // asi validate=false
                factory.setNamespaceAware(false);

                this.Parser = factory.newSAXParser().getParser();
                
                this.Parser.setDocumentHandler(this);
                this.Parser.setDTDHandler(this);
                this.Parser.setErrorHandler(this);
            }
        } catch (ParserConfigurationException e1) {
            SAXException classError = new SAXException(e1.getMessage());
            throw classError;
        }
        
        return 1;
    } /*Method-End*/

    public   void commonErrorProcessor(org.xml.sax.SAXParseException error) throws org.xml.sax.SAXException
    {        
        throw(error);
    } /*Method-End*/

    public  java.lang.Object  XlateObject() throws org.xml.sax.SAXException, java.io.IOException
    {
        this.Begin();
        
        this.Parser.parse(this.TheSource);
        
        return InitialObject;
    } /*Method-End*/

    public   void setSource(org.xml.sax.InputSource source)
    {
        this.TheSource = source;
    } /*Method-End*/

    public  java.lang.Object  XlateObject(java.io.InputStream stream) throws org.xml.sax.SAXException, java.io.IOException
    {
        this.Begin();
        
        InputSource is = new InputSource(stream);
        is.setSystemId("archiverNoID");
        this.setSource(is);
        
        this.Parser.parse(this.TheSource);
        
        return InitialObject;
    } /*Method-End*/
    
    public void DumpStatus()
    {
        // This method is a debug method to dump status information about this object
        
        System.out.println("Dump Status from class BaseXMLSerializer");
        System.out.println("The initial object is an instance of " + this.InitialObject.getClass().getName());
        System.out.println("The initial object state " + this.InitialObject.toString());
        System.out.println("The current stream dump is " + this.TheCharacters);
        System.out.println("Dump Status from class BaseXMLSerializer - END");
        
    }
    
    
}  // end of class
