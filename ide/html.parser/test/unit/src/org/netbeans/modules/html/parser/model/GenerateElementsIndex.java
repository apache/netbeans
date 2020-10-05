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
package org.netbeans.modules.html.parser.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import nu.validator.htmlparser.sax.HtmlParser;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;
import org.netbeans.junit.NbTestCase;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Not a real unit test, just for simple generation
 * of html element's descriptions.
 *
 * @author marekfukala
 */
public class GenerateElementsIndex extends NbTestCase {

    //parsed from http://www.whatwg.org/specs/web-apps/current-work/#global-attributes
    public static final String GLOBAL_ATTRIBUTES_BASE_URL = "http://www.whatwg.org/specs/web-apps/current-work/multipage/";
    public static final String[] GLOBAL = new String[]{
        "accesskey", "interaction.html#the-accesskey-attribute",
        "class", "dom.html#classes",
        "contenteditable", "interaction.html#attr-contenteditable",
        "contextmenu", "forms.html#attr-contextmenu",
        "dir", "dom.html#the-dir-attribute",
        "draggable", "interaction.html#the-draggable-attribute",
        "dropzone", "interaction.html#the-dropzone-attribute",
        "hidden", "interaction.html#the-hidden-attribute",
        "id", "dom.html#the-id-attribute",
        "itemid", "microdata.html#attr-itemid",
        "itemprop", "microdata.html#names:-the-itemprop-attribute",
        "itemref", "microdata.html#attr-itemref",
        "itemscope", "microdata.html#attr-itemscope",
        "itemtype", "microdata.html#attr-itemtype",
        "lang", "dom.html#attr-lang",
        "spellcheck", "interaction.html#attr-spellcheck",
        "style", "dom.html#the-style-attribute",
        "tabindex", "interaction.html#attr-tabindex",
        "title", "dom.html#attr-title",
        "translate", "dom.html#attr-translate"
    };
    public static final String EVENT_ATTRIBUTES_BASE_URL = "http://www.whatwg.org/specs/web-apps/current-work/multipage/webappapis.html#handler-";
    public static final String[] GLOBAL_EVENT = new String[]{
        "onabort", "webappapis.html#handler-onabort",
        "onautocomplete", "webappapis.html#handler-onautocomplete",
        "onautocompleteerror", "webappapis.html#handler-onautocompleteerror",
        "onafterprint", "webappapis.html#handler-window-onafterprint",
        "onbeforeprint", "webappapis.html#handler-window-onbeforeprint",
        "onbeforeunload", "webappapis.html#handler-window-onbeforeunload",
        "onblur", "webappapis.html#handler-onblur",
        "oncancel", "webappapis.html#handler-oncancel",
        "oncanplay", "webappapis.html#handler-oncanplay",
        "oncanplaythrough", "webappapis.html#handler-oncanplaythrough",
        "onchange", "webappapis.html#handler-onchange",
        "onclick", "webappapis.html#handler-onclick",
        "onclose", "webappapis.html#handler-onclose",
        "oncontextmenu", "webappapis.html#handler-oncontextmenu",
        "oncuechange", "webappapis.html#handler-oncuechange",
        "ondblclick", "webappapis.html#handler-ondblclick",
        "ondrag", "webappapis.html#handler-ondrag",
        "ondragend", "webappapis.html#handler-ondragend",
        "ondragenter", "webappapis.html#handler-ondragenter",
        "ondragexit", "webappapis.html#handler-ondragexit",
        "ondragleave", "webappapis.html#handler-ondragleave",
        "ondragover", "webappapis.html#handler-ondragover",
        "ondragstart", "webappapis.html#handler-ondragstart",
        "ondrop", "webappapis.html#handler-ondrop",
        "ondurationchange", "webappapis.html#handler-ondurationchange",
        "onemptied", "webappapis.html#handler-onemptied",
        "onended", "webappapis.html#handler-onended",
        "onerror", "webappapis.html#handler-onerror",
        "onfocus", "webappapis.html#handler-onfocus",
        "onhashchange", "webappapis.html#handler-window-onhashchange",
        "oninput", "webappapis.html#handler-oninput",
        "oninvalid", "webappapis.html#handler-oninvalid",
        "onkeydown", "webappapis.html#handler-onkeydown",
        "onkeypress", "webappapis.html#handler-onkeypress",
        "onkeyup", "webappapis.html#handler-onkeyup",
        "onlanguagechange", "webappapis.html#handler-window-onlanguagechange",
        "onload", "webappapis.html#handler-onload",
        "onloadeddata", "webappapis.html#handler-onloadeddata",
        "onloadedmetadata", "webappapis.html#handler-onloadedmetadata",
        "onmessage", "webappapis.html#handler-window-onmessage",
        "onloadstart", "webappapis.html#handler-onloadstart",
        "onmousedown", "webappapis.html#handler-onmousedown",
        "onmouseenter", "webappapis.html#handler-onmouseenter",
        "onmousemove", "webappapis.html#handler-onmousemove",
        "onmouseout", "webappapis.html#handler-onmouseout",
        "onmouseover", "webappapis.html#handler-onmouseover",
        "onmouseup", "webappapis.html#handler-onmouseup",
        "onmousewheel", "webappapis.html#handler-onmousewheel",
        "onoffline", "webappapis.html#handler-window-onoffline",
        "ononline", "webappapis.html#handler-window-ononline",
        "onpagehide", "webappapis.html#handler-window-onpagehide",
        "onpageshow", "webappapis.html#handler-window-onpageshow",
        "onpause", "webappapis.html#handler-onpause",
        "onplay", "webappapis.html#handler-onplay",
        "onplaying", "webappapis.html#handler-onplaying",
        "onpopstate", "webappapis.html#handler-window-onpopstate",
        "onprogress", "webappapis.html#handler-onprogress",
        "onratechange", "webappapis.html#handler-onratechange",
        "onreset", "webappapis.html#handler-onreset",
        "onresize", "webappapis.html#handler-onresize",
        "onscroll", "webappapis.html#handler-onscroll",
        "onseeked", "webappapis.html#handler-onseeked",
        "onseeking", "webappapis.html#handler-onseeking",
        "onselect", "webappapis.html#handler-onselect",
        "onshow", "webappapis.html#handler-onshow",
        "onstalled", "webappapis.html#handler-onstalled",
        "onstorage", "webappapis.html#handler-window-onstorage",
        "onsubmit", "webappapis.html#handler-onsubmit",
        "onsuspend", "webappapis.html#handler-onsuspend",
        "ontimeupdate", "webappapis.html#handler-ontimeupdate",
        "ontoggle", "webappapis.html#handler-ontoggle",
        "onunload", "webappapis.html#handler-window-onunload",
        "onvolumechange", "webappapis.html#handler-onvolumechange",
        "onwaiting", "webappapis.html#handler-onwaiting"
    };
    
    
    private static final String WHATWG_SPEC_HTML5_ELEMENTS_INDEX_URL = Constants.HTML5_MULTIPAGE_SPEC_BASE_URL + "indices.html#index";
    private static final String WHATWG_SPEC_HTML5_NAMED_REFS_INDEX_URL = Constants.HTML5_MULTIPAGE_SPEC_BASE_URL + "named-character-references.html";

