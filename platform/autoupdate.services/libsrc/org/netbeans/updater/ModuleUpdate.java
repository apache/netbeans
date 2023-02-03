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

package org.netbeans.updater;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.jar.*;
import java.util.jar.Attributes;
import java.util.logging.Level;

import org.w3c.dom.*;
import org.xml.sax.*;

/** This class represents one module update available on the web
 *
 * @author  Ales Kemr
 * @version
 */
class ModuleUpdate extends Object {

    // Constants
    private static final String ATTR_CODENAMEBASE = "codenamebase"; // NOI18N

    /** Holds value of property codenamebase. */
    private String codenamebase = null;
    /** Holds value of sv */
    private String specification_version = null;
    
    private boolean pError = false;

    private boolean l10n = false;
    
    /** Creates new ModuleUpdate for downloaded .nbm or .jar file */
    ModuleUpdate (File file) {
        if(file.getName().endsWith(ModuleUpdater.JAR_EXTENSION)) {
            createFromOSGiDistribution( file );
        } else {
            createFromNbmDistribution( file );
        }
    }

    /** Creates module from downloaded OSGi .jar file */
    private void createFromOSGiDistribution( File file ) {
        JarFile jf = null;
        boolean exit = false;
        String errorMessage = null;
        try {
            jf = new JarFile(file);
            String cnb = extractCodeName(jf.getManifest().getMainAttributes());
            if(cnb!=null) {
                setCodenamebase(cnb);
            }
            String specVersion = jf.getManifest().getMainAttributes().getValue("Bundle-Version");
            setSpecification_version(specVersion!=null ?
                specVersion.replaceFirst("^(\\d+([.]\\d+([.]\\d+)?)?)([.].+)?$", "$1") :
                "0");


        } catch ( java.io.IOException e ) {
            errorMessage = "Missing info : " + file.getAbsolutePath (); // NOI18N
            XMLUtil.LOG.log(Level.WARNING, errorMessage, e);
            exit = true;
        }
        finally {
            try {
                if (jf != null)
                    jf.close();
            } catch ( IOException ioe ) {
                XMLUtil.LOG.log(Level.WARNING, "Cannot close " + jf, ioe);
                exit = true;
            }
        }

        if (exit) {
            throw new RuntimeException (errorMessage);
        }
    }

    
    // copied from nbbuild.JarWithModuleAttributes.extractCodeName()
    static String extractCodeName(Attributes attr) {
        String codename = attr.getValue("OpenIDE-Module");
        if (codename != null) {
            return codename;
        }
        codename = attr.getValue("Bundle-SymbolicName");
        if (codename == null) {
            return null;
        }
        codename = codename.replace('-', '_');
        int params = codename.indexOf(';');
        if (params >= 0) {
            return codename.substring(0, params);
        } else {
            return codename;
        }
    }
    

    /** Creates module from downloaded .nbm file */
    private void createFromNbmDistribution( File nbmFile ) {

        Document document = null;
        Node node = null;
        Element documentElement = null;
        
        // Try to parse the info file
        JarFile jf = null;
        InputStream is = null;
        boolean exit = false;
        String errorMessage = null;
        try {
            jf = new JarFile(nbmFile);
            is = jf.getInputStream(jf.getEntry("Info/info.xml"));  // NOI18N
            
            InputSource xmlInputSource = new InputSource( is );
            document = XMLUtil.parse( xmlInputSource, false, false, new ErrorCatcher(), XMLUtil.createAUResolver() );
            
            documentElement = document.getDocumentElement();
            node = documentElement;
            if (is != null)
                is.close();
        }
        catch ( org.xml.sax.SAXException e ) {
            errorMessage = "Bad info : " + nbmFile.getAbsolutePath (); // NOI18N
            XMLUtil.LOG.log(Level.WARNING, errorMessage, e);
            exit = true;
        }            
        catch ( java.io.IOException e ) {
            errorMessage = "Missing info : " + nbmFile.getAbsolutePath (); // NOI18N
            XMLUtil.LOG.log(Level.WARNING, errorMessage, e);
            exit = true;
        }
        finally {
            try {
                if (is != null)
                    is.close();
                if (jf != null)
                    jf.close();
            } catch ( IOException ioe ) {
                XMLUtil.LOG.log(Level.WARNING, "Cannot close " + jf, ioe);
                exit = true;
            }
        }
        
        if (exit) {
            throw new RuntimeException (errorMessage);
        }

        setCodenamebase( getAttribute( node, ATTR_CODENAMEBASE ) );
        NodeList nodeList = ((Element)node).getElementsByTagName( "l10n" ); // NOI18N

        if ( nodeList.getLength() > 0 ) {
            l10n = true;
            Node n = nodeList.item( 0 );            
            setSpecification_version( getAttribute( n, "module_spec_version" ) );
        } else {
            nodeList = ((Element)node).getElementsByTagName( "manifest" ); // NOI18N

            for( int i = 0; i < nodeList.getLength(); i++ ) {

                if ( nodeList.item( i ).getNodeType() != Node.ELEMENT_NODE ||
                        !( nodeList.item( i ) instanceof Element ) ) {
                    break;
                }

                // ((Element)nodeList.item( i )).normalize();
                NamedNodeMap attrList = nodeList.item( i ).getAttributes();            
                for( int j = 0; j < attrList.getLength(); j++ ) {
                    Attr attr = (Attr) attrList.item( j );
                    if (attr.getName().equals("OpenIDE-Module"))  // NOI18N
                        setCodenamebase(attr.getValue());
                    else if (attr.getName().equals("OpenIDE-Module-Specification-Version"))  // NOI18N
                        setSpecification_version(attr.getValue());
                }
            }
        }
    }
    
    private String getAttribute(Node n, String attribute) {
        Node attr = n.getAttributes().getNamedItem( attribute );
        return attr == null ? null : attr.getNodeValue();
    }

    /** Getter for property codeNameBase.
     *@return Value of property codeNameBase.
     */
    String getCodenamebase() {
        return codenamebase;
    }

    /** Setter for property Codenamebase.
     *@param manufacturer New value of property Codenamebase.
     */
    void setCodenamebase(String codenamebase) {
        this.codenamebase = codenamebase;
    }

    /** Getter for property specification_version.
     *@return Value of property specification_version.
     */
    String getSpecification_version() {
        return specification_version;
    }

    /** Setter for property specification_version.
     *@param notification New value of property specification_version.
     */
    void setSpecification_version(String specification_version) {        
        this.specification_version = specification_version;        
    }
    
    /** Getter for property l10n.
     * @return Value of property l10n.
     *
     */
    public boolean isL10n() {
        return l10n;
    }
    
    class ErrorCatcher implements org.xml.sax.ErrorHandler {


        @Override
        public void error (org.xml.sax.SAXParseException e) {
            // normally a validity error
            pError = true;
        }

        @Override
        public void warning (org.xml.sax.SAXParseException e) {
            //parseFailed = true;
        }

        @Override
        public void fatalError (org.xml.sax.SAXParseException e) {
            pError = true;
        }
    } //end of inner class ErrorPrinter
    
}
