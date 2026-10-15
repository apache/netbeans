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

package org.netbeans.modules.maven.grammar.spi;

import java.awt.Component;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import org.apache.maven.project.MavenProject;
import org.jdom2.Content;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.jdom2.Document;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.netbeans.modules.xml.spi.dom.AbstractNode;
import org.openide.ErrorManager;
import org.openide.util.Enumerations;
import org.openide.nodes.Node.Property;
import org.openide.util.ImageUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author mkleint
 */
public abstract class AbstractSchemaBasedGrammar implements GrammarQuery {
    Document schemaDoc;

    private GrammarEnvironment environment;
    /** Creates a new instance of NewClass */
    public AbstractSchemaBasedGrammar(GrammarEnvironment env) {
        environment = env;
        try {
            SAXBuilder builder = new SAXBuilder();
            InputStream stream = getSchemaStream();
            schemaDoc = builder.build(stream);
        } catch (Exception exc) {
            ErrorManager.getDefault().notify(exc);
        }
        
    }
    
    protected final GrammarEnvironment getEnvironment() {
        return environment;
    }
    
    protected final MavenProject getMavenProject() {
        Project proj = FileOwnerQuery.getOwner(environment.getFileObject());
        if (proj != null) {
            NbMavenProject watch = proj.getLookup().lookup(NbMavenProject.class);
            assert watch != null;
            return watch.getMavenProject();
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, "File " + environment.getFileObject() + " has maven2 code completion but doesn't belong to a maven2 project."); //NOI18N
        return null;
    }
    
    /**
     * the input stream of the xml schema document that describes the document elements.
     */
    protected abstract InputStream getSchemaStream();
    
    
    /**
     * to override by subclasses that want to provide some dynamic content un a specific subtree.
     * @param path is slash separated path string
     * @return the actual completion nodes or empty list
     */
    protected List<GrammarResult> getDynamicCompletion(String path, HintContext hintCtx, org.jdom2.Element lowestParent) {
        return Collections.<GrammarResult>emptyList();
    }

    /**
     * to override by subclasses that want to provide some dynamic content un a specific subtree.
     * @param path is slash separated path string
     * @return null, if no such offering exists or the actual completion nodes..
     */
    protected Enumeration<GrammarResult> getDynamicValueCompletion(String path, HintContext virtualTextCtx, org.jdom2.Element el) {
        return null;
    }
    
    
    protected final org.jdom2.Element findElement(org.jdom2.Element parent, String name) {
        @SuppressWarnings("unchecked")
        List<org.jdom2.Element> childs = parent.getChildren("element", parent.getNamespace()); //NOI18N
        for (org.jdom2.Element el : childs) {
            if (name.equals(el.getAttributeValue("name"))) { //NOI18N
                return el;
            }
        }
        return null;
    }

    
    protected final org.jdom2.Element findNonTypedContent(org.jdom2.Element root) {
        org.jdom2.Element complex = root.getChild("complexType", root.getNamespace()); //NOI18N
        if (complex != null) {
            complex = complex.getChild("sequence", root.getNamespace()); //NOI18N
        }
        return complex;
        
    }

    
    protected final org.jdom2.Element findTypeContent(final String type, org.jdom2.Element docRoot) {
        List<Content> lst = docRoot.getContent();
        for (Content c : lst) {
            if (c instanceof org.jdom2.Element) {
                org.jdom2.Element el = (org.jdom2.Element)c;
                if ("complexType".equals(el.getName()) && type.equals(el.getAttributeValue("name"))) { //NOI18N
                    return el.getChild("all", docRoot.getNamespace()); //NOI18N
                }
            }
        }
        return null;
    }
    

    
      private void processElement(String matches, org.jdom2.Element childEl, Vector<GrammarResult> suggestions) {
        String childRefAttr = childEl.getAttributeValue("ref"); //NOI18N
        if (childRefAttr == null) {
            // if ref not defined, go check name attribute..
            childRefAttr = childEl.getAttributeValue("name"); //NOI18N
        }
        if (childRefAttr != null && childRefAttr.startsWith(matches)) {
            suggestions.add(new MyElement(childRefAttr));
        }
    }
    