    private boolean parse = true;
    private boolean intbody, intr, inth_or_td, ina, incode;
    private int column;
    private String href;
    private String attr_code;
    private Collection<Element> elements;
    private final Collection<NamedCharRef> refs = new LinkedList<NamedCharRef>();
    private NamedCharRef currentRef;
    private Stack<Element> currents = new Stack<Element>();
    private String LINK_URL_BASE; //if the spec contains full urls its empty
    private StringBuilder textBuffer;

    public GenerateElementsIndex(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
//        suite.addTest(new GenerateElementsIndex("test_GenerateNamedCharacterReferencesIndex"));

//        suite.addTest(new GenerateElementsIndex("test_GenerateSVGAndMATHMLElementsIndex"));
//        suite.addTest(new GenerateElementsIndex("test_GenerateElementsIndex"));
        suite.addTest(new GenerateElementsIndex("test_GenerateGlobalAndEventAttributesEnumMembers"));
//        suite.addTest(new GenerateElementsIndex("test_GenerateAttributesIndex"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    private void parseSpecification() throws URISyntaxException, MalformedURLException, IOException, SAXException {
        if(elements != null) {
            return ; //already parsed
        }
        
        elements = new LinkedList<Element>();
        InputStream spec = getSpec();
        assertNotNull(spec);

        HtmlParser parser = new HtmlParser();
        parser.setContentHandler(new ContentHandlerAdapter() {

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                handleWholeText();
                element(localName, false, null);
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                handleWholeText();
                element(localName, true, atts);
            }

            private void element(String localName, boolean open, Attributes atts) throws SAXException {
                if (!parse) {
                    return; //parsing already stopped
                }
                if (localName.equals("tbody")) {
                    intbody = open;
                    if (!intbody) {
                        parse = false;
                    }
                } else if (localName.equals("tr")) {
                    if (intbody) {
                        intr = open;
                        if (open) {
                            column = -1;
                        } else {
                            //h1-h6 support (more elements in the element name cell)
                            Element filledElement = currents.pop();
                            for (Element e : currents) {
                                Element copy = filledElement.shallowCopy();
                                copy.name = e.name;
                                elements.add(copy);
                            }
                            elements.add(filledElement);

                            currents.clear();
                        }
                    }
                } else if (localName.equals("th") || localName.equals("td")) {
                    if (intr) {
                        inth_or_td = open;
                        if (open) {
                            column++;
                        }
                    }
                } else if (localName.equals("a")) {
                    if (inth_or_td) {
                        ina = open;
                        if (open) {
                            //get link target
                            href = atts.getValue("href");
                        } else {
                            href = null;
                        }
                    }
                } else if(localName.equals("code")) {
                    if(inth_or_td) {
                        attr_code = atts != null ? atts.getValue("id") : null;
                        if (attr_code != null) {
                            if (attr_code.contains("elements-3")) {
                                attr_code = attr_code.substring("elements-3:".length());
                            }
                            if (attr_code.length() > 2 && attr_code.charAt(attr_code.length()-2) == '-' && Character.isDigit(attr_code.charAt(attr_code.length()-1))) {
                                attr_code = attr_code.substring(0, attr_code.length()-2);
                            }
                        }
                        
                    } else {
                        attr_code = null;
                    }
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if(textBuffer == null) {
                    textBuffer = new StringBuilder();
                }
                textBuffer.append(ch, start, length);
            }

           private void handleWholeText() {
                if(textBuffer == null) {
                    return ;
                }
                String text = textBuffer.toString();
                textBuffer = null;

                if(column >= 2 && column <=5) {
                    //metadata content fix:
                    //the ContentType.METADATA name
                    //needs to be matched for "metadata content" text
                    //in the link. It seems to be the only such link
                    //in the spec's index.7
                    text = text.trim();
                    int wsIndex = text.indexOf(' ');
                    if(wsIndex != -1) {
                        System.out.println(String.format("Whitespace in '%s' link name at the column %d. Trimming...", text, column));
                        text = text.substring(0, wsIndex);
                    }
                }

                switch (column) {
                    case 0:
                        //name
                        if (ina) {
                            currents.push(new Element());
                            currents.peek().name = new LLink(text, href);
                        }
                        break;
                    case 1:
                        //description
                        currents.peek().descriptionBuilder.append(text);
                        break;
                    case 2:
                        //categories
                        if (ina) {
                            currents.peek().categories.add(new LLink(text, href));
                        }
                        break;
                    case 3:
                        //parents
                        if (ina) {
                            currents.peek().parents.add(new LLink(text, href));
                        }
                        break;
                    case 4:
                        //children
                        if (ina) {
                            currents.peek().children.add(new LLink(text, href));
                        }
                        break;
                    case 5:
                        //attributes
                        if (ina) {
                            currents.peek().attributes.add(new LLink(attr_code, href));
                        }
                        break;
                    case 6:
                        //interface
                        if (ina) {
                            currents.peek().interfacee = new LLink(text, href);
                        }
                        break;
                    default:
                        assert false;

                }
            }

        });

        InputSource input = new InputSource(spec);
        parser.parse(input);

        System.out.println("Found " + elements.size() + " elements:");

    }

