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

package org.netbeans.modules.ant.grammar;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.netbeans.modules.xml.spi.dom.AbstractNode;
import org.netbeans.modules.xml.api.model.DescriptionSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Rather simple query implementation based on static Ant introspection info.
 * Hints given by this grammar cannot guarantee that valid XML document is created.
 *
 * @author Petr Kuzel, Jesse Glick
 */
class AntGrammar implements GrammarQuery {

    private static final Logger LOG = Logger.getLogger(AntGrammar.class.getName());

    /**
     * Allow to get names of <b>parsed general entities</b>.
     * @return list of <code>CompletionResult</code>s (ENTITY_REFERENCE_NODEs)
     */
    public @Override Enumeration<GrammarResult> queryEntities(String prefix) {
        List<GrammarResult> list = new ArrayList<GrammarResult>();

        // add well-know build-in entity names

        if ("lt".startsWith(prefix)) {
            list.add(new MyEntityReference("lt"));
        }
        if ("gt".startsWith(prefix)) {
            list.add(new MyEntityReference("gt"));
        }
        if ("apos".startsWith(prefix)) {
            list.add(new MyEntityReference("apos"));
        }
        if ("quot".startsWith(prefix)) {
            list.add(new MyEntityReference("quot"));
        }
        if ("amp".startsWith(prefix)) {
            list.add(new MyEntityReference("amp"));
        }

        LOG.log(Level.FINE, "queryEntities({0}) -> {1}", new Object[] {prefix, list});
        return Collections.enumeration(list);
    }

    /*
    private static String getTaskClassFor(String elementName) {
        Map defs = getAntGrammar().getDefs("task");
        return (String) defs.get(elementName);
    }

    private static String getTypeClassFor(String elementName) {
        Map defs = getAntGrammar().getDefs("type");
        return (String) defs.get(elementName);
    }
     */

    private static IntrospectedInfo getAntGrammar() {
        return IntrospectedInfo.getKnownInfo();
    }

    enum Kind {
        /** this element is a task */
        TASK,
         /** this element is a data type */
        TYPE,
         /** this element is part of some other structure (task or type) */
        DATA,
         /** tag for root project element */
        PROJECT,
         /** tag for a target element */
        TARGET,
         /** tag for a project description element */
        DESCRIPTION,
         /** tag for an import statement */
        IMPORT;
    }
    static class ElementType {
        final Kind kind;
        final String name; // null for PROJECT, TARGET, DESCRIPTION, IMPORT
        ElementType(Kind kind, String name) {
            this.kind = kind;
            this.name = name;
        }
    }