    @Override
    public Component getCustomizer(HintContext nodeCtx) {
        return null;
    }

    /**
     * Allows Grammars to supply properties for the HintContext
     * @param ctx the hint context node
     * @return an array of properties for this context
     */
    @Override
    public Property[] getProperties(HintContext nodeCtx) {
        return new Property[0];
    }

    
    @Override
    public boolean hasCustomizer(HintContext nodeCtx) {
        return false;
    }

    /**
     * Distinquieshes between empty enumaration types.
     * @return <code>true</code> there is no known result
     *         <code>false</code> grammar does not allow here a result
     */
    @Override
    public boolean isAllowed(Enumeration en) {
        return true;
    }

    
    protected final void processSequence(String matches, org.jdom2.Element seqEl, Vector<GrammarResult> suggestions) {
        @SuppressWarnings("unchecked")
        List<org.jdom2.Element> availables = seqEl.getContent(new ElementFilter());
        for (org.jdom2.Element childEl : availables) {
            if ("element".equals(childEl.getName()) || "group".equals(childEl.getName())) { //NOI18N
                processElement(matches, childEl, suggestions);
            }
        }
    }

    
    /**
     * Query attribute options for given context. All implementations must handle
     * queries based on owner element context.
     * @stereotype query
     * @output list of results that can be queried on name, and attributes
     * @time Performs fast up to 300 ms.
     * @param ownerElementCtx represents owner <code>Element</code> that will host result.
     * @return enumeration of <code>GrammarResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
     *         Every list member represents one possibility.
     */
    @Override
    public Enumeration<GrammarResult> queryAttributes(HintContext ownerElementCtx) {
        return Enumerations.<GrammarResult>empty();
    }

    /**
     * query default value for given context. Two context types must be handled:
     * an attribute and an element context.
     * @param parentNodeCtx context for which default is queried
     * @return default value or <code>null</code>
     */
    @Override
    public GrammarResult queryDefault(HintContext parentNodeCtx) {
        return null;
    }

    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @param virtualElementCtx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
     * @return enumeration of <code>GrammarResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes.
     *         Every list member represents one possibility.
     */
    @Override
    public Enumeration<GrammarResult> queryElements(HintContext virtualElementCtx) {
        String start = virtualElementCtx.getCurrentPrefix();
        
        Node parentNode = virtualElementCtx.getParentNode();
        boolean hasSchema = false;
        if (parentNode != null && schemaDoc != null) {
            List<String> parentNames = new ArrayList<String>();
            while (parentNode != null & parentNode.getNodeName() != null) {
                parentNames.add(0, parentNode.getNodeName());
                if (parentNode.getParentNode() == null || parentNode.getParentNode().getNodeName() == null) {
                    NamedNodeMap nnm = parentNode.getAttributes();
                    hasSchema  = nnm.getNamedItemNS("xsi","schemaLocation") != null;
                }
                parentNode = parentNode.getParentNode();
            }
            org.jdom2.Element schemaParent = schemaDoc.getRootElement();
            Iterator<String> it = parentNames.iterator();
            String path = ""; //NOI18N
            Vector<GrammarResult> toReturn = new Vector<GrammarResult>();
            while (it.hasNext() && schemaParent != null) {
                String str = it.next();
                path = path + "/" + str; //NOI18N
                org.jdom2.Element el = findElement(schemaParent, str);
                if (!it.hasNext()) {
                    toReturn.addAll(getDynamicCompletion(path, virtualElementCtx, el));
                }
                if (el != null) {
                    String type = el.getAttributeValue("type"); //NOI18N
                    if (type != null) {
                        schemaParent = findTypeContent(type, schemaDoc.getRootElement());
                        if (schemaParent == null) {
                            System.err.println("no schema parent for " + str + " of type " + el.getAttributeValue("type")); //NOI18N
                        }
                    } else {
                        schemaParent = findNonTypedContent(el);
                    }
                } else {
//                    System.err.println("cannot find element=" + str); //NOI18N
                }
            }
            if (schemaParent != null && !hasSchema) {
                processSequence(start, schemaParent, toReturn);
            }
            return toReturn.elements();
        } else {
            return Enumerations.<GrammarResult>empty();
        }
    }

    
    
