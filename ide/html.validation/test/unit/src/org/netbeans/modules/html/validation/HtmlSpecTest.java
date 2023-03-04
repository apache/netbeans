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
package org.netbeans.modules.html.validation;

import com.thaiopensource.xml.util.Name;
import java.io.IOException;
import java.io.StringWriter;
import nu.validator.saxtree.DocumentFragment;
import nu.validator.saxtree.TreeParser;
import nu.validator.spec.Spec;
import nu.validator.spec.html5.Html5SpecBuilder;
import org.netbeans.junit.NbTestCase;
import nu.validator.localentities.LocalCacheEntityResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * For generation of the content of org.netbeans.modules.html.parser.ElementAttributes
 *
 * @author marekfukala
 */
public class HtmlSpecTest extends NbTestCase {

    private static String HTML_NS = "http://www.w3.org/1999/xhtml"; //NOI18N

    
    private static String[] ELEMENT_NAMES = new String[]{"a", "b", "g", "i", "p", "q", "s", "u",
        "br", "ci", "cn", "dd", "dl", "dt", "em", "eq",
        "fn", "h1", "h2", "h3", "h4", "h5", "h6", "gt",
        "hr", "in", "li", "ln", "lt", "mi", "mn", "mo",
        "ms", "ol", "or", "pi", "rp", "rt", "td", "th",
        "tr", "tt", "ul", "and", "arg", "abs", "big", "bdo",
        "csc", "col", "cos", "cot", "del", "dfn", "dir", "div",
        "exp", "gcd", "geq", "img", "ins", "int", "kbd", "log",
        "lcm", "leq", "mtd", "min", "map", "mtr", "max", "neq",
        "not", "nav", "pre", "rem", "sub", "sec", "svg", "sum",
        "sin", "sep", "sup", "set", "tan", "use", "var", "wbr",
        "xmp", "xor", "area", "abbr", "base", "bvar", "body", "card",
        "code", "cite", "csch", "cosh", "coth", "curl", "desc", "diff",
        "defs", "form", "font", "grad", "head", "html", "line", "link",
        "list", "meta", "msub", "mode", "math", "mark", "mask", "mean",
        "msup", "menu", "mrow", "none", "nobr", "nest", "path", "plus",
        "rule", "real", "reln", "rect", "root", "ruby", "sech", "sinh",
        "span", "samp", "stop", "sdev", "time", "true", "tref", "tanh",
        "text", "view", "aside", "audio", "apply", "embed", "frame", "false",
        "floor", "glyph", "hkern", "image", "ident", "input", "label", "limit",
        "mfrac", "mpath", "meter", "mover", "minus", "mroot", "msqrt", "mtext",
        "notin", "piece", "param", "power", "reals", "style", "small", "thead",
        "table", "title", "tspan", "times", "tfoot", "tbody", "union", "vkern",
        "video", "arcsec", "arccsc", "arctan", "arcsin", "arccos", "applet", "arccot",
        "approx", "button", "circle", "center", "cursor", "canvas", "divide", "degree",
        "dialog", "domain", "exists", "fetile", "figure", "forall", "filter", "footer",
        "header", "iframe", "keygen", "lambda", "legend", "mspace", "mtable", "mstyle",
        "mglyph", "median", "munder", "marker", "merror", "moment", "matrix", "option",
        "object", "output", "primes", "source", "strike", "strong", "switch", "symbol",
        "spacer", "select", "subset", "script", "tbreak", "vector", "article", "animate",
        "arcsech", "arccsch", "arctanh", "arcsinh", "arccosh", "arccoth", "acronym", "address",
        "bgsound", "command", "compose", "ceiling", "csymbol", "caption", "discard", "declare",
        "details", "ellipse", "fefunca", "fefuncb", "feblend", "feflood", "feimage", "femerge",
        "fefuncg", "fefuncr", "handler", "inverse", "implies", "isindex", "logbase", "listing",
        "mfenced", "mpadded", "marquee", "maction", "msubsup", "noembed", "polygon", "pattern",
        "product", "setdiff", "section", "tendsto", "uplimit", "altglyph", "basefont", "clippath",
        "codomain", "colgroup", "datagrid", "emptyset", "factorof", "fieldset", "frameset", "feoffset",
        "glyphref", "interval", "integers", "infinity", "listener", "lowlimit", "metadata", "menclose",
        "mphantom", "noframes", "noscript", "optgroup", "polyline", "prefetch", "progress", "prsubset",
        "quotient", "selector", "textarea", "textpath", "variance", "animation", "conjugate", "condition",
        "complexes", "font-face", "factorial", "intersect", "imaginary", "laplacian", "matrixrow", "notsubset",
        "otherwise", "piecewise", "plaintext", "rationals", "semantics", "transpose", "annotation", "blockquote",
        "divergence", "eulergamma", "equivalent", "imaginaryi", "malignmark", "munderover", "mlabeledtr", "notanumber",
        "solidcolor", "altglyphdef", "determinant", "eventsource", "femergenode", "fecomposite", "fespotlight", "maligngroup",
        "mprescripts", "momentabout", "notprsubset", "partialdiff", "altglyphitem", "animatecolor", "datatemplate", "exponentiale",
        "feturbulence", "fepointlight", "femorphology", "outerproduct", "animatemotion", "color-profile", "font-face-src", "font-face-uri",
        "foreignobject", "fecolormatrix", "missing-glyph", "mmultiscripts", "scalarproduct", "vectorproduct", "annotation-xml", "definition-src",
        "font-face-name", "fegaussianblur", "fedistantlight", "lineargradient", "naturalnumbers", "radialgradient", "animatetransform", "cartesianproduct",
        "font-face-format", "feconvolvematrix", "fediffuselighting", "fedisplacementmap", "fespecularlighting", "domainofapplication", "fecomponenttransfer"};

    public HtmlSpecTest(String name) {
        super(name);
    }

    public void testSpec() throws IOException, SAXException {
        Spec spec = Html5SpecBuilder.parseSpec(LocalCacheEntityResolver.getHtml5SpecAsStream());

        StringWriter sw = new StringWriter();
        AttrsGatheringContentHandler handler = new AttrsGatheringContentHandler(sw);

        sw.write("private static String[][] ATTRS = new String[][]{");
        for (String name : ELEMENT_NAMES) {
            Name element = new Name(HTML_NS, name);
            DocumentFragment df = spec.elementSpecificAttributesDescription(element);

            if (df == null) {
//                System.err.println("DocumentFragment for element "  + name + " not found!");
                continue;
            }

            sw.write("{\"" + name + "\"}, {");

            TreeParser tp = new TreeParser(handler, null);
            handler.startDocument();
            tp.parse(df);
            handler.endDocument();

            sw.write("},\n");

        }

//        System.out.println("Output:\n" + sw.toString());


    }

    private class AttrsGatheringContentHandler implements ContentHandler {

        private StringWriter writer;
        private boolean inlink;
        private boolean first;

        public AttrsGatheringContentHandler(StringWriter writer) {
            this.writer = writer;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
            inlink = false;
            first = true;
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("a".equals(localName)) {
                inlink = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("a".equals(localName)) {
                inlink = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inlink) {
                String text = new String(ch, start, length);
                if ("Global attributes".equals(text)) {
                    //ignore
                    return;
                }

                if(!first) {
                    writer.write(", ");
                } else {
                    first = false;
                }

                writer.write("\"");
                writer.write(ch, start, length);
                writer.write("\"");
            }
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
        }
    }
}
