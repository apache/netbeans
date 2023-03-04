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
package org.netbeans.modules.xsl.utils;

import java.io.*;
import java.net.*;
import java.beans.PropertyVetoException;

import junit.framework.*;
import org.netbeans.junit.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.openide.filesystems.*;
import org.openide.loaders.*;

import org.netbeans.api.xml.cookies.*;

/**
 *
 * @author Libor Kramolis
 */
public class TransformUtilTest extends NbTestCase {

    public TransformUtilTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TransformUtilTest.class);
        return suite;
    }
    
    
    public void testIsXSLTransformation () throws Exception {
        System.out.println("testIsXSLTransformation");
  
        assertTrue (".xml document must NOT pass!", false==TransformUtil.isXSLTransformation (getDataObject ("doc.xml")));
        assertTrue (".xsl document MUST pass!",            TransformUtil.isXSLTransformation (getDataObject ("doc2xhtml.xsl")));
    }
    
    public void testGetURLName () throws Exception {
        System.out.println("testGetURLName");
        
        FileObject docXML = getFileObject("doc.xml");
        String docXMLName = TransformUtil.getURLName(docXML);
        System.out.println("    docXML: " + docXML + " => '" + docXMLName + "'");
        assertTrue ("URL should not contain nbsf://!",-1==docXMLName.indexOf("nbfs"));
    }

    public void testCreateURL () throws Exception {
        System.out.println("testCreateURL");
        
        URL dataURL = getClass().getResource("data/");
        URL docXMLURL = getClass().getResource("data/doc.xml");
        URL docDTDURL = getClass().getResource("data/doc.dtd");
        
        assertTrue ("Both URLs must be same!",            docXMLURL.sameFile (TransformUtil.createURL (dataURL, "doc.xml")));
        assertTrue ("Both URLs must be same!",            docXMLURL.sameFile (TransformUtil.createURL (docDTDURL, "doc.xml")));
        assertTrue ("Both URLs must be same!",            docXMLURL.sameFile (TransformUtil.createURL (docDTDURL, "../data/doc.xml")));
        assertTrue ("Both URLs must NOT be same!", false==docXMLURL.sameFile (TransformUtil.createURL (docDTDURL, "data/doc.xml")));
        assertTrue ("Both URLs must be same!",     false==docXMLURL.sameFile (TransformUtil.createURL (docDTDURL, docDTDURL.toExternalForm())));
    }

    public void testGetAssociatedStylesheet () throws Exception {
        System.out.println("testGetAssociatedStylesheet");
        
        URL docXMLURL = getClass().getResource("data/doc.xml");
        URL invalidDocXMLURL = getClass().getResource("data/InvalidDocument.xml");
        
        //
        assertTrue ("doc.xml does NOT have associated stylesheet",     null==TransformUtil.getAssociatedStylesheet(docXMLURL));
        
        // FAILS probably because bug in org.apache.xml.utils.URI =>
        //      "org.apache.xml.utils.URI$MalformedURIException: Path contains invalid character: [" if it is nbfs: URL!
        //assertTrue ("InvalidDocument.xml DOES have associated stylesheet", null!=TransformUtil.getAssociatedStylesheet(invalidDocXMLURL));
        
        // Same URL converted to file: format.
        FileObject FO = URLMapper.findFileObjects (invalidDocXMLURL)[0];
        URL url = URLMapper.findURL(FO, URLMapper.EXTERNAL);
        assertTrue ("InvalidDocument.xml DOES have associated stylesheet", null!=TransformUtil.getAssociatedStylesheet (url));
    }
    
    public void testGuessOutputExt () throws Exception {
        System.out.println("testGuessOutputExt");
        
        URL doc2htmlURL = getClass().getResource("data/doc2html.xsl");
        URL doc2textURL = getClass().getResource("data/doc2text.xsl");
        URL doc2xhtmlURL = getClass().getResource("data/doc2xhtml.xsl");
        
        assertTrue ("doc2html.xsl produces HTML output!", "html".equals (TransformUtil.guessOutputExt (getSource (doc2htmlURL))));
        assertTrue ("doc2text.xsl produces TXT output!",  "txt".equals (TransformUtil.guessOutputExt (getSource (doc2textURL))));
        assertTrue ("doc2xhtml.xsl produces XML output!", "xml".equals (TransformUtil.guessOutputExt (getSource (doc2xhtmlURL))));
    }
    
    public void testTransform () throws Exception {
        System.out.println("testTransform");
        
        assertTrue ("Correct XML and correct XSLT must pass!",               transform ("data/doc.xml", "data/doc2xhtml.xsl"));
        assertTrue ("Incorrect XML and correct XSLT must not pass!",  false==transform ("data/InvalidDocument.xml", "data/doc2xhtml.xsl"));
        assertTrue ("Correct XML and incorrect XSLT must not pass!",  false==transform ("data/doc.xml", "data/InvalidDocument.xml"));
        assertTrue ("Incrrect XML and incorrect XSLT must not pass!", false==transform ("data/InvalidDocument.xml", "data/InvalidDocument.xml"));
    }

    private boolean transform (String xml, String xslt) {
        URL xmlURL = getClass().getResource(xml);
        URL xsltURL = getClass().getResource(xslt);
        Source xmlSource = new SAXSource (new InputSource (xmlURL.toExternalForm()));
        Source xsltSource = new SAXSource (new InputSource (xsltURL.toExternalForm()));
        Result outputResult = new StreamResult (new StringWriter());
        
        Observer observer = new Observer(); // not yet used
        boolean exceptionThrown = false;
        try {
            TransformUtil.transform (xmlSource, null, xsltSource, outputResult, observer);
        } catch (TransformerException exc) {
            System.err.println("!!! " + exc);
            exceptionThrown = true;
        }
        
        System.out.println(xml + " & " + xslt + " => " + ( exceptionThrown ? "WRONG" : "OK" ));
        return exceptionThrown==false;
    }
        
    //
    // utils
    //
    
    private FileObject getFileObject (String name) throws PropertyVetoException, IOException {
        URL url = getClass().getResource("data/" + name);
/*        FileSystem FS = getDataFileSystem();
        FileObject FO = FS.findResource (name);        
        return FO;*/
        
        FileObject[] fos = URLMapper.findFileObjects (url);
        return fos[0];
    }
    
    private DataObject getDataObject (String name) throws PropertyVetoException, IOException, DataObjectNotFoundException {
        FileObject FO = getFileObject (name);
        DataObject DO = DataObject.find (FO);
        
        return DO;
    }
   
/*    private FileSystem getDataFileSystem () throws PropertyVetoException, IOException {
        URL dataURL = getClass().getResource("data");
        String dataSysName = dataURL.toExternalForm();
        Repository repository = Repository.getDefault();
        FileSystem dataFS = repository.findFileSystem (dataSysName);
        
        if ( dataFS == null ) {
            LocalFileSystem locFS = new LocalFileSystem();
            locFS.setRootDirectory (new File (dataSysName));
            dataFS = locFS;
        }
        
        return dataFS;
    }*/
    
    private Source getSource (URL url) throws ParserConfigurationException, SAXException {
        XMLReader reader = TransformUtil.newXMLReader();
        reader.setEntityResolver (TransformUtil.getEntityResolver());
        Source source = new SAXSource (reader, new InputSource (url.toExternalForm()));
        return source;
    }

    //
    // class Observer
    //
    
    private static class Observer implements CookieObserver {
        private int receives;
        private int warnings;
        
        public void receive(CookieMessage msg) {
            receives++;
            if (msg.getLevel() >= msg.WARNING_LEVEL) {
                warnings++;
            }
        }
        public int getWarnings() {
            return warnings;
        }        
    } // class Observer
    
}