    /**
     * Allow to get names of <b>parsed general entities</b>.
     * @param prefix prefix filter
     * @return enumeration of <code>GrammarResult</code>s (ENTITY_REFERENCE_NODEs)
     */
    @Override
    public Enumeration<GrammarResult> queryEntities(String prefix) {
        return Enumerations.<GrammarResult>empty();
    }

    /**
     * Allow to get names of <b>declared notations</b>.
     * @param prefix prefix filter
     * @return enumeration of <code>GrammarResult</code>s (NOTATION_NODEs)
     */
    @Override
    public Enumeration<GrammarResult> queryNotations(String prefix) {
        return Enumerations.<GrammarResult>empty();
    }

    /**
     * @inherited
     * @param virtualTextCtx
     * @return
     */
    @Override
    public Enumeration<GrammarResult> queryValues(HintContext virtualTextCtx) {
        Node parentNode = virtualTextCtx.getParentNode();
        List<String> parentNames = new ArrayList<String>();
        if (virtualTextCtx.getCurrentPrefix().length() == 0) {
            parentNames.add(virtualTextCtx.getNodeName());
        }
        if (parentNode != null && schemaDoc != null) {
            while (parentNode != null & parentNode.getNodeName() != null) {
                parentNames.add(0, parentNode.getNodeName());
                parentNode = parentNode.getParentNode();
            }
            org.jdom2.Element schemaParent = schemaDoc.getRootElement();
            Iterator<String> it = parentNames.iterator();
            String path = ""; //NOI18N
            while (it.hasNext() && schemaParent != null) {
                String str = it.next();
                path = path + "/" + str; //NOI18N
                org.jdom2.Element el = findElement(schemaParent, str);
                if (!it.hasNext()) {
                    Enumeration<GrammarResult> en = getDynamicValueCompletion(path, virtualTextCtx, el);
                    if (en != null) {
                        return en;
                    }
                }
                if (el != null) {
                    String type = el.getAttributeValue("type"); //NOI18N
                    if (type != null) {
                        schemaParent = findTypeContent(type, schemaDoc.getRootElement());
                        if (schemaParent == null) {
                            System.err.println("no schema parent for " + str + " of type=" + el.getAttributeValue("type")); //NOI18N
                        }
                    } else {
                        schemaParent = findNonTypedContent(el);
                    }
                } else {
                    //System.err.println("cannot find element=" + str); //NOI18N
                }                
            }
        }
        return Enumerations.<GrammarResult>empty();
    }


    /**
     * for subclasses that  have a given list of possible values in the element's text content. 
     */
    protected final Enumeration<GrammarResult> createTextValueList(String[] values, HintContext context) {
        Collection<GrammarResult> elems = new ArrayList<GrammarResult>();
        for (String value :  values) {
            if (value.startsWith(context.getCurrentPrefix())) {
                elems.add(new MyTextElement(value, context.getCurrentPrefix()));
            }
        }
        return Collections.enumeration(elems);
        
    }
    
    protected abstract static class AbstractResultNode extends AbstractNode implements GrammarResult {
        private String desc;
        private Icon icon;
        
        @Override
        public Icon getIcon(int kind) {
            return icon;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }
        
        /**
         * @output provide additional information simplifiing decision
         */
        @Override
        public String getDescription() {
            return desc;
        }

        public void setDescription(String desc) {
            this.desc = desc;
        }
        
        /**
         * @output text representing name of suitable entity
         * //??? is it really needed
         */
        public String getText() {
            return getNodeName();
        }
        
