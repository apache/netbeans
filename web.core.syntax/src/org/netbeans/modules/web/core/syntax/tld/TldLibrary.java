/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.core.syntax.tld;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.xml.services.UserCatalog;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */

public class TldLibrary extends LibraryDescriptor {

    public static TldLibrary create(FileObject definitionFile) throws LibraryDescriptorException {
        return new TldLibrary(definitionFile);
    }

    static TldLibrary create(InputStream content) throws LibraryDescriptorException {
        return new TldLibrary(content);
    }

    public TldLibrary() {
    }

    private TldLibrary(FileObject definitionFile) throws LibraryDescriptorException {
        super(definitionFile);
        parseLibrary();
    }

    private TldLibrary(InputStream content) throws LibraryDescriptorException {
        super(content);
        parseLibrary(content);
    }

    public  static String parseNamespace(InputStream content) {
        return parseNamespace(content, "taglib", "uri"); //NOI18N
    }

    protected void parseLibrary(InputStream content) throws LibraryDescriptorException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource(content); //default encoding?!?!
            is.setEncoding("UTF-8");
            docBuilder.setEntityResolver(UserCatalog.getDefault().getEntityResolver()); //we count on TaglibCatalog from web.core module
            Document doc = docBuilder.parse(is);

            //usually the default taglib prefix
            Node tagLib = getNodeByName(doc, "taglib"); //NOI18N
 
            prefix = getTextContent(tagLib, "short-name"); //NOI18N
            if(prefix == null) {
                //no default prefix
                prefix = "";
            }

            uri = getTextContent(tagLib, "uri"); //NOI18N
            if(uri == null) {
                throw new LibraryDescriptorException("Missing uri entry in " + getDefinitionFile().getPath() + " library.", null);
            }

            displayName = getTextContent(tagLib, "display-name"); //NOI18N
//            if(displayName == null) {
//                //no display-name specified in the TLD, lets try to get the displayname from names registry
//                displayName = DefaultFaceletLibraries.getLibraryDisplayName(uri);
//                if(displayName == null) {
//                    //no entry even here, use TLD file name
//                    displayName = getDefinitionFile().getNameExt();
//                }
//            }

            //scan the <tag> nodes content - the tag descriptions
            NodeList tagNodes = doc.getElementsByTagName("tag"); //NOI18N
            if (tagNodes != null) {
                for (int i = 0; i < tagNodes.getLength(); i++) {
                    Node tag = tagNodes.item(i);
                    String tagName = getTextContent(tag, "name"); //NOI18N
                    String tagDescription = getTextContent(tag, "description"); //NOI18N

                    Map<String, Attribute> attrs = new HashMap<String, Attribute>();
                    //find attributes
                    for(Node attrNode : getNodesByName(tag, "attribute")) { //NOI18N
                        String aName = getTextContent(attrNode, "name"); //NOI18N
                        String aDescription = getTextContent(attrNode, "description"); //NOI18N
                        boolean aRequired = Boolean.parseBoolean(getTextContent(attrNode, "required")); //NOI18N

                        attrs.put(aName, new Attribute(aName, aDescription, aRequired));
                    }

                    tags.put(tagName, new TagImpl(tagName, tagDescription, attrs));

                }
            }

            //scan the <function> nodes content - the tag descriptions
            NodeList functionNodes = doc.getElementsByTagName("function"); //NOI18N
            if (functionNodes != null) {
                for (int i = 0; i < functionNodes.getLength(); i++) {
                    Node function = functionNodes.item(i);
                    String funcName = getTextContent(function, "name"); //NOI18N
                    String funcSignature = getTextContent(function, "function-signature"); //NOI18N
                    String funcDescription = getTextContent(function, "description"); //NOI18N
                    String funcExample = getTextContent(function, "example"); //NOI18N

                    functions.put(funcName, new FunctionImpl(funcName, funcSignature, funcDescription, funcExample));
                }
            }

        } catch (ParserConfigurationException ex) {
            throw new LibraryDescriptorException("Error parsing TLD library: ", ex); //NOI18N
        } catch (SAXException ex) {
            throw new LibraryDescriptorException("Error parsing TLD library: ", ex); //NOI18N
        } catch (IOException ex) {
            throw new LibraryDescriptorException("Error parsing TLD library: ", ex); //NOI18N
        }


    }

}