    public  void test_GenerateNamedCharacterReferencesIndex() throws URISyntaxException, MalformedURLException, IOException, SAXException {
        InputStream spec = getNamedCharacterReferencesSpec();
        assertNotNull(spec);

        HtmlParser parser = new HtmlParser();
        parser.setContentHandler(new ContentHandlerAdapter() {

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                handleWholeText();
                element(localName, false, null);
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                handleWholeText();
                element(localName, true, atts);
            }

            private void element(String localName, boolean open, Attributes atts) throws SAXException {
                if (!parse) {
                    return; //parsing already stopped
                }
                if (localName.equals("tbody")) {
                    intbody = open;
                    if (!intbody) {
                        parse = false;
                    }
                } else if (localName.equals("tr")) {
                    if (intbody) {
                        intr = open;
                        if (open) {
                            column = -1;
                        } else {
                            refs.add(currentRef);
                            currentRef = null;
                        }
                    }
                } else if (localName.equals("th") || localName.equals("td")) {
                    if (intr) {
                        inth_or_td = open;
                        if (open) {
                            column++;
                        }
                    }
                } else if(localName.equals("code")) {
                    if(inth_or_td) {
                        incode = open;
                    } 
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if(textBuffer == null) {
                    textBuffer = new StringBuilder();
                }
                textBuffer.append(ch, start, length);
            }

             private void handleWholeText() {
                if(textBuffer == null) {
                    return ;
                }
                String text = textBuffer.toString().trim();
                if(text.length() == 0) {
                    return ; //ignore ws
                }

                textBuffer = null;

                switch (column) {
                    case 0:
                        //name 
                        if (incode) {
                            currentRef = new NamedCharRef();
                            currentRef.name = text;
                        }
                        break;
                    case 1:
                        //code
                        currentRef.code = text;
                        break;
                    case 2:
                        break;
                    default:
                        assert false;

                }
            }

            
        });

        InputSource input = new InputSource(spec);
        parser.parse(input);

        System.out.println("Found " + refs.size() + " named char refs");

        Writer out = new StringWriter();
        for(NamedCharRef ref : refs) {
            if(ref.name.endsWith(";")) {
                String name = ref.name.substring(0, ref.name.length() -1);
                String enumName = name;
                if(enumName.equals("int")) {
                    enumName = "INT";
                }
                out.write(enumName);
                out.write("(\"");
                out.write(name);
                out.write("\",0x");
                out.write(ref.code.substring(2));
                out.write("),\n");
            }
           
        }

        System.out.println(out);

    }

