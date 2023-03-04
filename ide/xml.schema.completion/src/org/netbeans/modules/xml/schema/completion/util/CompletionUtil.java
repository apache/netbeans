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
package org.netbeans.modules.xml.schema.completion.util;

import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.completion.*;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.visitor.FindSubstitutions;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Utility class containing methods to query the model for completion suggestions.
 * 
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompletionUtil {
    public static final String
        TAG_FIRST_CHAR = "<", //NOI18N
        TAG_LAST_CHAR  = ">", //NOI18N
        END_TAG_PREFIX = "</", //NOI18N
        END_TAG_SUFFIX = "/>"; //NOI18N

    // Pattern: ("<" + ("blank space" or "\n") + "any characters")
    //          ("<" + "/" + ("blank space" or "\n") + "any characters")
    public static final Pattern PATTERN_TEXT_TAG_EOLs = Pattern.compile("</?[\\s]+.*");
    
    private static final Logger _logger = Logger.getLogger(CompletionUtil.class.getName());

    public static boolean noCompletion(JTextComponent target) {
        if (target == null || target.getCaret() == null) {
            return false;
        }
        int offset = target.getCaret().getDot();
        if (offset < 0) {
            return false;
        }
        //no completion inside CDATA or comment section
        BaseDocument document = (BaseDocument) target.getDocument();
        ((AbstractDocument) document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            if (ts == null) {
                return false;
            }
            ts.move(offset);
            Token token = ts.token();
            if (token == null) {
                ts.moveNext();
                token = ts.token();
                if (token == null) {
                    return false;
                }
            }
            if (token.id() == XMLTokenId.CDATA_SECTION || token.id() == XMLTokenId.BLOCK_COMMENT || token.id() == XMLTokenId.PI_START || token.id() == XMLTokenId.PI_END || token.id() == XMLTokenId.PI_CONTENT || token.id() == XMLTokenId.PI_TARGET) {
                return true;
            }
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
        return false;
    }
    
    /**
     * No instantiation.
     */
    private CompletionUtil() {
    }
        
    /**
     * For debugging purposes only.
     */
    public static void printPath(List<QName> path) {
        StringBuffer buffer = new StringBuffer();
        for(QName item: path) {
            if(buffer.toString().equals(""))
                buffer.append(item);
            else
                buffer.append("/" + item);
        }
        //System.out.println(buffer);
    }
    
    public static boolean isRoot(String tag, CompletionModel cm) {
        if(cm == null)
            return false;
        AXIModel model = AXIModelFactory.getDefault().getModel(cm.getSchemaModel());
        for(AbstractElement element : model.getRoot().getChildElements()) {
            if(tag.endsWith(element.getName()))
                return true;
        }
        return false;
    }        
    
    /**
     * Returns the prefix from the element's tag.
     */
    public static String getPrefixFromTag(String tagName) {
        if (tagName == null) return null;
        int index = tagName.indexOf(":");
        if (index == -1) return null;

        String prefixName = tagName.substring(0, index);
        if (prefixName.startsWith(END_TAG_PREFIX)) {
            prefixName = prefixName.substring(END_TAG_PREFIX.length());
        } else if (prefixName.startsWith(TAG_FIRST_CHAR)) {
            prefixName = prefixName.substring(TAG_FIRST_CHAR.length());
        }
        return prefixName;
    }
    
    /**
     * Returns the local name from the element's tag.
     */
    public static String getLocalNameFromTag(String tagName) {
        if (tagName == null) return null;
        return (tagName.indexOf(":") == -1) ? tagName :
                tagName.substring(tagName.indexOf(":") + 1, tagName.length());
    }
    
    /**
     * Returns any prefix declared with this namespace. For example, if
     * the namespace was declared as xmlns:po, the prefix 'po' will be returned.
     * Returns null for declaration that contains no prefix.
     */
    public static String getPrefixFromXMLNS(String namespace) {
        if (namespace == null) return null;
        return (namespace.indexOf(":") == -1) ? null :
                namespace.substring(namespace.indexOf(":") + 1);
    }
    
    /**
     * Returns the list of prefixes declared against the specified
     * namespace. For example a document may declare namespaces as follows:
     * xmlns="tns" xmlns:a="tns" xmlns:b="tns"
     * The returned list in this case will be [a, b, null]
     */
    public static List<String> getPrefixesAgainstNamespace(
            CompletionContextImpl context, String namespace) {
        List<String> list = new ArrayList<String>();
        
        for(Map.Entry<String, String> entry : context.getDeclaredNamespaces().entrySet()) {
            String key = entry.getKey();
            String ns = entry.getValue();
            if(ns.equals(namespace))
                list.add(getPrefixFromXMLNS(key));
        }
        
        return list;
    }
            
    /**
     * Populates schema URIs from schemaLocation and noNamespaceSchemaLocation
     * attributes of the doc-root. For schemaLocation, uses the 2nd token, where as
     * for the later, uses every token.
     */
    public static void loadSchemaURIs(String schemaLocation, List<URI> uris, Map<String, String> schemaLocationMap) {
        StringTokenizer st = new StringTokenizer(
                schemaLocation.replace("\n", " "), " "); //NOI18N
        while(st.hasMoreTokens()) {
            URI uri = null;
            try {
                String token1 = st.nextToken().trim();
                if(schemaLocationMap == null) {
                    uri = URI.create(token1); //every token is a schema
                    if(uri != null)
                        uris.add(uri);
                    continue;
                }
                if(st.hasMoreTokens()) {
                        String token2 = st.nextToken().trim();
                        uri = URI.create(token2); //every 2nd token is a schema
                        if(uri != null)
                            uris.add(uri);
                        schemaLocationMap.put(token1, token2);
                }
            } catch (Exception ex) {
                continue;
            }
        }
    }
    
    private static AXIComponent findOriginal(AXIComponent c) {
        while (true) {
            switch (c.getComponentType()) {
                case REFERENCE:
                    c = c.getSharedComponent();
                    break;
                case PROXY:
                    c = c.getOriginal();
                    break;
                default:
                    return c;
            }
        }
    }
    
    /**
     * Returns the list of attributes for a given element.
     */
    public static List<CompletionResultItem> getAttributes(
            CompletionContextImpl context) {
        Element element = findAXIElementAtContext(context);        
        if(element == null)
            return null;
        List<CompletionResultItem> results = new ArrayList<CompletionResultItem>();
        for(AbstractAttribute aa: element.getAttributes()) {
            AXIComponent original = findOriginal(aa);
            if(original.getTargetNamespace() == null) {  //no namespace
                CompletionResultItem item = createResultItem(original, null, context);
                if(item != null)
                    results.add(item);
                continue;
            }            
            if(original instanceof AnyAttribute) {
                results.addAll(substituteAny((AnyAttribute)original, context));
                continue;
            }
            addNSAwareCompletionItems(original,context,null,results);
        }
        return results;
    }
    
    /**
     * Returns the list of child elements for a given element. This includes
     * all available substitutions for child elements from all 
     * {@link CompletionModel}s in the specified completion context. It excludes
     * abstract elements
     * @param context the completion context for which to find all valid 
     * elements for insertion
     * @return all elements appropriate to be inserted in the specified context,
     * or {@code null} if no parent element can be found for the specified 
     * context.
     */
    public static List<CompletionResultItem> getElements(CompletionContextImpl context) {
        Element element = findAXIElementAtContext(context);
        if(element == null) {
            return null;
        }
        
        List<CompletionResultItem> results = new ArrayList<CompletionResultItem>();
        for(AbstractElement ae: element.getChildElements()) {
            AXIComponent original = findOriginal(ae);
            if(original.getTargetNamespace() == null) {  //no namespace
                CompletionResultItem item = createResultItem(original, null, context);
                if(item != null) {
                    results.add(item);
                }
            } else if(original instanceof AnyElement) {
                results.addAll(substituteAny((AnyElement)original, context));
            } else if(original instanceof Element) {
                Element childElement = (Element) original;
                if(!childElement.getAbstract()) {
                    addNSAwareCompletionItems(original, context, null, results);
                }
                addSubstitutionCompletionItems(childElement, context, results);
            }
        }
        return results;
    }
    
    public static List<CompletionResultItem> getElementValues(
            CompletionContextImpl context) {
        Element element = findAXIElementAtContext(context);
        List<CompletionResultItem> result = new ArrayList<CompletionResultItem>();
        if(element == null)
            return null;
        AXIType type = element.getType();
        if(!(type instanceof Datatype) ||
           ((Datatype) type).getEnumerations() == null)
            return null;
        for(Object value: ((Datatype)type).getEnumerations()) {
            if(context.getTypedChars() == null || context.getTypedChars().equals("")) {
                ValueResultItem item = new ValueResultItem(element, (String)value, context);
                result.add(item);
                continue;
            }
            String str = (String)value;
            if(str.startsWith(context.getTypedChars())) {
                ValueResultItem item = new ValueResultItem(element, (String)value, context);
                result.add(item);
            }
        }
        return result;
    }    
    
    public static List<CompletionResultItem> getAttributeValues(
            CompletionContextImpl context) {
        Element element = findAXIElementAtContext(context);
        if(element == null)
            return null;        
        List<CompletionResultItem> result = new ArrayList<CompletionResultItem>();
        Attribute attr = null;
        for(AbstractAttribute a: element.getAttributes()) {
            if(a instanceof AnyAttribute)
                continue;
            if(a.getName().equals(context.getAttribute())) {
                attr = (Attribute)a;
                break;
            }
        }
        if(attr == null)
            return null;
        AXIType type = attr.getType();
        if(!(type instanceof Datatype) ||
           ((Datatype) type).getEnumerations() == null)
            return null;                
        for(Object value: ((Datatype)type).getEnumerations()) {
            String str = (value != null) ? value.toString() : null;
            if(context.getTypedChars() == null || context.getTypedChars().equals("")) {
                ValueResultItem item = new ValueResultItem(attr, str, context);
                result.add(item);
                continue;
            }
            if (str != null && str.startsWith(context.getTypedChars())) {
                ValueResultItem item = new ValueResultItem(attr, str, context);
                result.add(item);
            }
        }
        return result;
    }    
        
    /**
     * Find all possible substitutions for the specified {@link Element} within
     * the specified {@link CompletionContextImpl completion context}.
     * @param forSubstitution the Element to find substitutions for.
     * @param context the context to use to find available substitutions. All
     * available {@link CompletionModel}s' {@link SchemaModel}s will be searched.
     * @param results the result set to add the results to.
     */
    private static void addSubstitutionCompletionItems(Element forSubstitution, CompletionContextImpl context, List<CompletionResultItem> results) {
        AXIModel model = forSubstitution.getModel();
        String nsUri = forSubstitution.getTargetNamespace();
        String localName = forSubstitution.getName();
        for (CompletionModel completionModel : context.getCompletionModels()) {
            SchemaModel schemaModel = completionModel.getSchemaModel();
            Set<GlobalElement> substitutions = FindSubstitutions.resolveSubstitutions(schemaModel, nsUri, localName);
            for (GlobalElement substitution : substitutions) {
                AXIComponent substitutionElement = getAxiComponent(model, substitution);
                addNSAwareCompletionItems(substitutionElement, context, completionModel, results);
            }
        }
    }
    
    
    /**
     * Finds the {@link AXIComponent} representing the specified 
     * {@link SchemaComponent}, from the specified {@link AXIModel}.
     * Modified from org.netbeans.modules.xml.axi.impl.Util.lookup(), 
     * org.netbeans.modules.xml.axi.impl.AXIModelImpl.lookup(), 
     * and org.netbeans.modules.xml.axi.impl.AXIModelImpl.lookupFromOtherModel().
     * @param model the model to search in to find the representation of the 
     * specified {@link SchemaComponent}
     * @param schemaComponent the {@link SchemaComponent} to search for a 
     * representation of
     * @return the AXI representation of the specified schema component, or null
     * if no AXI representation could be found for the schema component.
     */
    private static AXIComponent getAxiComponent(AXIModel model, SchemaComponent schemaComponent) {
        if(model.getSchemaModel() == schemaComponent.getModel()) {
            return findChild(model.getRoot(), schemaComponent);
        }
        if(!schemaComponent.isInDocumentModel()) {
            return null;
        }
        AXIModelFactory factory = AXIModelFactory.getDefault();
        AXIModel otherModel = factory.getModel(schemaComponent.getModel());
        return otherModel == null ? null : findChild(otherModel.getRoot(), schemaComponent);
    }
    
    /**
     * Finds an {@link AXIComponent} representation of the specified 
     * {@link SchemaComponent}, by searching the children of the specified
     * {@link AXIDocument}.
     * Adapted from org.netbeans.modules.xml.axi.impl.AXIDocumentImpl.findChild().
     * @param document the document to search through to find the representation
     * of the specified schema component
     * @param child the schema component whose representation to find in the 
     * specified document
     * @return the AXI representation of the specified schema component, from 
     * the specified document, or {@code null} if the schema component has no
     * representation in the children of the document.
     */
    private static AXIComponent findChild(AXIDocument document, SchemaComponent child) {
        for(AXIComponent childRepresentation : document.getChildren()) {
            if(childRepresentation.getPeer() == child) {
                return childRepresentation;
            }
        }
        return null;        
    }
    
    private static void addNSAwareCompletionItems(AXIComponent axi, 
        CompletionContextImpl context, CompletionModel cm, List<CompletionResultItem> results) {
        String typedChars = context.getTypedChars();
        CompletionResultItem item = null;
        if (! isFormQualified(axi)) {
            item = createResultItem(axi, null, context);
            if (item == null)
                return;
            if (typedChars == null) {
                results.add(item);
            } else if (isResultItemTextStartsWith(item, typedChars)) {
                results.add(item);
            }
            return;
        }
        // namespace aware items
        List<String> prefixes = getPrefixes(context, axi, cm);
        if (prefixes.size() == 0) {
           prefixes.add(null);
        }
        for (String prefix: prefixes) {
            item = createResultItem(axi, prefix, context);
            if (item == null)
                continue;
            if (typedChars == null) {
                results.add(item);
            } else if (isResultItemTextStartsWith(item, typedChars)) {
                results.add(item);
            }
        }
    }
    
    private static boolean isResultItemTextStartsWith(CompletionResultItem resultItem, 
        String text) {
        if ((resultItem == null) || (text == null)) return false;

        String resultText = resultItem.getReplacementText();
        int startIndex = 0;
        if (resultText.startsWith(END_TAG_PREFIX) && (! text.startsWith(END_TAG_PREFIX))) {
            startIndex = END_TAG_PREFIX.length();
        } else if (resultText.startsWith(TAG_FIRST_CHAR) &&
                  (! text.startsWith(TAG_FIRST_CHAR))) {
            startIndex = TAG_FIRST_CHAR.length();
        }
        boolean result = resultText.startsWith(text, startIndex);
        return result;
    }

    private static CompletionResultItem createResultItem(AXIComponent axi,
            String prefix, CompletionContextImpl context) {
        CompletionResultItem item = null;
        if(axi instanceof AbstractElement) {
            if(prefix == null)
                item = new ElementResultItem((AbstractElement)axi, context);
            else
                item = new ElementResultItem((AbstractElement)axi, prefix, context);
        }
        
        if(axi instanceof AbstractAttribute) {
            Attribute a = (Attribute)axi;
            if(prefix == null) {
                if(!context.getExistingAttributes().contains(a.getName()))
                    item = new AttributeResultItem((AbstractAttribute)axi, context);
            } else {
                if(!context.getExistingAttributes().contains(prefix+":"+a.getName()))
                    item = new AttributeResultItem((AbstractAttribute)axi, prefix, context);
            }
        }
        
        return item;
    }
    
    private static List<String> getPrefixes(CompletionContextImpl context, AXIComponent ae,
        CompletionModel cm) {
        List<String> prefixes = new ArrayList<String>();
        if(cm == null) {
            ae = findOriginal(ae);
            String defaultNS = context.getDefaultNamespace();
            String targetNS = ae.getTargetNamespace();
            if((defaultNS != targetNS) &&  // in case both are null, go away
               (defaultNS == null || targetNS == null ||
                !context.getDefaultNamespace().equals(ae.getTargetNamespace()))) {
                prefixes = getPrefixesAgainstNamespace(context, ae.getTargetNamespace());
                if(prefixes.size() != 0)
                    return prefixes;
                String prefix = context.suggestPrefix(ae.getTargetNamespace());
                CompletionModel m = new CompletionModelEx(context, prefix,
                    ae.getModel().getSchemaModel());
                context.addCompletionModel(m);
                prefixes.add(prefix); //NOI18N
                return prefixes;
            }
            return getPrefixesAgainstNamespace(context, ae.getTargetNamespace());
        }
        
        prefixes = getPrefixesAgainstNamespace(context, cm.getTargetNamespace());
        if(prefixes.size() == 0)
            prefixes.add(cm.getSuggestedPrefix());
        
        return prefixes;
    }
    
    private static boolean isFormQualified(AXIComponent component) {
        if(component instanceof Attribute) {
            AXIComponent original = component.getOriginal();
            if( ((Attribute)original).isReference() ||
                (original.getParent() instanceof AXIDocument) )
                return true;
            
            Attribute a = (Attribute)component;            
            return (a.getForm() == Form.QUALIFIED);
        }
        
        if(component instanceof Element) {
            AXIComponent original = component.getOriginal();
            if( ((Element)original).isReference() ||
                (original.getParent() instanceof AXIDocument) )
                return true;
            Element e = (Element)component;
            return (e.getForm() == Form.QUALIFIED);
        }
        
        return false;
    }
    
    /**
     * Returns the appropriate AXIOM element for a given context.
     */
    public static Element findAXIElementAtContext(
            CompletionContextImpl context) {
        List<QName> path = context.getPathFromRoot();
        if(path == null || path.size() == 0)
            return null;
        
        CompletionModel cm = null;
        QName tag = context.getPathFromRoot().get(0);
        String tns = tag.getNamespaceURI();
        if(tns != null && tns.equals(XMLConstants.NULL_NS_URI)) {
            cm = context.getActiveNoNSModel();
        } else {
            cm = context.getCompletionModelMap().get(tns);
        }
        if(cm == null)
            return null;
        
        AXIModel am = AXIModelFactory.getDefault().getModel(cm.getSchemaModel());
        AXIComponent parent = am.getRoot();
        if(parent == null)
            return null;
        
        AXIComponent child = null;
        for(QName qname : path) {
            child = findChildElement(parent, qname,context);
            parent = child;
        }
        
        if((child instanceof Element))
            return (Element)child;
        
        return null;
    }
    
    private static AXIComponent findChildElement(AXIComponent parent,
            QName qname, CompletionContextImpl context) {
        if(parent == null) {
            return null;
        }
        for(AXIComponent element : parent.getChildElements()) {
            if(!(element instanceof Element)) {
                continue;
            }
            Element e = (Element)element;
            if(qname.getLocalPart().equals(e.getName())) {
                return element;
            }
             if(e.isReference()){
                Element ref=e.getReferent();
                // check substitutions
                AXIModel model = ref.getModel();
                String nsUri = ref.getTargetNamespace();
                String localName = ref.getName();
                for (CompletionModel completionModel : context.getCompletionModels()) {
                    SchemaModel schemaModel = completionModel.getSchemaModel();
                    Set<GlobalElement> substitutions = FindSubstitutions.resolveSubstitutions(schemaModel, nsUri, localName);
                    for (GlobalElement substitution : substitutions) {
                        AXIComponent substitutionElement = getAxiComponent(model, substitution);
                        if(substitutionElement instanceof Element){
                            if(((Element)substitutionElement).getName().equals(qname.getLocalPart())) {
                                return substitutionElement;
                            }
                        }
                    }
                }
            }
        }
        
        for (AXIComponent c : parent.getChildren()) {
            if (c instanceof SchemaReference) {
                SchemaReference ref = (SchemaReference)c;
                SchemaModelReference in = (SchemaModelReference)ref.getPeer();
                SchemaModel model;
                try {
                    model = in.resolveReferencedModel();
                    AXIModel am = AXIModelFactory.getDefault().getModel(model);
                    AXIComponent check = findChildElement(am.getRoot(), qname, context);
                    if (check != null) {
                        return check;
                    }
                } catch (CatalogModelException ex) {
                    // ignore
                }
            }
        }
        return null;
    }
        
    /**
     * Substitue any or anyAttribute with a valid list of items.
     */
    private static List<CompletionResultItem> substituteAny(AXIComponent any,
            CompletionContextImpl context) {
        List<CompletionResultItem> items = new ArrayList<CompletionResultItem>();
        String anyNamespace = any.getTargetNamespace();
        String tns = any.getModel().getRoot().getTargetNamespace();        
        for (CompletionModel cm : context.getCompletionModels()) {
            if (cm == null) continue;

            //##other => items from other namespaces
            if (anyNamespace.equals("##other")) { //NOI18N
                if ((tns != null) && (! tns.equals(cm.getTargetNamespace()))) {
                    populateItemsForAny(cm, any, context, items);
                }
            }

            //##targetNamespace => items from target namespace
            if(anyNamespace.equals("##targetNamespace")) { //NOI18N
               if(tns != null && tns.equals(cm.getTargetNamespace()))
                populateItemsForAny(cm,any,context,items);
            }
            
            //##local => unqualified items
            if(anyNamespace.equals("##local") && //NOI18N
               cm.getTargetNamespace() == null) {
                populateItemsForAny(cm,any,context,items);
            }
            
            //only specfied namespaces
            if(!anyNamespace.startsWith("##") &&  //NOI18N
               cm.getTargetNamespace() != null &&
               anyNamespace.indexOf(cm.getTargetNamespace()) != -1) {
                populateItemsForAny(cm,any,context,items);
            }
            
            //##any => unconditional
            if(anyNamespace.equals("##any")) { //NOI18N
                populateItemsForAny(cm,any,context,items);
            }
        }
        
        return items;
    }
    
    private static void populateItemsForAny(CompletionModel cm, AXIComponent any,
            CompletionContextImpl context, List<CompletionResultItem> items) {
        if (cm == null) return;

        AXIModel am = AXIModelFactory.getDefault().getModel(cm.getSchemaModel());
        if (any instanceof AnyElement) {
            for(Element e : am.getRoot().getElements()) {
                addNSAwareCompletionItems(e, context, cm, items);
            }
        }
        if (any instanceof AnyAttribute) {
            for(Attribute a : am.getRoot().getAttributes()) {
                addNSAwareCompletionItems(a, context, cm, items);
            }
        }        
    }
    
    /**
     * Finds namespaces declared in all start tags in the document and keeps a map
     * of namespaces to their prefixes.
     * @param document
     * @return
     */
    public static HashMap<String, String> getNamespacesFromStartTags(Document document) {
        HashMap<String, String> map = new HashMap<String, String>();
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            String lastNS = null;
            while(ts.moveNext()) {
                Token t = ts.token();
                if(t.id() == XMLTokenId.ARGUMENT &&
                   t.text().toString().startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                    lastNS = t.text().toString();
                }
                if(t.id() == XMLTokenId.VALUE && lastNS != null) {
                    String value = t.text().toString();
                    if(value.length() >= 2 && (value.startsWith("'") || value.startsWith("\""))) {
                        value = value.substring(1, value.length()-1);
                    }
                    map.put(value, CompletionUtil.getPrefixFromXMLNS(lastNS));
                    lastNS = null;
                }
            } //while loop            
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
        return map;
    }
    
//    private static NsHandler getNamespaces(InputSource is) {
//        NsHandler handler = new NsHandler();
//        try {
//            XMLReader xmlReader = org.openide.xml.XMLUtil.createXMLReader(false, true);
//            xmlReader.setContentHandler(handler);
//            xmlReader.parse(is);
//        } catch (Exception ex) {
//            logger.log(Level.INFO, ex.getMessage());
//        }
//        return handler;
//    }
    
//    private static class NsHandler extends org.xml.sax.helpers.DefaultHandler {
//
//        //key=tns, value=prefix
//        private HashMap<String, String> nsMap;
//
//        NsHandler() {
//            nsMap = new HashMap<String, String>();
//        }
//
//        @Override
//        public void startElement(String uri, String localName, String rawName,
//            Attributes atts) throws SAXException {
//            if (atts.getLength() > 0) {
//                String locations = atts.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
//                    "schemaLocation"); // NOI18N
//                if (locations == null)
//                    return;                
//                StringTokenizer tokenizer = new StringTokenizer(locations);
//                if ((tokenizer.countTokens() % 2) == 0) {
//                    while (tokenizer.hasMoreElements()) {
//                        String nsURI = tokenizer.nextToken();
//                        String nsLocation = tokenizer.nextToken();
//                        //mapping.put(nsURI, nsLocation);
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void startPrefixMapping(String prefix, String uri) throws SAXException {
//            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(uri)) {
//                return;
//            }
//            nsMap.put(uri, prefix);
//        }
//
//        HashMap<String, String> getNamespaceMap() {
//            return nsMap;
//        }
//    }
    
    /**
     * Checks to see if this document declares any DOCTYPE or not?
     * If exists, it must appear before the first xml tag.
     * @return true if found, else false.
     */
    public static boolean isDTDBasedDocument(Document document) {
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            while(ts.moveNext()) {
                Token token = ts.token();
                //if an xml tag is found, we have come too far.
                if(token.id() == XMLTokenId.TAG)
                    return false;
                if(token.id() == XMLTokenId.DECLARATION)
                    return true;
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
        return false;
    }
    
    /**
     * Finds the namespace insertion offset in the xml document.
     */
    public static int getNamespaceInsertionOffset(Document document) {
        int offset = 0;
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            while(ts.moveNext()) {
                Token nextToken = ts.token();
                if(nextToken.id() == XMLTokenId.TAG && nextToken.text().toString().equals(">")) {
                   offset = nextToken.offset(th);
                   break;
                }
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
        
        return offset;
    }
    
    
    /**
     * Finds the root element of the xml document. It also populates the
     * attributes for the root element.
     * 
     * See DocRoot.
     */
    public static DocRoot getDocRoot(Document document) {
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            List<DocRootAttribute> attributes = new ArrayList<DocRootAttribute>();
            String name = null;
            while(ts.moveNext()) {
                Token nextToken = ts.token();
                if(nextToken.id() == XMLTokenId.TAG) {
                    String tagName = nextToken.text().toString();
                    if(name == null && tagName.startsWith("<"))
                        name = tagName.substring(1);
                    String lastAttrName = null;
                    while(ts.moveNext() ) {
                        Token t = ts.token();
                        if(t.id() == XMLTokenId.TAG && t.text().toString().equals(">"))
                            break;
                        if(t.id() == XMLTokenId.ARGUMENT) {
                            lastAttrName = t.text().toString();
                        }
                        if(t.id() == XMLTokenId.VALUE && lastAttrName != null) {
                            String value = t.text().toString();
                            if(value == null || value.length() == 1)
                                value = null;
                            else if(value.startsWith("'") || value.startsWith("\""))
                                value = value.substring(1, value.length()-1);
                            attributes.add(new DocRootAttribute(lastAttrName, value));
                            lastAttrName = null;
                        }
                    } //while loop
                    
                    //first start tag with a valid name is the root
                    if(name != null)
                        break;
                }
            } //while loop
            
            return new DocRoot(name, attributes);
        } finally {
            ((AbstractDocument)document).readUnlock();
        }        
    }
    
    /**
     * Can't provide completion for XML documents based on DTD
     * and for those who do not declare namespaces in root tag.
     */
    public static boolean canProvideCompletion(BaseDocument doc) {
        FileObject file = getPrimaryFile(doc);
        if(file == null)
            return false;
        
        //for .xml documents        
        if("xml".equals(file.getExt())) { //NOI18N
            //if docroot doesn't declare ns, no completion
            DocRoot root = CompletionUtil.getDocRoot(doc);
            if(root != null && !root.declaresNamespace()) {
                return false;
            }
        }
        
        return true;
    }
    
    public static FileObject getPrimaryFile(Document doc) {
        Object o = doc.getProperty(Document.StreamDescriptionProperty);
        if (o instanceof FileObject) {
            return (FileObject) o;
        } else if (o instanceof Lookup.Provider) {
            //Note: DataObject is a Lookup.Provider
            return ((Lookup.Provider) o).getLookup().lookup(FileObject.class);
        } else {
            return null;
        }        
    }

    /*
    private static StringBuilder getTokenSequenceDump(TokenSequence ts) {
        StringBuilder sb = new StringBuilder();
        ts.moveStart();
        int i = 0;
        while (ts.moveNext()) {
            Token token = ts.token();
            sb.append(i + ". [" + token.id() + ", " + ts.offset() + "] ==>\n");
            int tokenLength = token.length();
            String text = token.text().toString();
            text = (text.trim().length() < 1 ? (tokenLength + " ws") : text);
            sb.append(text);
            sb.append("\n<==\n");
            ++i;
        }
        return sb;
    }
    */

    public static CompletionResultItem getEndTagCompletionItem(JTextComponent component,
        BaseDocument document) {
        int caretPos = component.getCaret().getDot();
        try {
            ((AbstractDocument) document).readLock();
            
            TokenHierarchy tokenHierarchy = TokenHierarchy.get(document);
            TokenSequence tokenSequence = tokenHierarchy.tokenSequence();

            String incompleteTagName = findIncompleteTagName(caretPos, tokenSequence);
            if (isCaretInsideTag(caretPos, tokenSequence)) return null;

            boolean beforeUnclosedStartTagFound = isUnclosedStartTagFoundBefore(
                caretPos, tokenSequence);
            if (! beforeUnclosedStartTagFound) return null;

            Token token = tokenSequence.token();
            String startTagName = getTokenTagName(token);
            if (startTagName == null) return null;

            boolean closingTagFound = isClosingEndTagFoundAfter(caretPos,
                tokenSequence, startTagName);
            if (closingTagFound) return null;
            
            CompletionResultItem resultItem;
            if ((incompleteTagName != null) && 
                (! startTagName.startsWith(incompleteTagName))) {
                resultItem = new TagLastCharResultItem(incompleteTagName, tokenSequence);
            } else {
                resultItem = new EndTagResultItem(startTagName, tokenSequence);
            }
            return resultItem;
        } catch(Exception e) {
            _logger.log(Level.WARNING,
                e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
            return null;
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
    }

    private static boolean isUnclosedStartTagFoundBefore(int caretPos,
        TokenSequence tokenSequence) {
        tokenSequence.move(caretPos);

        boolean startTagFound = false, tagLastCharFound = false;
        Stack<String> existingEndTags = new Stack<String>();
        String startTagName, endTagName;

        while (tokenSequence.movePrevious()) {
            Token token = tokenSequence.token();
            if (isTagLastChar(token)) {
                tagLastCharFound = true;
            } else if (isEndTagPrefix(token)) {
                tagLastCharFound = startTagFound = false;
                endTagName = getTokenTagName(token);
                if (endTagName != null) {
                    existingEndTags.push(endTagName);
                }
            } else if (isTagFirstChar(token) && tagLastCharFound) {
                startTagName = getTokenTagName(token);
                endTagName = existingEndTags.isEmpty() ? null : existingEndTags.peek();
                if ((startTagName != null) && (endTagName != null) &&
                    startTagName.equals(endTagName)) {
                    existingEndTags.pop();
                    tagLastCharFound = startTagFound = false;
                    continue;
                }
                startTagFound = true;
                break;
            }
        }
        return startTagFound;
    }

    private static String findIncompleteTagName(int caretPos, TokenSequence tokenSequence) {
        if (! isTokenSequenceUsable(tokenSequence)) return null;

        boolean tagFirstCharFound = false;
        Token token = null;
        String incompleteTagName = null;

        int cut = tokenSequence.move(caretPos);
        tokenSequence.moveNext();
        boolean inMiddle = cut != 0;
        do {
            token = tokenSequence.token();
            TokenId tokenID = token.id();
            if (tokenID.equals(XMLTokenId.TAG) || tokenID.equals(XMLTokenId.TEXT)) {
                String tokenText = token.text().toString();
                if ((tokenText == null) || tokenText.isEmpty()) continue;

                if (tokenText.startsWith(TAG_FIRST_CHAR)) {
                    if (tokenSequence.offset() < caretPos) {
                        tagFirstCharFound = true;
                        incompleteTagName = getTokenTagName(token);
                        break;
                    }
                } else {
                    return null;
                }
            } else if (inMiddle && tokenID.equals(XMLTokenId.BLOCK_COMMENT)) {
                
            }
            inMiddle = false;
        } while (tokenSequence.movePrevious());

        if (! tagFirstCharFound) return null;

        tokenSequence.move(caretPos);
        while (tokenSequence.moveNext()) {
            token = tokenSequence.token();
            TokenId tokenID = token.id();
            if (tokenID.equals(XMLTokenId.TAG)) {
                String tokenText = token.text().toString();
                if ((tokenText == null) || tokenText.isEmpty()) continue;

                if (tokenText.contains(TAG_LAST_CHAR)) {
                    return null;
                } else {
                    return incompleteTagName;
                }
            }
        }
        return incompleteTagName;
    }

    private static boolean isClosingEndTagFoundAfter(int caretPos,
        TokenSequence tokenSequence, String startTagName) {
        if ((tokenSequence == null) || (startTagName == null)) return false;
        tokenSequence.move(caretPos);
        
        int unclosedTagCount = 1;
        while (tokenSequence.moveNext()) {
            Token token = tokenSequence.token();
            String nextTagName = getTokenTagName(token);
            // fix for issue #185048
            // (http://netbeans.org/bugzilla/show_bug.cgi?id=185048)
            // also: must not count ends of nested tags, just the
            // same level
            if (isEndTagPrefix(token)) {
                if (unclosedTagCount-- == 0) {
                    return false;
                }
            } else if (isTagFirstChar(token)) {
                unclosedTagCount++;
            } else {
                continue;
            }
            if (unclosedTagCount == 0) {
                if (isEndTagPrefix(token)) {
                    return startTagName.equals(nextTagName);
                }
            }
        }
        return false;
    }

    public static boolean isTokenSequenceUsable(TokenSequence tokenSequence) {
        return ((tokenSequence != null) && (tokenSequence.isValid()) &&
                (! tokenSequence.isEmpty()));
    }

    public static boolean isCaretInsideTag(int caretPos, TokenSequence tokenSequence) {
        if (! isTokenSequenceUsable(tokenSequence)) return false;

        boolean tagFirstCharFound = false, tagLastCharFound = false;
        Token token = null;

        boolean checkComment = tokenSequence.move(caretPos) != 0;
        tokenSequence.moveNext();
        do {
            token = tokenSequence.token();
            if (isTagFirstChar(token)) {
                tagFirstCharFound = true;
                break;
            } else if (checkComment && token.id() == XMLTokenId.BLOCK_COMMENT) {
                return true;
            }
            checkComment = false;
        } while (tokenSequence.movePrevious());
        
        if (! tagFirstCharFound) return false;

        while (tokenSequence.moveNext()) {
            token = tokenSequence.token();
            int tokenOffset = tokenSequence.offset();
            boolean isEndTagSuffix = isEndTagSuffix(token);
            if (isTagLastChar(token) || isEndTagSuffix) {
                if ((tokenOffset >= caretPos) ||
                    (isEndTagSuffix && (tokenOffset == caretPos - 1))) {
                    tagLastCharFound = true;
                }
                break;
            }
            if (token.id() == XMLTokenId.TAG || token.id() == XMLTokenId.TEXT) {
                if (token.text().charAt(0) == '<') {
                    break;
                }
            }
        }
        return (tagFirstCharFound && tagLastCharFound);
    }

    public static boolean isTagFirstChar(Token token) {
        if (token == null) return false;

        TokenId tokenID = token.id();
        if (tokenID.equals(XMLTokenId.TAG) || tokenID.equals(XMLTokenId.TEXT)) {
            String tokenText = token.text().toString();
            if ((! isEndTagPrefix(token)) && tokenText.startsWith(TAG_FIRST_CHAR)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isTagLastChar(Token token) {
        if (token == null) return false;

        TokenId tokenID = token.id();
        if (tokenID.equals(XMLTokenId.TAG)) {
            String tokenText = token.text().toString();
            if (tokenText.equals(TAG_LAST_CHAR)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEndTagPrefix(Token token) {
        if (token == null) return false;

        TokenId tokenID = token.id();
        if (tokenID.equals(XMLTokenId.TAG) || tokenID.equals(XMLTokenId.TEXT)) {
            String tokenText = token.text().toString();
            if (tokenText.startsWith(END_TAG_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEndTagSuffix(Token token) {
        if (token == null) return false;

        TokenId tokenID = token.id();
        if (tokenID.equals(XMLTokenId.TAG)) {
            String tokenText = token.text().toString();
            if (tokenText.equals(END_TAG_SUFFIX)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTextTag(Token token) {
        if (token == null) return false;

        TokenId tokenID = token.id();
        if (! tokenID.equals(XMLTokenId.TEXT)) return false;

        String tokenText = token.text().toString();
        if (tokenText.equals("<")) {
            return true;
        }
        boolean result = PATTERN_TEXT_TAG_EOLs.matcher(tokenText).matches();
        return result;
    }

    public static String getTokenTagName(Token token) {
        if (token == null) return null;

        int index = -1;
        if (isTagFirstChar(token)) {
            index = TAG_FIRST_CHAR.length();
        } else if (isEndTagPrefix(token)) {
            index = END_TAG_PREFIX.length();
        } else {
            return null;
        }
        String tokenText = token.text().toString().substring(index).trim();
        return tokenText.isEmpty() ? null : tokenText;
    }

    //========================================================================//
    public static class DocRoot {
        //name of the root along with prefix, e.g. po:purchaseOrder
        private String name;
        private List<DocRootAttribute> attributes;
        
        DocRoot(String name, List<DocRootAttribute> attributes) {
            this.name = name;
            this.attributes = new ArrayList(attributes);
        }
        
        public String getName() {
            return name;
        }
        
        public String getPrefix() {
            return CompletionUtil.getPrefixFromTag(name);
        }
        
        public List<DocRootAttribute> getAttributes() {
            return attributes;
        }
        
        public boolean declaresNamespace() {
            for(DocRootAttribute attr: getAttributes()) {
                if(attr.getName().startsWith(XMLConstants.XMLNS_ATTRIBUTE))
                    return true;
            }            
            return false;
        }
    }
    //========================================================================//
    public static class DocRootAttribute {
        private String name;
        private String value;
        
        DocRootAttribute(String name, String value) {
            this.name = name;            
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return name+"="+value;
        }
    }    
}