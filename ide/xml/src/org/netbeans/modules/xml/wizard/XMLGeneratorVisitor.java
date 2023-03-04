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
package org.netbeans.modules.xml.wizard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class XMLGeneratorVisitor extends DeepAXITreeVisitor {
            
    private static final Logger LOG = Logger.getLogger(XMLGeneratorVisitor.class.getName());
    /**
     * Creates a new instance of PrintAXITreeVisitor
     */
    
    private XMLContentAttributes contentAttr;
    private String elemPrefix ="", attrPrefix ="", defaultPrefix;
    private AXIModel axiModel;
    private int depth = 0;
    private String schemaFileName;
    private Element rElement;
    private StringBuffer writer;
    private String primaryTNS;
    Map<String, String> namespaceToPrefix;
    private int counter = 1;
    private static final String PREFIX = "ns"; // NOI18N
    private boolean qualifiedElem;
    /**
     * Stack of elements on the path to the root. Prevents (in)direct recursion.
     */
    private Stack<String> nestingStack = new Stack<String>();
    
    /**
     * Set of elements this machine has decided to include, although minOccurs allowed to skip it.
     */
    private Set<String> machineIncluded = new HashSet<String>();
    
    /**
     * True, if parent compositor can be skipped. Evaluated at compositors, set to true whenever XML is printed
     */
    private boolean parentSkippable;
    
    public XMLGeneratorVisitor(String schemaFileName, XMLContentAttributes attr, StringBuffer writer) {
        super();
        this.contentAttr=attr;

        //this.defaultPrefix = contentAttr.getPrefix() + ":";
        this.defaultPrefix = contentAttr.getPrefix();
        if (defaultPrefix == null) defaultPrefix = "";
        this.defaultPrefix += (defaultPrefix.trim().length() < 1 ? "" : ":");

        this.schemaFileName = schemaFileName;
        this.writer = writer;
        this.namespaceToPrefix = contentAttr.getNamespaceToPrefixMap();
       
    }
    
    //method added for Junit testing
    
   public void generateXML(String rootElement, SchemaModel model){
        if(model.getSchema().getAttributeFormDefaultEffective().equals(Form.QUALIFIED))
            attrPrefix = defaultPrefix;
        if(model.getSchema().getElementFormDefaultEffective().equals(Form.QUALIFIED))
            elemPrefix =defaultPrefix;
        this.axiModel = AXIModelFactory.getDefault().getModel(model);
        rElement = findAXIGlobalElement(rootElement);
        if(rElement != null) {
            primaryTNS = rElement.getTargetNamespace();
            this.visit(rElement);
        }
        contentAttr.setNamespaceToPrefixMap(namespaceToPrefix);
   }
    
    public void generateXML(String rootElement) {        
        //TO DO better exception handling
        if(rootElement == null || schemaFileName == null || schemaFileName.equals("") || rootElement.equals(""))
                return;
        File f = new File(schemaFileName);
        f = FileUtil.normalizeFile(f);
        FileObject fObj =FileUtil.toFileObject(f);
        //temp fix to handle http based xsd files
        if(fObj == null)
            return;
        ModelSource ms = null;
        try {
            ms = Utilities.createModelSource(fObj, true);
        } catch (Exception e){
            //dont do anything
            return;
        }
        if(ms == null)
            return;
        SchemaModel model = SchemaModelFactory.getDefault().getModel(ms);
        if (model.getSchema() == null) {
            return;
        }
        generateXML(rootElement, model);
        
    }
    
    public void generateXML(Element element) {
         if(element != null) {
            this.visit(element);
        }
    }

    @Override
    public void visit(Element element) { 
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Processing: " + getTab() + element);
        }
       int occurs = getOccurence(element, element.getMinOccurs(), element.getMaxOccurs());
       
        //do we need to generate optional elements
       if( !contentAttr.generateOptionalElements() ) {
           if(isElementOptional(element))
               return;
       }
        String elementId = getElementId(element);
        nestingStack.add(elementId);
        try {
            for(int i=0; i < occurs ; i++) {
                visitChildren(element);
            }
        } finally {
            nestingStack.remove(elementId);
        }
    }
    
    private boolean blockExpansion;
    
    @Override
    protected void visitChildren(AXIComponent component) {
       boolean saveBlockExpansion = blockExpansion;
       boolean saveSkippable = parentSkippable;
        try {
            // will print an element, content is not skippable for children
            parentSkippable = false;
            printModel(component);
            depth++;
            this.visitChildrenForXML(component);
            this.postVisitChildren(component);
        } catch (Exception e) {
            //need to figure out how to handle this exception
        } finally {
            parentSkippable = saveSkippable;
            blockExpansion = saveBlockExpansion;
            depth--;
        }
    }

    private boolean isElementOptional(Element element) {
        int i = Integer.parseInt(element.getMinOccurs());
        if(i ==0)
            return true;
        else
            return false;
    }

            
    private void printModel(AXIComponent component) throws IOException {
        StringBuffer buffer = new StringBuffer();
        boolean newLine = true;
        if(component.getChildElements().isEmpty())
            newLine = false;
            
        if(component instanceof Compositor) {
            Compositor compositor = (Compositor)component;
            buffer.append((getTab() == null) ? compositor : getTab() + compositor);
            buffer.append("<min=" + compositor.getMinOccurs() + ":max=" + compositor.getMaxOccurs() + ">");
            return;
        }
        if(component instanceof Element) {
            Element element = (Element)component;
            
            //set prefix
            String prefix = setPrefixForElement(element);
            
            //dont print the root element 
            if (element.equals(rElement)) {
                //check if root element has attributes;
                if (element.getAttributes().size() != 0) {
                    int i = writer.lastIndexOf("\n");
                    if (i != -1) {
                        writer = writer.insert(i - 1, " " + getAttributes(element));
                    }
                }
                return;
            }
            
            buffer.append((getTab() == null) ? element.getName() : getTab() + "<" + prefix  +element.getName() );
            if(element.getAttributes().size() != 0) {
                buffer.append(" " + getAttributes(element) );
            }
           if(newLine)
                writer.append(buffer.toString() +">" +"\n");
            else
                writer.append(buffer.toString() + ">");
            
            //write the default/fixed value of the element, if any
            writer.append(getComponentValue(element));
        }
        
        
    }
        
    
    private String getAttributes(Element element) {
        String lprefix;
        StringBuffer attrs = new StringBuffer();
        for(AbstractAttribute attr : element.getAttributes()) {
            lprefix = attrPrefix;
            if(isGlobal(attr))
               lprefix = contentAttr.getPrefix() + ":";
            if(attr instanceof Attribute) {
                if(!contentAttr.generateOptionalAttributes()){ 
                   if(((Attribute)attr).getUse().equals(Use.REQUIRED)){
                        attrs.append(lprefix + attr+ "=\"" + getComponentValue((Attribute)attr) + "\" ");
                    }
                    continue;
                }
            }
            if(attr instanceof Attribute)
                attrs.append(lprefix + attr+ "=\"" + getComponentValue((Attribute)attr) + "\" ");
            else
                attrs.append(attr+"= \" \" ");            
        }
        if(attrs.length() > 0)
            return attrs.toString().substring(0, attrs.length()-1);
        else
            return attrs.toString();
    }
    
    private String getTab() {
        String tabStr = "    ";
        
        if(depth == 0) {
            return null;
        }
        
        StringBuffer tab = new StringBuffer();
        for(int i=0; i<depth ; i++) {
            tab.append(tabStr);
        }
        return tab.toString();
    }
    
    protected void visitChildrenForXML(AXIComponent component) {
        if( !super.canVisit(component) )
            return;
                
        if(component instanceof Compositor) {
            
           // save the current minoccurs
           Compositor.CompositorType type =((Compositor)component).getType();
           String minOccurs = ((Compositor)component).getMinOccurs();
           boolean canSkip = parentSkippable;
           boolean saveSkippable = parentSkippable;
           if (minOccurs != null) {
               canSkip = parentSkippable || Integer.parseInt(minOccurs) == 0;
           }
           if(type.equals(Compositor.CompositorType.CHOICE) ){
               List<AXIComponent> children = component.getChildren();
               if (children != null) {
                   for (AXIComponent axiCo : children) {
                       String id = getElementId(axiCo);
                       if (id == null || (!nestingStack.contains(id) && !machineIncluded.contains(id))) {
                           if (canSkip) {
                               // can skip the item, but we deliberately include it -> record machine decision
                               machineIncluded.add(id);
                           }
                           this.parentSkippable = canSkip;
                           try {
                                axiCo.accept(this);
                           } finally {
                               parentSkippable = saveSkippable;
                           }
                           break;
                       }
                       // the axiCo element should not printed - it's a parent of the
                       // current element, or already included by min/max/choice decision, no need
                       // to repeat it.
                       if (canSkip) {
                           break;
                       }
                   }
               }
               return;
           }           
        }
                
        for(AXIComponent child: component.getChildren()) {
            child.accept(this);
        }
        
    }
    
    private String getElementId(AXIComponent co) {
        if (!(co instanceof Element)) {
            return null;
        }
        Element e = (Element)co;
        String ns = e.getTargetNamespace();
        if (ns == null) {
            return e.getName();
        } else {
            return ns + ":" + e.getName();
        }
    }
    
    private int getOccurence(Element el, String minOccurs, String maxOccurs) {
        int min = Integer.parseInt(minOccurs);
        String elementId = getElementId(el);
        boolean nestedIn = nestingStack.contains(elementId);
        boolean alreadyIncluded = machineIncluded.contains(elementId);
        
        boolean minimize = (nestedIn || alreadyIncluded || blockExpansion);
        if (minimize && min == 0) {
            return 0;
        }
        if(maxOccurs.equals("unbounded")) {
            if (minimize) {
                return min;
            } else {
                // exception, we choose a # of elements, record as machine decision
                machineIncluded.add(getElementId(el));
                return contentAttr.getPreferredOccurences();
            }
        }
        int max = Integer.parseInt(maxOccurs);
        
        if(contentAttr.getPreferredOccurences() > min && contentAttr.getPreferredOccurences() <max) {
            blockExpansion = true;
            if (min == 0) {
                machineIncluded.add(getElementId(el));
            }
            return contentAttr.getPreferredOccurences();
        }
        
        if(contentAttr.getPreferredOccurences() > max)
            return max;
        
        if(contentAttr.getPreferredOccurences() < min )
            return min;
        
        return min;
    }
    
    private void postVisitChildren(AXIComponent component) throws IOException {
        if(component instanceof Element) {
            //dont write the closing root element
            if( ((Element)component).equals(rElement))
                return;
            
             //set prefix
            String prefix  = setPrefixForElement((Element)component);
            
            if(component.getChildElements().isEmpty())
                writer.append("</" + prefix +((Element)component).getName() + ">" + "\n");
            else
               writer.append(getTab() + "</" + prefix +((Element)component).getName() + ">" + "\n");
        }
    }
    
     private Element findAXIGlobalElement(String name) {
        if(name == null)
            return null;
        
        for(Element e : axiModel.getRoot().getElements()) {
            if(e.getName().equals(name)) {
                return e;
            }
        }
        
        return null;
    }
     
   private String getComponentValue(AXIComponent component) {
       String value = null;
       if(component instanceof Attribute ) {
           Attribute attribute = (Attribute)component;
           value = attribute.getFixed();
           if(value == null)
               value = attribute.getDefault();
       } else if(component instanceof Element) {
           Element element =(Element)component;
           value = element.getFixed();
           if(value == null)
               value = element.getDefault();    
       }
       
       if(value != null)
           return value;   
       else
           return "";
       
    }

    private String generatePrefix(){
        String generatedName = PREFIX + counter++;
        while(namespaceToPrefix.containsValue(generatedName) )
            generatedName = PREFIX + counter++;
        return generatedName;
    
    }
    
    private String setPrefixForElement(Element element ){
        String prefix = elemPrefix;
        String ns;
        
        if(element.isReference())
            ns = element.getReferent().getTargetNamespace();
        else
            ns = element.getTargetNamespace();
        
        if(ns == null)
            return prefix ;
        
        if(! ns.equals(primaryTNS)) {
               if(namespaceToPrefix == null)
                    namespaceToPrefix = new HashMap<String, String>();
                
                      
                String pre = namespaceToPrefix.get(ns);
                if(pre == null || pre.equals("")) {
                    pre = generatePrefix();
                    namespaceToPrefix.put(ns, pre);
                }
                prefix = pre + ":";
                return prefix;
        } 
        if(isGlobal(element)){
            return defaultPrefix;
        } 
        
        return prefix;  
         
    }
    
    private boolean isGlobal(AXIComponent component) {
      AXIComponent original = component.getOriginal();
      if (original.getComponentType() == ComponentType.REFERENCE) {
          return true;
      }
      return original.isGlobal();
  } 
   
}