    //generates a list of global and event attributes - members of the Attribute enum
    public void test_GenerateGlobalAndEventAttributesEnumMembers() throws IOException, URISyntaxException, MalformedURLException, SAXException {
        parseSpecification();
        Writer out = new StringWriter();
        out.write("//global attributes\n");
        for (int i = 0; i < GLOBAL.length; i++) {
            String name = GLOBAL[i];
            String link = GLOBAL[++i];

            String fullLink = GLOBAL_ATTRIBUTES_BASE_URL + link;

            out.write(Attribute.attributeId2EnumName(name));
            out.write("(new Link(\"");
            out.write(name);
            out.write("\", \"");
            out.write(link);
            out.write("\")),\n");

        }
        out.write("\n//event attributes\n");
        for (int i = 0; i < GLOBAL_EVENT.length; i++) {
            String name = GLOBAL_EVENT[i];
            String link = GLOBAL_EVENT[++i];
            String fullLink = GLOBAL_ATTRIBUTES_BASE_URL + link;

            out.write(Attribute.attributeId2EnumName(name));
            out.write("(new Link(\"");
            out.write(name);
            out.write("\", \"");
            out.write(link);
            out.write("\")),\n");

        }
        System.out.println(out);
    }

    public void test_GenerateAttributesIndex() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
        //generate the part of the Attributes enum class for elements specific attributes
        parseSpecification();
        Writer out = new StringWriter();
        out.write("\n//properietary attributes\n");
        
        //gather all possible attributes
        Collection<Link> attrs = new HashSet<Link>();
        for (Element e : elements) {
           attrs.addAll(e.attributes);
        }

        for(Link l : attrs) {
            String attrId = l.getName();
            if(attrId == null) {
                //not valid link, happens for the "global" attributes link
                continue;
            }

            out.write(Attribute.attributeId2EnumName(attrId));
            out.write("(new Link(\"");
            out.write(attrId);
            out.write("\", \"");
            out.write(l.getLink());
            out.write("\")),\n");
        }