    /**
     * Determine what a particular element in a build script represents,
     * based on its name and the names of all of its parents.
     * Returns a pair of the kind of the element (one of the KIND_* constants)
     * and the details (a class name suitable for {@link IntrospectedInfo}, or
     * in the case of {@link KIND_SPECIAL}, one of the SPECIAL_* constants).
     * @param e an element
     * @return a two-element string (kind and details), or null if this element is anomalous
     */
    static ElementType typeOf(Element e) {
        String name = e.getNodeName();
        Node p = e.getParentNode();
        if (p == null) {
            throw new IllegalArgumentException("Detached node: " + e); // NOI18N
        }
        if (p.getNodeType() == Node.DOCUMENT_NODE) {
            if (name.equals("project")) { // NOI18N
                return new ElementType(Kind.PROJECT, null);
            } else {
                // Weird root element? Ignore.
                return null;
            }
        } else if (p.getNodeType() == Node.ELEMENT_NODE) {
            // Find ourselves in context.
            ElementType ptype = typeOf((Element)p);
            if (ptype == null) {
                // Unknown parent, therefore this is unknown too.
                return null;
            }
            switch (ptype.kind) {
            case PROJECT:
                // <project> may have <description>, or types, or targets, or tasks
                if (name.equals("description")) { // NOI18N
                    return new ElementType(Kind.DESCRIPTION, null);
                } else if (name.equals("target")) { // NOI18N
                    return new ElementType(Kind.TARGET, null);
                } else if (name.equals("import")) { // NOI18N
                    return new ElementType(Kind.IMPORT, null);
                } else {
                    String taskClazz = getAntGrammar().getDefs("task").get(name); // NOI18N
                    if (taskClazz != null) {
                        return new ElementType(Kind.TASK, taskClazz);
                    } else {
                        String typeClazz = getAntGrammar().getDefs("type").get(name); // NOI18N
                        if (typeClazz != null) {
                            return new ElementType(Kind.TYPE, typeClazz);
                        } else {
                            return null;
                        }
                    }
                }
            case TARGET:
                // <target> may have tasks and types
                String taskClazz = getAntGrammar().getDefs("task").get(name); // NOI18N
                if (taskClazz != null) {
                    return new ElementType(Kind.TASK, taskClazz);
                } else {
                    String typeClazz = getAntGrammar().getDefs("type").get(name); // NOI18N
                    if (typeClazz != null) {
                        return new ElementType(Kind.TYPE, typeClazz);
                    } else {
                        return null;
                    }
                }
            case DESCRIPTION:
                // <description> should have no children!
                return null;
            case IMPORT:
                // <import> should have no children!
                return null;
            default:
                // We must be data.
                String clazz = getAntGrammar().isKnown(ptype.name) ? getAntGrammar().getElements(ptype.name).get(name) : null;
                if (clazz != null) {
                    return new ElementType(Kind.DATA, clazz);
                } else {
                    // Unknown data.
                    return null;
                }
            }
        } else {
            throw new IllegalArgumentException("Bad parent for " + e.toString() + ": " + p); // NOI18N
        }
    }

    /**
     * @stereotype query
     * @output list of results that can be queried on name, and attributes
     * @time Performs fast up to 300 ms.
     * @param ctx represents virtual attribute <code>Node</code> to be replaced. Its parent is a element node.
     * @return list of <code>CompletionResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
     *        Every list member represents one possibility.
     */
    public @Override Enumeration<GrammarResult> queryAttributes(HintContext ctx) {

        Element ownerElement = null;
        // Support both versions of GrammarQuery contract
        if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            ownerElement = ((Attr)ctx).getOwnerElement();
        } else if (ctx.getNodeType() == Node.ELEMENT_NODE) {
            ownerElement = (Element) ctx;
        }
        if (ownerElement == null) {
            return Enumerations.empty();
        }

        NamedNodeMap existingAttributes = ownerElement.getAttributes();
        List<String> possibleAttributes;
        ElementType type = typeOf(ownerElement);
        if (type == null) {
            return Enumerations.empty();
        }

        switch (type.kind) {
        case PROJECT:
            possibleAttributes = new LinkedList<String>();
            possibleAttributes.add("default");
            possibleAttributes.add("name");
            possibleAttributes.add("basedir");
            break;
        case TARGET:
            possibleAttributes = new LinkedList<String>();
            possibleAttributes.add("name");
            possibleAttributes.add("depends");
            possibleAttributes.add("description");
            possibleAttributes.add("if");
            possibleAttributes.add("unless");
            break;
        case DESCRIPTION:
            return Enumerations.empty();
        case IMPORT:
            possibleAttributes = new LinkedList<String>();
            possibleAttributes.add("file");
            possibleAttributes.add("optional");
            break;
        default:
            // task, type, or data; anyway, we have the defining class
            possibleAttributes = new LinkedList<String>();
            if (type.kind == Kind.TYPE) {
                possibleAttributes.add("id");
            }
            if (getAntGrammar().isKnown(type.name)) {
                possibleAttributes.addAll(new TreeSet<String>(getAntGrammar().getAttributes(type.name).keySet()));
            }
            if (type.kind == Kind.TASK) {
                // Can have an ID too, but less important; leave at end.
                possibleAttributes.add("id");
                // Currently IntrospectedInfo includes this in the props for a type,
                // though it excludes it for tasks. So for now add it explicitly
                // only to tasks.
                possibleAttributes.add("description");
                // Also useful sometimes:
                possibleAttributes.add("taskname");
            }
        }