        /**
         * @output name that is presented to user
         */
        @Override
        public String getDisplayName() {
            return getNodeName();
        }
        
        /**
         * For elements provide hint whether element has empty content model.
         * @return true element has empty content model (no childs) and can
         * be completed in empty element form i.e. <code>&lt;ement/></code>.
         * @since 6th Aug 2004
         */
        @Override
        public boolean isEmptyElement() {
            return false;
        }

        @Override public String toString() {
            return getDisplayName();
        }
        
    }

    
    public static class MyElement extends AbstractResultNode implements Element {
        
        private String name;
        
        public MyElement(String name) {
            this.name = name;
            setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/maven/grammar/element.png", false)); //NOI18N
        }
        
        @Override
        public short getNodeType() {
            return Node.ELEMENT_NODE;
        }
        
        @Override
        public String getNodeName() {
            return name;
        }
        
        @Override
        public String getTagName() {
            return name;
        }
        
    }
    
    public static class PartialTextElement extends AbstractResultNode implements Text {
        public PartialTextElement() {
            setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/maven/navigator/wait.gif", false)); //NOI18N
        }
        
        @Override
        public short getNodeType() {
            return Node.TEXT_NODE;
        }
        
        @Override
        public String getNodeName() {
            return "Incomplete result, still processing indices...";
        }
        
        @Override
        public String getTagName() {
            return "Partial result";
        }
        
        @Override
        public String getNodeValue() {
            return "";
        }        
    }


     public static class MyTextElement extends AbstractResultNode implements Text {
        
        protected final String name;
        protected final String prefix;
        
        public MyTextElement(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
            setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/maven/grammar/value.png", false)); //NOI18N
        }
        
        @Override
        public short getNodeType() {
            return Node.TEXT_NODE;
        }
        
        @Override
        public String getNodeName() {
            return name;
        }
        
        @Override
        public String getTagName() {
            return name;
        }
        
        @Override
        public String getNodeValue() {
            return name.substring(prefix.length());
        }
        
    }
     
    protected static class ExpressionValueTextElement extends AbstractSchemaBasedGrammar.MyTextElement {
        private final int delLen;
        private final String suffix;
        
        public ExpressionValueTextElement(String pr, String propPrefix, String suffix) {
            super(pr, propPrefix);
            this.suffix = suffix;
            this.delLen = -1;
        }

        public ExpressionValueTextElement(String pr, String propPrefix, int delL) {
            super(pr, propPrefix);
            this.suffix = null;
            this.delLen = delL;
        }
        
          @Override
        public String getNodeValue() {
            String end = name.substring(prefix.length()) + "}";
            if (suffix == null) {
                return end;
            }
            String suff = suffix;
            if (suff.indexOf("}") < suff.indexOf("{")) {
                suff = suff.substring(suff.indexOf("}") + 1);
            }
            return suff;
         }
        
        public int getLength() {
            return delLen == -1 ? super.getLength() : delLen;
        }
    }
     
    protected static class ComplexElement extends AbstractResultNode implements Element {
        
        private String name;
        private String display;
        private NodeList list;
        
        ComplexElement(String tagName, String displayName, NodeList listimpl) {
            this.name = tagName;
            display = displayName;
            list = listimpl;
        }
        
        @Override
        public short getNodeType() {
            return Node.ELEMENT_NODE;
        }
        
        @Override
        public String getNodeName() {
            return name;
        }
        
        @Override
        public String getTagName() {
            return name;
        }
        
        @Override
        public String getDisplayName() {
            return display;
        }

        @Override
        public NodeList getChildNodes() {
            return list;
        }
     /**
     * @return false
     */
        @Override
    public boolean hasChildNodes() {
        return true;
    }
        @Override
    public org.w3c.dom.Node getLastChild() {
        return list.item(list.getLength() - 1);
    }
    /**
     * @return null
     */
        @Override
    public org.w3c.dom.Node getFirstChild() {
        return list.item(0);
    }

       
        
    }
     
   
}