        System.out.println(out);
    }

    public void test_GenerateElementsIndex() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
        parseSpecification();
        //generate the ElementDescriptor enum class
        Writer out = new StringWriter();
        Map<String, Element> elementsMap = new HashMap<String, Element>();
        for (Element e : elements) {
            elementsMap.put(e.name.getName(), e);
        }

        for (Element e : elements) {
            out.write(e.name.getName().toUpperCase());
            out.write("(\n");
            out.write("\tHtmlTagType.HTML,\n");
            out.write("\tnew Link(\"");
            out.write(e.name.getName());
            out.write("\", \"");

            //relativize the absolute link
            String link = e.name.getLink();
            out.write(link);
            out.write("\"),\n\t \"");
            out.write(e.getDescription());
            out.write("\", ");

            out.write("\n\t");

            //categories - content type of form associated element category
            writeContentTypes(e.categories, out);
            out.write("\n\t");
            writeFormAssociatedElementsCategoryTypes(e.categories, out);
            out.write("\n\t");

            //parents - content type or element
            writeContentTypes(e.parents, out);
            out.write("\n\t");
            writeElements(e.parents, elementsMap, out);
            out.write("\n\t");

            //children - content type or element
            writeContentTypes(e.children, out);
            out.write("\n\t");
            writeElements(e.children, elementsMap, out);
            out.write("\n\t");

            //attributes
            writeAttributes(e.attributes, out);
            out.write("\n\t");

            //dom interface
            out.write("new Link(\"");
            out.write(e.interfacee.getName());
            out.write("\", \"");
            //relativize the absolute link
            link = e.interfacee.getLink();
            out.write(link);
            out.write("\")");

            out.write("\n), \n\n");
        }


        System.out.println(out);
    }

    //TODO this is very simple version only, just names and categories, other metadata need to be parsed from the specs
    public void test_GenerateSVGAndMATHMLElementsIndex() throws IOException {
        Writer out = new StringWriter();
        generateSimpleElementDescriptors(out, ElementDescriptorRules.MATHML_TAG_NAMES, "MATHML");
        generateSimpleElementDescriptors(out, ElementDescriptorRules.SVG_TAG_NAMES, "SVG");

        System.out.println(out);
    }

    
    public void generateSimpleElementDescriptors(Writer out, Collection<String> tagNames, String type) throws IOException {
        out.write("\n//" + type + " elements:\n//-----------------------\n\n");
        for(String tagName : tagNames) {
            out.write(ElementDescriptor.elementName2EnumName(tagName));
            out.write("(\n");
            out.write("\tHtmlTagType." + type + ",\n");
            out.write("\tnew Link(\"");
            out.write(tagName);
            out.write("\", null),\n");
            out.write("\tnull,\n");
            out.write("\tEnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),\n");
            out.write("\tEnumSet.noneOf(FormAssociatedElementsCategory.class),\n");
            out.write("\tEnumSet.noneOf(ContentType.class),\n");
            out.write("\tnew String[]{},\n");
            out.write("\tEnumSet.noneOf(ContentType.class),\n");
            out.write("\tnew String[]{},\n");
            out.write("\tEnumSet.noneOf(Attribute.class),\n");
            out.write("\tnull\n");
            out.write("),\n");
        }
    }

    private void writeAttributes(Collection<? extends Link> links, Writer out) throws IOException {
        Collection<Attribute> attrs = new ArrayList<Attribute>();

        for (Iterator<? extends Link> i = links.iterator(); i.hasNext();) {
            Link cat = i.next();

            if(cat.getName() == null) {
                //not valid attribute link, skip
                continue;
            }

            //Attribute
            try {
                Attribute attr = Attribute.valueOf(Attribute.attributeId2EnumName(cat.getName()));
                attrs.add(attr);
            } catch (IllegalArgumentException ex) {
                //the element doesn't represent content type
            }
        }

        writeEnumCollection(attrs, "Attribute", out);
    }

    private void writeElements(Collection<Link> links, Map<String, Element> elementsMap, Writer out) throws IOException {
        Collection<Element> els = new ArrayList<Element>();

        for (Iterator<Link> i = links.iterator(); i.hasNext();) {
            Link cat = i.next();

            Element el = elementsMap.get(cat.getName());
            if (el != null) {
                els.add(el);
            }

        }

        writeElementsCollection(els, out);
    }

    private void writeElementsCollection(Collection<Element> elements, Writer out) throws IOException {
        out.write("new String[]{");
        for (Iterator<Element> i = elements.iterator(); i.hasNext();) {
            Element e = i.next();
            out.write("\"");
            out.write(e.name.getName());
            out.write("\"");
            if (i.hasNext()) {
                out.write(", ");
            }
        }
        out.write("}, ");
    }

    private void writeContentTypes(Collection<Link> links, Writer out) throws IOException {
        Collection<ContentType> ctypes = new ArrayList<ContentType>();

        for (Iterator<Link> i = links.iterator(); i.hasNext();) {
            Link cat = i.next();
            String nameInUpperCase = cat.getName().toUpperCase();

            //ContentType
            try {
                ContentType contentType = ContentType.valueOf(nameInUpperCase);
                ctypes.add(contentType);
            } catch (IllegalArgumentException ex) {
                //the element doesn't represent content type
            }

        }

        writeEnumCollection(ctypes, "ContentType", out);
    }

    private void writeFormAssociatedElementsCategoryTypes(Collection<Link> links, Writer out) throws IOException {
        Collection<FormAssociatedElementsCategory> fasecs = new ArrayList<FormAssociatedElementsCategory>();

        for (Iterator<Link> i = links.iterator(); i.hasNext();) {
            Link cat = i.next();
            String nameInUpperCase = cat.getName().toUpperCase();

            try {
                FormAssociatedElementsCategory fasec = FormAssociatedElementsCategory.valueOf(nameInUpperCase);
                fasecs.add(fasec);
            } catch (IllegalArgumentException ex) {
                //the element doesn't represent FormAssociatedElementsCategory
            }

        }

        writeEnumCollection(fasecs, "FormAssociatedElementsCategory", out);
    }

    private void writeEnumCollection(Collection<? extends Enum<?>> enumCollection, String enumName, Writer out) throws IOException {
        if (enumCollection.isEmpty()) {
            out.write("EnumSet.noneOf(");
            out.write(enumName);
            out.write(".class), ");
        } else {
            out.write("EnumSet.of(");
            for (Iterator<? extends Enum<?>> i = enumCollection.iterator(); i.hasNext();) {
                Enum ct = i.next();
                out.write(enumName);
                out.write(".");
                out.write(ct.name());
                if (i.hasNext()) {
                    out.write(", ");
                }
            }
            out.write("), ");
        }
    }

    //1. try to freshest spec from whatwg.org
    //2. if unavailable, use local copy
    private InputStream getSpec() throws URISyntaxException, MalformedURLException, IOException {
        URL u = new URL(WHATWG_SPEC_HTML5_ELEMENTS_INDEX_URL);
        try {
            URLConnection con = u.openConnection();
            LINK_URL_BASE = Constants.HTML5_MULTIPAGE_SPEC_BASE_URL; //only relative paths, use the relative link url
            return con.getInputStream();
        } catch (IOException ex) {
            //cannot connect, use local copy
            u = ClassLoader.getSystemResource("org/netbeans/modules/html/parser/model/indices-index_2015.html");
            assertNotNull(u);
            try {
                URLConnection con = u.openConnection();
                LINK_URL_BASE = ""; //downloaded file has resolved links to full urls
                System.err.println("Cannot download the specification from " + WHATWG_SPEC_HTML5_ELEMENTS_INDEX_URL + " using local copy.\nBEWARE, it is very likely outdated!!!");
                return con.getInputStream();
            } catch (IOException ex1) {
                throw ex1;
            }

        }
    }

    private InputStream getNamedCharacterReferencesSpec() throws URISyntaxException, MalformedURLException, IOException {
        URL u = new URL(WHATWG_SPEC_HTML5_NAMED_REFS_INDEX_URL);
        URLConnection con = u.openConnection();
        return con.getInputStream();
    }

    private class LLink extends Link {

        public LLink(String name, String url) {
            super(name, LINK_URL_BASE + url);
        }
    }

    private static class NamedCharRef {
        public String name, code;
    }

    private static class Element {

        public Link name;
        public StringBuilder descriptionBuilder = new StringBuilder();
        public String description;
        public Collection<Link> categories = new PrinteableArrayList<Link>();
        public Collection<Link> parents = new PrinteableArrayList<Link>();
        public Collection<Link> children = new PrinteableArrayList<Link>();
        public Collection<Link> attributes = new PrinteableArrayList<Link>();
        public Link interfacee;

        @Override
        public String toString() {
            return "Element{" + "name=" + name + ", description=" + getDescription()
                    + ", categories=" + categories + ", parents=" + parents
                    + ", children=" + children + ", attributes=" + attributes
                    + ", interfacee=" + interfacee + '}';
        }

        public Element shallowCopy() {
            Element copy = new Element();
            copy.name = name;
            copy.description = getDescription();
            copy.categories = categories;
            copy.parents = parents;
            copy.children = children;
            copy.attributes = attributes;
            copy.interfacee = interfacee;
            return copy;
        }

        public String getDescription() {
            return descriptionBuilder.toString().trim();
        }

    }

    private static class PrinteableArrayList<T> extends ArrayList<T> {

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            for (T t : this) {
                b.append(t.toString());
                b.append(',');
            }
            if (b.length() > 0) {
                b.deleteCharAt(b.length() - 1);
            }
            return b.toString();
        }
    }

    private static class ContentHandlerAdapter implements ContentHandler {

        public void setDocumentLocator(Locator locator) {
        }

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
        }

        public void skippedEntity(String name) throws SAXException {
        }
    }
}
