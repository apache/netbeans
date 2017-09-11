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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.common.taginfo;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class LibraryMetadata {
    private String id;
    private Map<String, TagMetadata> tagMap = new TreeMap<String, TagMetadata>();

    public LibraryMetadata(String id, Collection<TagMetadata> tags) {
        this.id = id;

        for (TagMetadata tag : tags){
            tagMap.put(tag.getName(), tag);
        }
    }

    public TagMetadata getTag(String tagName){
        return tagMap.get(tagName);
    }

    public String getId() {
        return id;
    }

    public static LibraryMetadata readFromXML(InputStream inputStream) throws Exception {
        Collection<TagAttrMetadata> commonAttrs = new ArrayList<TagAttrMetadata>();
        Collection<TagMetadata> tags = new ArrayList<TagMetadata>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList commonAttrLst = doc.getElementsByTagName("common_attributes");

        for (int i = 0; i < commonAttrLst.getLength(); i++) {
            Node tagNode = commonAttrLst.item(i);

            if (tagNode.getNodeType() == Node.ELEMENT_NODE){
                Element elem = (Element) tagNode;
                readFromXML_parseTagAttributes(elem, commonAttrs);
            }
        }

        NodeList tagLst = doc.getElementsByTagName("tag"); //NOI18N
        String tagLibId = doc.getDocumentElement().getAttribute("id"); //NOI18N

        for (int i = 0; i < tagLst.getLength(); i++) {
            Node tagNode = tagLst.item(i);

            if (tagNode.getNodeType() == Node.ELEMENT_NODE){
                Element elem = (Element) tagNode;
                String tagName = elem.getAttribute("name"); //NOI18N
                Collection<TagAttrMetadata> attrs = new ArrayList<TagAttrMetadata>(commonAttrs);
                readFromXML_parseTagAttributes(elem, attrs);

                tags.add(new TagMetadata(tagName, attrs));
            }
        }

        return new LibraryMetadata(tagLibId, tags);
    }

    private static void readFromXML_parseTagAttributes(Element tagNode, Collection<TagAttrMetadata> result){
        NodeList attrLst = tagNode.getElementsByTagName("attr"); //NOI18N

        for (int i = 0; i < attrLst.getLength(); i++) {
            Node attrNode = attrLst.item(i);

            if (attrNode.getNodeType() == Node.ELEMENT_NODE){
                Element elem = (Element) attrNode;
                String attrName = elem.getAttribute("name"); //NOI18N

                String mimeType = parseMimeType(elem);
                Collection<AttrValueType> valueTypes = readFromXML_parseValueTypes(elem);

                result.add(new TagAttrMetadata(attrName, valueTypes, mimeType));
            }
        }
    }

    private static String parseMimeType(Element attrNode){
        NodeList mimeTypeLst = attrNode.getElementsByTagName("mimetype"); //NOI18N

        if (mimeTypeLst.getLength() > 0){
            Node firstNode = mimeTypeLst.item(0);
            return firstNode.getTextContent();
        }

        return null;
    }

    private static Collection<AttrValueType> readFromXML_parseValueTypes(Element attrNode){
        Collection<AttrValueType> result = new ArrayList<AttrValueType>();
        NodeList typeLst = attrNode.getElementsByTagName("type"); //NOI18N

        for (int i = 0; i < typeLst.getLength(); i++) {
            Node typeNode = typeLst.item(i);

            if (typeNode.getNodeType() == Node.ELEMENT_NODE){
                Element elem = (Element) typeNode;
                String typeName = elem.getAttribute("name"); //NOI18N

                AttrValueType valueType = null;

                if ("boolean".equals(typeName)){
                    valueType = AttrValueType.BOOL;
                } else {
                    String legalVals[] = readFromXML_parseAttrValues(elem);
                    valueType = new AttrValueType(typeName, legalVals);
                }

                result.add(valueType);
            }
        }

        return result;
    }

    private static String[] readFromXML_parseAttrValues(Element typeNode){
        Collection<String> result = new ArrayList<String>();
        NodeList typeLst = typeNode.getElementsByTagName("val"); //NOI18N

        for (int i = 0; i < typeLst.getLength(); i++) {
            Node valNode = typeLst.item(i);

            if (valNode.getNodeType() == Node.ELEMENT_NODE){
                Element elem = (Element) valNode;

                String val = elem.getTextContent();

                if (val != null){
                    result.add(val);
                }
            }
        }

        return result.toArray(new String[result.size()]);
    }
}