        String prefix = ctx.getCurrentPrefix();

        List<GrammarResult> list = new ArrayList<GrammarResult>();
        for (String attribute : possibleAttributes) {
            if (attribute.startsWith(prefix)) {
                if (existingAttributes.getNamedItem(attribute) == null) {
                    list.add(new MyAttr(attribute));
                }
            }
        }

        LOG.log(Level.FINE, "queryAttributes({0}) -> {1}", new Object[] {prefix, list});
        return Collections.enumeration(list);
    }

    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @param ctx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
     * @return list of <code>CompletionResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes
     *        Every list member represents one possibility.
     */
    public @Override Enumeration<GrammarResult> queryElements(HintContext ctx) {

        Node parent = ((Node)ctx).getParentNode();
        if (parent == null) {
            return Enumerations.empty();
        }
        if (parent.getNodeType() != Node.ELEMENT_NODE) {
            return Enumerations.empty();
        }

        List<String> elements;
        ElementType type = typeOf((Element)parent);
        if (type == null) {
            return Enumerations.empty();
        }

        switch (type.kind) {
        case PROJECT:
            elements = new LinkedList<String>();
            elements.add("target");
            elements.add("import");
            elements.add("property");
            elements.add("description");
            SortedSet<String> tasks = getSortedDefs("task");
            tasks.remove("property");
            tasks.remove("import");
            elements.addAll(tasks); // Ant 1.6 permits any tasks at top level
            elements.addAll(getSortedDefs("type"));
            break;
        case TARGET:
            elements = new ArrayList<String>(getSortedDefs("task"));
            // targets can have embedded types too, though less common:
            elements.addAll(getSortedDefs("type")); // NOI18N
            break;
        case DESCRIPTION:
            return Enumerations.empty();
        case IMPORT:
            return Enumerations.empty();
        default:
            // some introspectable class
            if (getAntGrammar().isKnown(type.name)) {
                elements = new ArrayList<String>(new TreeSet<String>(getAntGrammar().getElements(type.name).keySet()));
            } else {
                elements = Collections.emptyList();
            }
        }

        String prefix = ctx.getCurrentPrefix();

        List<GrammarResult> list = new ArrayList<GrammarResult>();
        for (final String element : elements) {
            if (element.startsWith(prefix)) {
                switch (type.kind) {
                case PROJECT:
                case TARGET:
                    list.add(new MyElement(element) {
                        private URL manpage;

                        {
                            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
                            manpage = cl.getResource("org/apache/tools/ant/module/docs/ant-docs/Tasks/" + element + ".html");
                            if (manpage == null) {
                                manpage = cl.getResource("org/apache/tools/ant/module/docs/ant-docs/Types/" + element + ".html");
                            }
                        }

                        @Override
                        public URL getContentURL() {
                            return manpage;
                        }
                        
                        
                    });
                    break;
                default:
                    list.add(new MyElement(element));
                }
            }
        }

        LOG.log(Level.FINE, "queryElements({0}) -> {1}", new Object[] {prefix, list});
        return Collections.enumeration(list);
    }

    private static SortedSet<String> getSortedDefs(String kind) {
        SortedSet<String> defs = new TreeSet<String>(Collator.getInstance());
        defs.addAll(getAntGrammar().getDefs(kind).keySet());
        return defs;
    }

    /**
     * Allow to get names of <b>declared notations</b>.
     * @return list of <code>CompletionResult</code>s (NOTATION_NODEs)
     */
    public @Override Enumeration<GrammarResult> queryNotations(String prefix) {
        return Enumerations.empty();
    }

    public @Override Enumeration<GrammarResult> queryValues(HintContext ctx) {
        LOG.log(Level.FINE, "queryValues({0})", ctx.getCurrentPrefix());
        // #38341: ctx is apparently instanceof Attr or Text
        // (actually never instanceof Text, just TEXT_NODE: #38339)
        Attr ownerAttr;
        if (canCompleteProperty(ctx.getCurrentPrefix())) {
            LOG.fine("...can complete property");
            return completeProperties(ctx);
        } else if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            ownerAttr = (Attr)ctx;
        } else {
            LOG.fine("...unknown node type");
            return Enumerations.empty();
        }
        Element ownerElement = ownerAttr.getOwnerElement();
        String attrName = ownerAttr.getName();
        ElementType type = typeOf(ownerElement);
        if (type == null) {
            LOG.fine("...unknown type");
            return Enumerations.empty();
        }
        List<String> choices = new ArrayList<String>();

        switch (type.kind) {
        case PROJECT:
            if (attrName.equals("default")) {
            // XXX list known targets?
            } else if (attrName.equals("basedir")) {
            // XXX file completion?
            }
            // freeform: name
            break;
        case TARGET:
            if (attrName.equals("depends")) {
                // XXX list known targets?
            } else if (attrName.equals("if") || attrName.equals("unless")) {
                choices.addAll(Arrays.asList(likelyPropertyNames(ctx)));
            }
            // freeform: description
            break;
        case DESCRIPTION:
            // nothing applicable
            break;
        case IMPORT:
            if (attrName.equals("file")) {
                // freeform
            } else if (attrName.equals("optional")) {
                choices.add("true");
                choices.add("false");
            }
            break;
        default:
            String elementClazz = type.name;
            if (getAntGrammar().isKnown(elementClazz)) {
                String attrClazzName = getAntGrammar().getAttributes(elementClazz).get(attrName);
                if (attrClazzName != null) {
                    if (getAntGrammar().isKnown(attrClazzName)) {
                        String[] enumTags = getAntGrammar().getTags(attrClazzName);
                        if (enumTags != null) {
                            choices.addAll(Arrays.asList(enumTags));
                        }
                    }
                    if (attrClazzName.equals("boolean")) {
                        choices.add("true");
                        choices.add("false");
                    } else if (attrClazzName.equals("org.apache.tools.ant.types.Reference")) {
                        // XXX add names of ids
                    } else if (attrClazzName.equals("org.apache.tools.ant.types.Path") ||
                               attrClazzName.equals("java.io.File")
                               /* || "path" attr on Path or Path.Element */
                              ) {
                        // XXX complete filenames
                    } else if (attrClazzName.equals("java.lang.String") &&
                               Arrays.asList(PROPERTY_NAME_VALUED_PROPERTY_NAMES).contains(attrName)) {
                        // <isset property="..."/>, <include name="*" unless="..."/>, etc.
                        choices.addAll(Arrays.asList(likelyPropertyNames(ctx)));
                    }
                }
            }
        }

        // Create the completion:
        String prefix = ctx.getCurrentPrefix();
        List<GrammarResult> list = new ArrayList<GrammarResult>();
        for (String choice : choices) {
            if (choice.startsWith(prefix)) {
                list.add (new MyText(choice, choice, -1));
            }
        }

        LOG.log(Level.FINE, "queryValues({0}) -> {1}", new Object[] {prefix, list});
        return Collections.enumeration(list);
    }

    /**
     * Check whether a given content string (of an attribute value or of an element's
     * content) has an uncompleted "${" sequence in it, i.e. one that has not been matched
     * with a corresponding "}".
     * E.g.:
     * <pathelement location="${foo
     *                             ^ caret
     * Also if the last character is "$" it can be completed.
     * @param content the current content of the attribute value or element
     * @return true if there is an uncompleted property here
     */
    private static boolean canCompleteProperty(String content) {
        content = deletedEscapedShells(content);
        if (content.length() == 0) {
            return false;
        }
        if (content.charAt(content.length() - 1) == '$') {
            return true;
        }
        int idx = content.lastIndexOf("${");
        return idx != -1 && content.indexOf('}', idx) == -1;
    }

    private static Enumeration<GrammarResult> completeProperties(HintContext ctx) {
        String content = ctx.getCurrentPrefix();
        assert content.length() > 0;
        String header;
        String propPrefix;
        String wholeText = ctx.getNodeValue();
        int replaceLen;
        String suffix = "";
        
        if (content.charAt(content.length() - 1) == '$') { // NOI18N
            header = content + '{';
            propPrefix = "";
            // no closing brace, since even opening brace is missing
            replaceLen = 1;
        } else {
            int idx = content.lastIndexOf("${"); // NOI18N
            assert idx != -1;
            header = content.substring(0, idx + 2);
            propPrefix = content.substring(idx + 2);
            
            int closingBrace = wholeText.indexOf('}', content.length()); // NOI18N
            if (closingBrace == -1) {
                replaceLen = content.length();
                suffix = wholeText.substring(replaceLen);
            } else {
                // only delete the text up to the closing brace
                replaceLen = closingBrace + 1;
            }
        }
        String[] props = likelyPropertyNames(ctx);
        // completion on text works differently from attrs:
        // the context should not be returned (#38342)
        boolean shortHeader = ctx.getNodeType() == Node.TEXT_NODE;
        List<GrammarResult> list = new ArrayList<GrammarResult>();
        int pl = propPrefix.length();
        for (int i = 0; i < props.length; i++) {
            if (props[i].startsWith(propPrefix)) {
                String text = header + props[i] + '}';
                String all = "${" + props[i] + "}";
                int l = 0;
                if (shortHeader) {
                    assert text.startsWith(content) : "text=" + text + " content=" + content;
                    text = text.substring(content.length());
                }
                for (; l < suffix.length() && l + pl < props[i].length(); l++) {
                    if (suffix.charAt(l) != props[i].charAt(l + pl)) {
                        break;
                    }
                }
                if (shortHeader && (content.length() <= replaceLen)) {
                    l = -content.length();
                }
                replaceLen += l;
                list.add(new MyText(text, all, replaceLen));
            }
        }
        LOG.log(Level.FINE, "completeProperties({0}) -> {1}", new Object[] {content, list});
        return Collections.enumeration(list);
    }

    /**
     * Names of Ant properties that are generally present and defined in any script.
     */
    private static final String[] STOCK_PROPERTY_NAMES = {
        // Present in most Ant installations:
        "ant.home", // NOI18N
        // Defined by Ant as standard properties:
        "basedir", // NOI18N
        "ant.file", // NOI18N
        "ant.project.name", // NOI18N
        "ant.java.version", // NOI18N
        "ant.version", // NOI18N
        // Defined by System.getProperties as standard system properties:
        "java.version", // NOI18N
        "java.vendor", // NOI18N
        "java.vendor.url", // NOI18N
        "java.home", // NOI18N
        "java.vm.specification.version", // NOI18N
        "java.vm.specification.vendor", // NOI18N
        "java.vm.specification.name", // NOI18N
        "java.vm.version", // NOI18N
        "java.vm.vendor", // NOI18N
        "java.vm.name", // NOI18N
        "java.specification.version", // NOI18N
        "java.specification.vendor", // NOI18N
        "java.specification.name", // NOI18N
        "java.class.version", // NOI18N
        "java.class.path", // NOI18N
        "java.library.path", // NOI18N
        "java.io.tmpdir", // NOI18N
        "java.compiler", // NOI18N
        "java.ext.dirs", // NOI18N
        "os.name", // NOI18N
        "os.arch", // NOI18N
        "os.version", // NOI18N
        "file.separator", // NOI18N
        "path.separator", // NOI18N
        "line.separator", // NOI18N
        "user.name", // NOI18N
        "user.home", // NOI18N
        "user.dir", // NOI18N
    };

    private static String[] likelyPropertyNames(HintContext ctx) {
        // #38343: ctx.getOwnerDocument returns some bogus unusable empty thing
        // so find the root element manually
        Element parent;
        // #38341: docs for queryValues says Attr or Element, but really Attr or Text
        // (and CDataSection never seems to permit completion at all...)
        if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            parent = ((Attr)ctx).getOwnerElement();
        } else if (ctx.getNodeType() == Node.TEXT_NODE) {
            Node p = ctx.getParentNode();
            if (p == null) {
                return new String[0];
            } else if (p.getNodeType() == Node.ELEMENT_NODE) {
                parent = (Element)p;
            } else {
                LOG.log(Level.WARNING, "strange parent of text node: {0} {1}", new Object[] {p.getNodeType(), p});
                return new String[0];
            }
        } else {
            LOG.log(Level.WARNING, "strange context type: {0} {1}", new Object[] {ctx.getNodeType(), ctx});
            return new String[0];
        }
        while (parent.getParentNode() != null && parent.getParentNode().getNodeType() == Node.ELEMENT_NODE) {
            parent = (Element)parent.getParentNode();
        }
        // #38343: getElementsByTagName just throws an exception, you can't use it...
        Set<String> choices = new TreeSet<String>(Arrays.asList(STOCK_PROPERTY_NAMES));
        visitForLikelyPropertyNames(parent, choices);
        Iterator<String> it = choices.iterator();
        while (it.hasNext()) {
            String propname = it.next();
            if (propname.indexOf("${") != -1) {
                // Not actually a direct property name, rather a computed name.
                // Skip it as it cannot be used here.
                it.remove();
            }
        }
        return choices.toArray(new String[0]);
    }

    private static final String[] PROPERTY_NAME_VALUED_PROPERTY_NAMES = {
        "if",
        "unless",
        // XXX accept any *property
        "property",
        "failureproperty",
        "errorproperty",
        "addproperty",
    };

    private static void visitForLikelyPropertyNames(Node n, Set<String> choices) {
        int type = n.getNodeType();
        switch (type) {
            case Node.ELEMENT_NODE:
                // XXX would be more precise to use typeOf here, but maybe slower?
                // Look for <property name="propname" .../> and similar
                Element el = (Element)n;
                String tagname = el.getTagName();
                if (tagname.equals("property")) {
                    String propname = el.getAttribute("name");
                    // #38343: Element impl is broken and can return null from getAttribute
                    if (propname != null && propname.length() > 0) {
                        choices.add(propname);
                    }
                    // XXX handle <property file="..."/> with a resolvable filename
                } else if (tagname.equals("buildnumber")) {
                    // This task always defines ${build.number}
                    choices.add("build.number");
                } else if (tagname.equals("tstamp")) {
                    // XXX handle prefix="whatever" -> ${whatever.TODAY} etc.
                    // XXX handle nested <format property="foo" .../> -> ${foo}
                    choices.add("DSTAMP");
                    choices.add("TSTAMP");
                    choices.add("TODAY");
                }
                // <available>, <dirname>, <pathconvert>, <uptodate>, <target>, <isset>, <include>, etc.
                for (int i = 0; i < PROPERTY_NAME_VALUED_PROPERTY_NAMES.length; i++) {
                    String propname = el.getAttribute(PROPERTY_NAME_VALUED_PROPERTY_NAMES[i]);
                    if (propname != null && propname.length() > 0) {
                        choices.add(propname);
                    }
                }
                break;
            case Node.ATTRIBUTE_NODE:
            case Node.TEXT_NODE:
                // Look for ${propname}
                String text = deletedEscapedShells(n.getNodeValue());
                int idx = 0;
                while (true) {
                    int start = text.indexOf("${", idx);
                    if (start == -1) {
                        break;
                    }
                    int end = text.indexOf('}', start + 2);
                    if (end == -1) {
                        break;
                    }
                    String propname = text.substring(start + 2, end);
                    if (propname.length() > 0) {
                        choices.add(propname);
                    }
                    idx = end + 1;
                }
                break;
            default:
                // ignore
                break;
        }
        NodeList l = n.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            visitForLikelyPropertyNames(l.item(i), choices);
        }
        // Element attributes are not considered child nodes as such.
        NamedNodeMap m = n.getAttributes();
        if (m != null) {
            for (int i = 0; i < m.getLength(); i++) {
                visitForLikelyPropertyNames(m.item(i), choices);
            }
        }
    }

    /**
     * Remove pairs of '$$' to avoid being confused by them.
     * They do not introduce property references.
     */
    private static String deletedEscapedShells(String text) {
        // XXX could be faster w/o regexps
        return text.replaceAll("\\$\\$", "");
    }

    // return defaults, no way to query them
    public @Override GrammarResult queryDefault(final HintContext ctx) {
        return null;
    }

    // it is not yet implemented
    public @Override boolean isAllowed(Enumeration<GrammarResult> en) {
        return true;
    }

    // customizers section ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public @Override java.awt.Component getCustomizer(HintContext ctx) {
        return null;
    }

    public @Override boolean hasCustomizer(HintContext ctx) {
        return false;
    }

    public @Override org.openide.nodes.Node.Property<?>[] getProperties(HintContext ctx) {
        return null;
    }


    // Result classes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private abstract static class AbstractResultNode extends AbstractNode implements GrammarResult {

        public @Override Icon getIcon(int kind) {
            return null;
        }

        public @Override String getDescription() {
            return null;
        }

        public @Override String getDisplayName() {
            return null;
        }

        // TODO in MyElement return true for really empty elements such as "pathelement"
        public @Override boolean isEmptyElement() {
            return false;
        }
    }

    private static class MyEntityReference extends AbstractResultNode implements EntityReference {

        private String name;

        MyEntityReference(String name) {
            this.name = name;
        }

        public @Override short getNodeType() {
            return Node.ENTITY_REFERENCE_NODE;
        }

        public @Override String getNodeName() {
            return name;
        }

    }

    private static class MyElement extends AbstractResultNode implements Element, DescriptionSource {

        private final String name;

        MyElement(String name) {
            this.name = name;
        }

        public @Override short getNodeType() {
            return Node.ELEMENT_NODE;
        }

        public @Override String getNodeName() {
            return name;
        }

        public @Override String getTagName() {
            return name;
        }

        public @Override String toString() {
            return name;
        }
        
        @Override
        public DescriptionSource resolveLink(String link) {
            return null;
        }
        
        @Override
        public boolean isExternal() {
            return false;
        }

        @Override
        public URL getContentURL() {
            return null;
        }
    }

    private static class MyAttr extends AbstractResultNode implements Attr {

        private final String name;

        MyAttr(String name) {
            this.name = name;
        }

        public @Override short getNodeType() {
            return Node.ATTRIBUTE_NODE;
        }

        public @Override String getNodeName() {
            return name;
        }

        public @Override String getName() {
            return name;
        }

        public @Override String getValue() {
            return null;  //??? what spec says
        }

        public @Override String toString() {
            return name;
        }

    }

    private static class MyText extends AbstractResultNode implements Text {

        private final String data;
        private final String displayName;
        private final int    replace;
        
        MyText(String data, String displayName, int replace) {
            this.data = data;
            this.displayName = displayName;
            this.replace = replace;
        }


        public @Override short getNodeType() {
            return Node.TEXT_NODE;
        }

        public @Override String getNodeValue() {
            return data;
        }

        public @Override String getData() throws DOMException {
            return data;
        }

        public @Override int getLength() {
//            if (replace != -1) {
//                return replace;
//            }
//            return data == null ? -1 : data.length();
            return replace;
        }

        public @Override String toString() {
            return data;
        }

        public @Override String getDisplayName() {
            return displayName;
        }

    }

}
