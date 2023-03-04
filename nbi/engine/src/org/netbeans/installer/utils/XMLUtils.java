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

package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.installer.product.dependencies.Conflict;
import org.netbeans.installer.product.dependencies.InstallAfter;
import org.netbeans.installer.product.dependencies.Requirement;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ExtendedUri;
import org.netbeans.installer.utils.helper.Feature;
import org.netbeans.installer.utils.helper.NbiProperties;
import org.netbeans.installer.utils.helper.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class XMLUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static void saveXMLDocument(
            final Document document,
            final File file) throws XMLException {
        LogManager.logEntry("saving document to xml file : " + file);
        FileOutputStream output = null;
        
        for (int i = 0; i < MAXIMUM_SAVE_ATTEMPTS; i++) {
            try {                
                output = new FileOutputStream(file);                
                saveXMLDocument(document, output);                
                // check the size of the resulting file -- sometimes it just happens
                // to be empty, we need to resave then; if we fail to save for
                // several times (MAXIMUM_SAVE_ATTEMPTS) -- fail with an
                // XMLException
            } catch (IOException e) {
                LogManager.log("... can`t save XML, an exception caught", e);
                LogManager.logExit("... document not saved");
                throw new XMLException("Cannot save XML document", e);
            } finally {
                if (output != null) {
                    try {
                        output.close();
                        output = null;
                    } catch (IOException e) {
                        ErrorManager.notifyDebug("Could not close the stream", e);
                    }
                }
            }
            if (file.length() > 0) {
                LogManager.logExit("... document saved");
                return;
            }            
        }
        LogManager.logExit("... throwing XML exception since xml file could not be saved for several attemps");
        throw new XMLException("Cannot save XML document after " +
                MAXIMUM_SAVE_ATTEMPTS + " attempts, the resulting " +
                "file is empty.");
        
    }
    
    public static void saveXMLDocument(
            final Document document,
            final OutputStream output) throws XMLException {
        try {
	    final Source source = new DOMSource(
                    document);
	    final Result result = new StreamResult(
                    output);
	    final Source xslt = new StreamSource(
                    FileProxy.getInstance().getFile(XSLT_REFORMAT_URI,true));
	    TransformerFactory tf = TransformerFactory.
                    newInstance();
            final Transformer transformer = tf.
                    newTransformer(xslt);
            transformer.transform(source, result);	    
        } catch (DownloadException e) {
            throw new XMLException("Cannot save XML document", e);
        } catch (TransformerConfigurationException e) {
            throw new XMLException("Cannot save XML document", e);
        } catch (TransformerException e) {
            throw new XMLException("Cannot save XML document", e);
        } 
    }
    
    public static Document loadXMLDocument(
            final File file) throws XMLException {
        FileInputStream input = null;
        
        try {
            return loadXMLDocument(input = new FileInputStream(file));
        } catch (IOException e) {
            throw new XMLException("Cannot open XML file", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    ErrorManager.notifyDebug("Cannot close the stream", e);
                }
            }
        }
    }
    
    public static Document loadXMLDocument(
            final InputStream input) throws XMLException {
        try {
            return DocumentBuilderFactory.
                    newInstance().
                    newDocumentBuilder().
                    parse(input);
        } catch (ParserConfigurationException e) {
            throw new XMLException("Cannot parse XML", e);
        } catch (SAXException e) {
            throw new XMLException("Cannot parse XML", e);
        } catch (IOException e) {
            throw new XMLException("Cannot parse XML", e);
        }
    }
    
    public static Element getDocumentElement(
            final File file) throws XMLException {
        return loadXMLDocument(file).getDocumentElement();
    }
    
    public static Element getDocumentElement(
            final InputStream input) throws XMLException {
        return loadXMLDocument(input).getDocumentElement();
    }
    
    public static List<Element> getChildren(
            final Element element) {
        final List<Element> children = new LinkedList<Element>();
        
        final NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            
            if (node instanceof Element) {
                children.add((Element) node);
            }
        }
        
        return children;
    }
    
    public static List<Element> getChildren(
            final Element element,
            final String... names) {
        final List<Element> children = new LinkedList<Element>();
        
        final NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            
            if (node instanceof Element) {
                for (int j = 0; j < names.length; j++) {
                    if (node.getNodeName().equals(names[j])) {
                        children.add((Element) node);
                        break;
                    }
                }
            }
        }
        
        return children;
    }
    
    public static Element getChild(
            final Element element,
            final String path) {
        final String[] pathComponents;
        
        if (path.contains(StringUtils.FORWARD_SLASH)) {
            pathComponents = path.split(StringUtils.FORWARD_SLASH);
        } else {
            pathComponents = new String[] {
                path
            };
        }
        
        Element child = element;
        
        for (String name: pathComponents) {
            boolean found = false;
            
            for (Element subchild: getChildren(child)) {
                if (subchild.getNodeName().equals(name)) {
                    child = subchild;
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                child = null;
                break;
            }
        }
        
        return child;
    }
    
    public static int countDescendants(
            final Element element,
            final String... names) {
        return countDescendants(element, Arrays.asList(names));
    }
    
    public static int countDescendants(
            final Element element,
            final List<String> names) {
        int count = 0;
        for (Element child: getChildren(element)) {
            if (names.contains(child.getNodeName())) {
                count++;
            }
            
            count += countDescendants(child, names);
        }
        
        return count;
    }
    
    public static Element appendChild(
            final Element element,
            final String name,
            final String text) {
        final Element child = element.getOwnerDocument().createElement(name);
        
        child.setTextContent(text != null ? text : StringUtils.EMPTY_STRING);
        element.appendChild(child);
        
        return child;
    }
    
    // object <-> dom ///////////////////////////////////////////////////////////////
    public static Dependency parseDependency(
            final Element element) throws ParseException {
        final String type = element.getNodeName();
        final String uid =
                element.getAttribute("uid");
        final Version lower =
                Version.getVersion(element.getAttribute("version-lower"));
        final Version upper =
                Version.getVersion(element.getAttribute("version-upper"));
        final Version resolved =
                Version.getVersion(element.getAttribute("version-resolved"));
        Dependency dependency = null;
        if(type.equals(Requirement.NAME)) {
            List <List <Requirement>> orList = new ArrayList<List<Requirement>> ();
            for(Element orElement : getChildren(element, "or")) {
                List < Requirement> requirements = new ArrayList <Requirement> ();
                for(Element el : getChildren(orElement)) {
                    Dependency dep = parseDependency(el);
                    if(dep instanceof Requirement) {
                        requirements.add((Requirement)dep);
                    } else {
                        throw new ParseException(
                                "OR dependencies are not supported for " +
                                dep.getName());
                    }
                }
                orList.add(requirements);
            }
            dependency = new Requirement(uid, lower, upper, resolved, orList);
        } else if(type.equals(Conflict.NAME)) {
            dependency = new Conflict(uid, lower, upper, resolved);
        } else if(type.equals(InstallAfter.NAME)) {
            dependency = new InstallAfter(uid, lower, upper, resolved);
        } else {
            throw new ParseException("Unknown dependency : " + type);
        }
        return dependency;
    }
    
    public static Element saveDependency(
            final Dependency dependency,
            final Element element) {
        element.setAttribute("uid",
                dependency.getUid());
        
        if (dependency.getVersionLower() != null) {
            element.setAttribute("version-lower",
                    dependency.getVersionLower().toString());
        }
        if (dependency.getVersionUpper() != null) {
            element.setAttribute("version-upper",
                    dependency.getVersionUpper().toString());
        }
        if (dependency.getVersionResolved() != null) {
            element.setAttribute("version-resolved",
                    dependency.getVersionResolved().toString());
        }
        if(dependency instanceof Requirement) {
            Requirement requirement = (Requirement)dependency;
            List <List <Requirement>> orList = requirement.getAlternatives();
            for(List <Requirement> requirememntsBlock : orList) {
                element.appendChild(
                        saveDependencies(requirememntsBlock,
                        element.getOwnerDocument().createElement("or")));
            }
        }
        
        return element;
    }
    
    public static List<Dependency> parseDependencies(
            final Element element) throws ParseException {
        final List<Dependency> dependencies = new LinkedList<Dependency>();
        
        for (Element child: getChildren(element)) {
            dependencies.add(parseDependency(child));
        }
        
        return dependencies;
    }
    
    public static Element saveDependencies(
            final List<? extends Dependency> dependencies,
            final Element element) {
        final Document document = element.getOwnerDocument();
        
        for (Dependency dependency: dependencies) {
            element.appendChild(saveDependency(
                    dependency,
                    document.createElement(dependency.getName())));
        }
        
        return element;
    }
    
    public static Properties parseProperties(
            final Element element) throws ParseException {
        final Properties properties = new Properties();
        
        if (element != null) {
            for (Element child: XMLUtils.getChildren(element, "property")) {
                String name = child.getAttribute("name");
                String value = child.getTextContent();
                
                properties.setProperty(name, value);
            }
        }
        
        return properties;
    }
    
    public static Element saveProperties(
            final Properties properties,
            final Element element) {
        final Document document = element.getOwnerDocument();

        properties.forEach((name, textObj) -> {
            final Element propertyElement = document.createElement("property");
            
            propertyElement.setAttribute("name", name.toString());
            propertyElement.setTextContent(textObj.toString());
            
            element.appendChild(propertyElement);
        });
        
        return element;
    }
    
    public static NbiProperties parseNbiProperties(
            final Element element) throws ParseException {
        return new NbiProperties(parseProperties(element));
    }
    
    public static Element saveNbiProperties(
            final NbiProperties properties,
            final Element element) {
        return saveProperties(properties, element);
    }
    
    public static ExtendedUri parseExtendedUri(
            final Element element) throws ParseException {
        try {
            final URI uri =
                    new URI(getChild(element, "default-uri").getTextContent());
            final long size =
                    Long.parseLong(element.getAttribute("size"));
            final String md5 =
                    element.getAttribute("md5");
            
            final List<URI> alternates = new LinkedList<URI>();
            
            for (Element alternateElement: XMLUtils.getChildren(
                    element, "alternate-uri")) {
                alternates.add(new URI(alternateElement.getTextContent()));
            }
            
            if (uri.getScheme().equals("file")) {
                return new ExtendedUri(uri, alternates, uri, size, md5);
            } else {
                return new ExtendedUri(uri, alternates, size, md5);
            }
        } catch (URISyntaxException e) {
            throw new ParseException("Cannot parse extended URI", e);
        } catch (NumberFormatException e) {
            throw new ParseException("Cannot parse extended URI", e);
        }
    }
    
    public static Element saveExtendedUri(
            final ExtendedUri uri,
            final Element element) {
        final Document document = element.getOwnerDocument();
        
        element.setAttribute("size", Long.toString(uri.getSize()));
        element.setAttribute("md5", uri.getMd5());
        
        // the default uri would be either "local" (if it's present) or the
        // "remote" one
        Element uriElement = document.createElement("default-uri");
        if (uri.getLocal() != null) {
            uriElement.setTextContent(uri.getLocal().toString());
        } else {
            uriElement.setTextContent(uri.getRemote().toString());
        }
        element.appendChild(uriElement);
        
        // if the "local" uri is not null, we should save the "remote" uri as the
        // first alternate, unless it's the same as the local
        if ((uri.getLocal() != null) && !uri.getRemote().equals(uri.getLocal())) {
            uriElement = document.createElement("alternate-uri");
            
            uriElement.setTextContent(uri.getRemote().toString());
            element.appendChild(uriElement);
        }
        
        for (URI alternateUri: uri.getAlternates()) {
            if (!alternateUri.equals(uri.getRemote())) {
                uriElement = document.createElement("alternate-uri");
                
                uriElement.setTextContent(alternateUri.toString());
                element.appendChild(uriElement);
            }
        }
        
        return element;
    }
    
    public static List<ExtendedUri> parseExtendedUrisList(
            final Element element) throws ParseException {
        final List<ExtendedUri> uris = new LinkedList<ExtendedUri>();
        
        for (Element uriElement: getChildren(element)) {
            uris.add(parseExtendedUri(uriElement));
        }
        
        return uris;
    }
    
    public static Element saveExtendedUrisList(
            final List<ExtendedUri> uris,
            final Element element) {
        final Document document = element.getOwnerDocument();
        
        for (ExtendedUri uri: uris) {
            element.appendChild(
                    saveExtendedUri(uri, document.createElement("file")));
        }
        
        return element;
    }
    
    public static Map<Locale, String> parseLocalizedString(
            final Element element) throws ParseException {
        final Map<Locale, String> map = new HashMap<Locale, String>();
        
        final Element defaultElement = getChild(element, "default");
        map.put(new Locale(StringUtils.EMPTY_STRING), StringUtils.parseAscii(
                defaultElement.getTextContent()));
        
        for (Element localizedElement: getChildren(element, "localized")) {
            final Locale locale = StringUtils.parseLocale(
                    localizedElement.getAttribute("locale"));
            final String localizedString = StringUtils.parseAscii(
                    localizedElement.getTextContent());
            
            map.put(locale, localizedString);
        }
        
        return map;
    }
    
    public static Element saveLocalizedString(
            final Map<Locale, String> map,
            final Element element) {
        final Document document = element.getOwnerDocument();

        String s = map.get(new Locale(StringUtils.EMPTY_STRING));

        final Element defaultElement = document.createElement("default");
        defaultElement.setTextContent(StringUtils.convertToAscii(s));
        element.appendChild(defaultElement);

        for (Map.Entry<Locale, String> entry : map.entrySet()) {
            if (!entry.getValue().equals(s)) {
                final Element localizedElement = document.createElement("localized");

                localizedElement.setAttribute("locale", entry.getKey().toString());
                localizedElement.setTextContent(StringUtils.convertToAscii(entry.getValue()));

                element.appendChild(localizedElement);
            }
        }
        
        return element;
    }
    
    public static Feature parseFeature(
            final Element element) throws ParseException {
        final String id = element.getAttribute("id");
        final long offset = Long.parseLong(element.getAttribute("offset"));
        final ExtendedUri iconUri = parseExtendedUri(getChild(element, "icon"));
        
        final Map<Locale, String> displayNames =
                parseLocalizedString(getChild(element, "display-name"));
        final Map<Locale, String> descriptions =
                parseLocalizedString(getChild(element, "description"));
        
        return new Feature(id, offset, iconUri, displayNames, descriptions);
    }
    
    public static Element saveFeature(
            final Feature feature,
            final Element element) {
        final Document document = element.getOwnerDocument();
        
        element.setAttribute("id", feature.getId());
        element.setAttribute("offset", Long.toString(feature.getOffset()));
        
        element.appendChild(saveExtendedUri(
                feature.getIconUri(), document.createElement("icon")));
        element.appendChild(saveLocalizedString(
                feature.getDisplayNames(), document.createElement("display-name")));
        element.appendChild(saveLocalizedString(
                feature.getDescriptions(), document.createElement("description")));
        
        return element;
    }
    
    public static List<Feature> parseFeaturesList(
            final Element element) throws ParseException {
        final List<Feature> features = new LinkedList<Feature>();
        
        for (Element featureElement: XMLUtils.getChildren(element)) {
            features.add(XMLUtils.parseFeature(featureElement));
        }
        
        return features;
    }
    
    public static Element saveFeaturesList(
            final List<Feature> features,
            final Element element) {
        final Document document = element.getOwnerDocument();
        
        for (Feature feature: features) {
            element.appendChild(
                    saveFeature(feature, document.createElement("feature")));
        }
        
        return element;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String XSLT_REFORMAT_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/utils/xml/reformat.xslt"; // NOI18N
    
    private static final int MAXIMUM_SAVE_ATTEMPTS =
            3;
}
