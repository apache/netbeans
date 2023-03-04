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

package org.netbeans.modules.xml.axi.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.netbeans.modules.xml.axi.datatype.CustomDatatype;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.axi.SchemaGenerator.PrimitiveCart;
import org.netbeans.modules.xml.axi.SchemaGenerator.UniqueId;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.visitor.AXINonCyclicVisitor;

/**
 *
 * @author Ayub Khan
 */
public abstract class DefaultSchemaGenerator extends SchemaGenerator {
    
    protected AXIModel am;
    
    protected SchemaModel sm;
    
    protected SchemaGenerator.UniqueId id;
    
    protected SchemaGenerator.PrimitiveCart pc;
    
    java.util.List<AXIComponent> path = new ArrayList<AXIComponent>();
    
    protected SchemaComponent sc;
    
    protected SchemaComponent scParent;
    
    protected SchemaComponent datatypeParent;
    
    protected SortedMap<Integer, java.util.List<Object>> fixNamesMap = null;
    
    protected java.util.List<SchemaComponent> createGlobals = null;
    
    protected HashMap<SchemaComponent, SchemaComponent> refMap = null;
    
    protected HashMap<Class, HashMap<String, SchemaComponent>> namesMap = null;
    
    protected java.util.List<Element> elementReuseList = null;
    
    protected SchemaGeneratorHelper sgh;
    
    private int fgeCount;
    
    public static final int commitRange = Integer.getInteger("schematools.axi.adp", 0);
    
    /**
     * Creates a new instance of DefaultSchemaGenerator
     */
    public DefaultSchemaGenerator(SchemaGenerator.Mode mode) {
        super(mode);
        id = createUniqueId();
        pc = createPrimitiveCart();
        fixNamesMap = new TreeMap<Integer, java.util.List<Object>>();
        createGlobals = new ArrayList<SchemaComponent>();
        refMap = new HashMap<SchemaComponent, SchemaComponent>();
        namesMap = new HashMap<Class, HashMap<String, SchemaComponent>>();
        elementReuseList = new ArrayList<Element>();
        fgeCount = 0;
    }
    
    public void updateSchema(SchemaModel sm) throws IOException {
        assert getMode() == SchemaGenerator.Mode.UPDATE;
        sgh = new UpdateHelper();
        sgh.execute(sm);
    }
    
    public void transformSchema(SchemaModel sm) throws IOException {
        assert getMode() == SchemaGenerator.Mode.TRANSFORM;
        sgh = new TransformHelper();
        sgh.execute(sm);
    }
    
    public void visit(Element element) {
        if(getMode() != SchemaGenerator.Mode.TRANSFORM && element instanceof ElementRef)
            prepareElementRef((ElementRef)element);
        else
            prepareLocalElement(element);
    }
    
    public void visit(Attribute attribute) {
        if(getMode() != SchemaGenerator.Mode.TRANSFORM && attribute instanceof AttributeRef)
            prepareAttributeRef((AttributeRef)attribute);
        else
            prepareLocalAttribute(attribute);
    }    
    
    public void visit(AXIType type) {
        if(type instanceof Datatype)
            ((Datatype)type).accept(this);
    }
    
    public void visit(Datatype d) {
        SchemaGeneratorUtil.createInlineSimpleType(d, sm, this.datatypeParent, pc);
    }
    
    public void visit(ContentModel cm) {
        if(scParent instanceof Schema) {
            GlobalComplexType gct = SchemaGeneratorUtil.createGlobalComplexType(sm);
            assert gct != null;
            gct.setName(cm.getName());
            SchemaGeneratorUtil.populateContentModel(gct, cm);
            if(getMode() != SchemaGenerator.Mode.TRANSFORM) {
                cm.setPeer(gct);
                int index = cm.getIndex(false);
                SchemaGeneratorUtil.addChildComponent(sm, sm.getSchema(), gct, index);
            } else
                scParent = gct;
            scParent = gct;
            for(AXIComponent child: cm.getChildren()) {
                child.accept(this);
            }
        }
    }

    public void visit(Compositor compositor) {
        int index = -1;
        if(getMode() != SchemaGenerator.Mode.TRANSFORM)
            index = compositor.getIndex(false);
        switch(compositor.getType()) {
            case SEQUENCE: {
                Sequence seq = null;
                if(scParent instanceof ComplexType) {
                    if(scParent instanceof LocalComplexType) {
                        LocalComplexType lct = (LocalComplexType) scParent;
                        if(lct.getDefinition() != null) {
                            ComplexTypeDefinition ctd = lct.getDefinition();
                            if(ctd instanceof SimpleContent)   {
                                transformToComplexContent(compositor, ctd);
                                seq = SchemaGeneratorUtil.createSequence(sm, lct);
                            } else
                                seq = SchemaGeneratorUtil.createSequence(sm, ctd, index);
                        } else
                            seq = SchemaGeneratorUtil.createSequence(sm,
                                    (LocalComplexType) scParent);
                    } else if(scParent instanceof GlobalComplexType) {
                        GlobalComplexType gct = (GlobalComplexType) scParent;
                        if(gct.getDefinition() != null) {
                            ComplexTypeDefinition ctd = gct.getDefinition();
                            if(ctd instanceof SimpleContent) {
                                transformToComplexContent(compositor, ctd);
                                seq = SchemaGeneratorUtil.createSequence(sm, gct);
                            } else
                                seq = SchemaGeneratorUtil.createSequence(sm, ctd, index);
                        } else
                            seq = SchemaGeneratorUtil.createSequence(sm,
                                    (GlobalComplexType) scParent);
                    }
                } else if(scParent instanceof ComplexContentDefinition) {
                    ComplexContentDefinition ccd = (ComplexContentDefinition) scParent;
                    if(ccd instanceof ComplexContentRestriction &&
                            ((ComplexContentRestriction)ccd).getDefinition() != null)
                        seq = SchemaGeneratorUtil.createSequence(sm,
                                ((ComplexContentRestriction)ccd).getDefinition(), index);
                    //TODO
//			else if(ccd instanceof ComplexExtension &&
//					((ComplexExtension)ccd).getLocalDefinition() != null)
//				seq = ((ComplexExtension)ccd).getLocalDefinition();
                    else
                        seq = SchemaGeneratorUtil.createSequence(sm,
                                (ComplexContentDefinition) scParent);
                } else if(scParent instanceof Sequence) {
                    seq = SchemaGeneratorUtil.createSequence(sm, (Sequence)scParent, index);
                } else if(scParent instanceof Choice) {
                    seq = SchemaGeneratorUtil.createSequence(sm, (Choice)scParent, index);
                }
                SchemaGeneratorUtil.populateCompositor(seq, compositor);
                if(getMode() != SchemaGenerator.Mode.TRANSFORM)
                    compositor.setPeer(seq);
                else
                    scParent = seq;
            }
            break;
            
            case CHOICE: {
                Choice c = null;
                if(scParent instanceof ComplexType) {
                    if(scParent instanceof LocalComplexType) {
                        LocalComplexType lct = (LocalComplexType) scParent;
                        if(lct.getDefinition() != null) {
                            ComplexTypeDefinition ctd = lct.getDefinition();
                            if(ctd instanceof SimpleContent)   {
                                transformToComplexContent(compositor, ctd);
                                c = SchemaGeneratorUtil.createChoice(sm, lct);
                            } else
                                c = SchemaGeneratorUtil.createChoice(sm, ctd, index);
                        } else
                            c = SchemaGeneratorUtil.createChoice(sm,
                                    (LocalComplexType) scParent);                    
                    }
                    if(scParent instanceof GlobalComplexType) {
                        GlobalComplexType gct = (GlobalComplexType) scParent;
                        if(gct.getDefinition() != null) {
                            ComplexTypeDefinition ctd = gct.getDefinition();
                            if(ctd instanceof SimpleContent) {
                                transformToComplexContent(compositor, ctd);
                                c = SchemaGeneratorUtil.createChoice(sm, gct);
                            } else
                                c = SchemaGeneratorUtil.createChoice(sm, ctd, index);
                        }
                        else
                            c = SchemaGeneratorUtil.createChoice(sm,
                                    (GlobalComplexType) scParent);
                    }
                } else if(scParent instanceof ComplexContentDefinition) {
                    ComplexContentDefinition ccd = (ComplexContentDefinition) scParent;
                    if(ccd instanceof ComplexContentRestriction &&
                            ((ComplexContentRestriction)ccd).getDefinition() != null)
                        c = SchemaGeneratorUtil.createChoice(sm,
                                ((ComplexContentRestriction)ccd).getDefinition(), index);
                    //TODO
//			else if(ccd instanceof ComplexExtension &&
//					((ComplexExtension)ccd).getLocalDefinition() != null)
//				seq = ((ComplexExtension)ccd).getLocalDefinition();
                    else
                        c = SchemaGeneratorUtil.createChoice(sm,
                                (ComplexContentDefinition) scParent);
                } else if(scParent instanceof Choice) {
                    c = SchemaGeneratorUtil.createChoice(sm, (Choice)scParent, index);
                } else if(scParent instanceof Sequence) {
                    c = SchemaGeneratorUtil.createChoice(sm, (Sequence)scParent, index);
                }
                SchemaGeneratorUtil.populateCompositor(c, compositor);
                if(getMode() != SchemaGenerator.Mode.TRANSFORM)
                    compositor.setPeer(c);
                else
                    scParent = c;
            }
            break;
            
            case ALL: {
                All a = null;
                if(scParent instanceof ComplexType) {
                    a = SchemaGeneratorUtil.createAll(sm, (ComplexType) scParent);
                } else if(scParent instanceof ComplexContentDefinition) {
                    ComplexContentDefinition ccd = (ComplexContentDefinition) scParent;
                    if(ccd instanceof ComplexContentRestriction &&
                            ((ComplexContentRestriction)ccd).getDefinition() != null)
                        a = (All)
                        ((ComplexContentRestriction)ccd).getDefinition();
                    //TODO
//			else if(ccd instanceof ComplexExtension &&
//					((ComplexExtension)ccd).getLocalDefinition() != null)
//				seq = ((ComplexExtension)ccd).getLocalDefinition();
                    else
                        a = SchemaGeneratorUtil.createAll(sm,
                                (ComplexContentDefinition) scParent);
                }
                SchemaGeneratorUtil.populateCompositor(a, compositor);
                if(getMode() != SchemaGenerator.Mode.TRANSFORM)
                    compositor.setPeer(a);
                else
                    scParent = a;
            }
            break;
            default: assert false;
        }
    }
    
    private ComplexType transformToComplexContent(Compositor compositor, final ComplexTypeDefinition ctd) {
        AXIComponent parent = compositor.getParent();
        ComplexType lct = null;
        if(ctd instanceof SimpleContent) {
            lct = (ComplexType) ctd.getParent();
            if(ctd.getChildren().get(0) instanceof SimpleExtension) {
                NamedComponentReference base = ((SimpleExtension)ctd.getChildren().get(0)).getBase();                
                if(base != null && base.get() instanceof GlobalSimpleType) {
                    SimpleContent sc1 = (SimpleContent) ctd;                    
                    copyAttribute(parent, lct, sc1);
                    copyAttribute(parent, lct, sc1.getLocalDefinition());
                }
            }
        }
        return lct;
    }

    private void copyAttribute(AXIComponent parent, final LocalAttributeContainer lac, 
            final SchemaComponent scd) {
        for(SchemaComponent sc: scd.getChildren()) {
            if(sc instanceof org.netbeans.modules.xml.schema.model.Attribute) {
                LocalAttribute la = (LocalAttribute)((org.netbeans.modules.xml.schema.model.Attribute)sc).copy(lac);
                lac.addLocalAttribute(la);
                for(AXIComponent child : parent.getChildren()) {
                    if(child.getPeer() == sc) {
                        child.setPeer(la);
                    }
                }                
            }
        }
    }
    
    public void visit(AXIComponent c) {
        throw new IllegalArgumentException("No action taken on this component: "+
                c.toString());
    }
    
    protected abstract SchemaGenerator.Pattern getSchemaDesignPattern();
    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////// helper methods   ////////////////////////
    ////////////////////////////////////////////////////////////////////
    
    protected void prepareGlobalElement(Element element) {
        GlobalElement e = null;
        ElementReference eref = null;
        int index = -1;
        if(getMode() != SchemaGenerator.Mode.TRANSFORM)
            index = element.getIndex(false);
        if(scParent instanceof Schema) {
            e = createGlobalElement(element);
            sgh.addElement(e, index);
            prepareFixGlobalElementNames(element, (GlobalElement) e, null);
        } else if(scParent instanceof ComplexTypeDefinition) {
            String seed = element.getName();
            
            boolean found = false;
            if(SchemaGeneratorUtil.isSimpleElement(element) ||
                    SchemaGeneratorUtil.hasProxyChild(element)) {
                HashMap<String, SchemaComponent> map =
                        namesMap.get(GlobalElement.class);
                if(map != null && map.get(seed) != null) {
                    GlobalElement ge1 = (GlobalElement) map.get(seed);
                    
                    GlobalElement ge2 = SchemaGeneratorUtil.createGlobalElement(sm);
                    ge2.setName(element.getName());
                    SchemaGeneratorUtil.populateElement(ge2, element);
                    this.datatypeParent = ge2;
                    if(element.getType() instanceof Datatype)
                        element.getType().accept(this);
                    else if(element.getType() instanceof ContentModel)
                        SchemaGeneratorUtil.setType(ge2,
                                (GlobalComplexType) ((ContentModel)element.
                                getType()).getPeer());
                    
                    if(SchemaGeneratorUtil.isIdentical(ge1, ge2)) {
                        found = true;
                        e = ge1;
                        if(!elementReuseList.contains(element))
                            elementReuseList.add(element);
                    }
                }
            }
            if(!found) {
                e = createGlobalElement(element);
                sgh.addElement(e, -1);
            }
            eref = SchemaGeneratorUtil.createElementReference(sm, scParent, e, index);
            addRef(eref, e);
            prepareFixGlobalElementNames(element, e, eref);
        }
        assert e != null;
        sc = e;
        
        //set block, final, fixed, default, form etc
        SchemaGeneratorUtil.populateElement(e, element);
        
        if(eref != null)
            SchemaGeneratorUtil.populateElement(eref, element);
        
        this.datatypeParent = e;
        if(element.getType() instanceof Datatype)
            element.getType().accept(this);
        
        setPeer(element, e, eref);
    }
    
    protected void prepareElementRef(ElementRef element) {
        int index = element.getIndex();
        org.netbeans.modules.xml.schema.model.ElementReference eRef =
                SchemaGeneratorUtil.createElementReference(sm, (ComplexTypeDefinition) scParent,
                (GlobalElement)element.getReferent().getPeer(), index);
        setPeer(element, null, eRef);
    }
    
    protected void prepareLocalElement(Element element) {        
        org.netbeans.modules.xml.schema.model.Element e = null;
        int index = -1;
        if(getMode() != SchemaGenerator.Mode.TRANSFORM)
            index = element.getIndex(false);
        if(scParent instanceof Schema) {
            e = createGlobalElement(element);
            sgh.addElement((GlobalElement) e, index);
            prepareFixGlobalElementNames(element, (GlobalElement) e, null);
        } else if(scParent instanceof ComplexTypeDefinition) {           
            e = SchemaGeneratorUtil.createLocalElement(sm,
                    (ComplexTypeDefinition) scParent, element.getName(), index);
        }
        assert e != null;
        sc = e;
        
        //set block, final, fixed, default, form etc
        SchemaGeneratorUtil.populateElement(e, element);
        
        this.datatypeParent = e;
        if(element.getType() instanceof Datatype)
            element.getType().accept(this);
        setPeer(element, e, null);
    }
    
    protected void prepareAttributeRef(AttributeRef attribute) {
        int index = attribute.getIndex();
        org.netbeans.modules.xml.schema.model.AttributeReference aRef =
                SchemaGeneratorUtil.createAttributeReference(sm, scParent,
                (GlobalAttribute)attribute.getReferent().getPeer(), index);
        setPeer(attribute, null, aRef);
    }

    protected void prepareLocalAttribute(Attribute attribute) {
        assert scParent != null;
        int index = -1;
        if(getMode() != SchemaGenerator.Mode.TRANSFORM)
            index = attribute.getIndex();
        LocalAttribute attr = null;
        if(scParent instanceof ComplexType && 
                ((ComplexType)scParent).getDefinition() instanceof ComplexTypeDefinition) {
            if(((ComplexType)scParent).getDefinition() instanceof SimpleContent) {
                SimpleContentDefinition def = ((SimpleContent)((ComplexType)scParent).
                        getDefinition()).getLocalDefinition();
                if(def instanceof SimpleContentRestriction)
                    attr = SchemaGeneratorUtil.createLocalAttribute(sm, attribute.getName(),
                        (SimpleContentRestriction)def, index);
                else if(def instanceof Extension)
                    attr = SchemaGeneratorUtil.createLocalAttribute(sm, attribute.getName(),
                        (Extension)def, index);
            }
            else if(((ComplexType)scParent).getDefinition() instanceof ComplexContent) {
                ComplexContentDefinition def = ((ComplexContent)((ComplexType)scParent).
                        getDefinition()).getLocalDefinition();
                if(def instanceof ComplexContentRestriction)
                    attr = SchemaGeneratorUtil.createLocalAttribute(sm, attribute.getName(),
                        (ComplexContentRestriction)def, index);
                else if(def instanceof Extension)
                    attr = SchemaGeneratorUtil.createLocalAttribute(sm, attribute.getName(),
                        (Extension)def, index);
            }
            else {
                attr = SchemaGeneratorUtil.createLocalAttribute(sm, attribute.getName(),
                    scParent, index);
            }
        } else
            attr = SchemaGeneratorUtil.createLocalAttribute(sm, attribute.getName(),
                scParent, index);
        assert attr != null;
        
        //set fixed, default, form, use etc
        SchemaGeneratorUtil.populateAttribute(attr, attribute);
        
        this.datatypeParent = attr;
        if(attribute.getType() instanceof Datatype)
            attribute.getType().accept(this);
        
        if(getMode() != SchemaGenerator.Mode.TRANSFORM)
            attribute.setPeer(attr);        
    }
    
    protected GlobalElement createGlobalElement(final Element element) {
        GlobalElement ge = SchemaGeneratorUtil.createGlobalElement(sm);
        String eName = findUniqueGlobalName(
                GlobalElement.class, ge, element.getName());
        ge.setName(eName);
        return ge;
    }
    
    protected GlobalComplexType createGlobalComplexType(final String seed) {
        GlobalComplexType gct;
        gct = (GlobalComplexType)
        SchemaGeneratorUtil.createGlobalComplexType(sm);
        String typeName = findUniqueGlobalName(
                GlobalComplexType.class, gct, seed);
        gct.setName(typeName);
        return gct;
    }
    
    public void createGlobalSimpleType(
            final Datatype d, final SchemaModel sm, final SchemaComponent sc,
            final SchemaGenerator.UniqueId id, SchemaGenerator.PrimitiveCart pc) {
        if(d != null) {
            NamedComponentReference<GlobalSimpleType> ref =null;
            if(SchemaGeneratorUtil.isPrimitiveType(d)) {
                ref = SchemaGeneratorUtil.createPrimitiveType(d, sc, pc);
            } else {
                GlobalSimpleType gst;
                gst = SchemaGeneratorUtil.createGlobalSimpleType(sm);
                String typeName = d.getName();
                typeName = findUniqueGlobalName(
                        GlobalSimpleType.class, gst,
                        "New"+typeName.substring(0, 1).toUpperCase()+
                        typeName.substring(1)+"Type"+String.valueOf(id.nextId()));
                gst.setName(typeName);
                sgh.addSimpleType(gst, -1);
                if(d instanceof CustomDatatype)
                    SchemaGeneratorUtil.populateSimpleType(
                            ((CustomDatatype)d).getBase(), sm, gst, pc);
                else
                    SchemaGeneratorUtil.populateSimpleType(d, sm, gst, pc);
                ref = sc.createReferenceTo(gst, GlobalSimpleType.class);
            }
            SchemaGeneratorUtil.setSimpleType(sc, ref);
        }
    }
    
    protected GlobalType createPeerGlobalComplexType(Element element) {
        org.netbeans.modules.xml.schema.model.Element e =
                (org.netbeans.modules.xml.schema.model.Element) element.getPeer();
        if(e instanceof ElementReference)
            e = (GlobalElement) getRef(e);
        GlobalComplexType gct = createGlobalComplexType(element.getName()+"Type");
        assert gct != null;
        sgh.addComplexType(gct, -1);
        SchemaGeneratorUtil.setType(e, gct);
        return gct;
    }
    
    protected void prepareFixGlobalElementNames(final Element element, final GlobalElement e,
            final ElementReference eref) {
        java.util.List<Object> scs = new ArrayList<Object>();
        scs.add(element);
        scs.add(e);
        scs.add(eref);
        fixNamesMap.put(Integer.valueOf(fgeCount++), scs);
    }
    
    protected void fixGlobalElementNames() {
        //clear unique names map
        namesMap.clear();
        
        //create buckets
        HashMap<GlobalElement, java.util.List<ElementReference>> erefMap = new
                HashMap<GlobalElement, java.util.List<ElementReference>>();
        for (Entry<Integer,java.util.List<Object>> e : fixNamesMap.entrySet()) {
            java.util.List<Object> scs = e.getValue();
            if(scs != null && scs.size() > 1) {
                GlobalElement ge = (GlobalElement) scs.get(1);
                ElementReference eref = (ElementReference) scs.get(2);
                java.util.List<ElementReference> erefs = erefMap.get(ge);
                if(erefs == null) {
                    erefs = new ArrayList<ElementReference>();
                    erefMap.put(ge, erefs);
                }
                if(eref != null && !erefs.contains(eref))
                    erefs.add(eref);
            }
        }
        
        int count = 0;
        Iterator it = erefMap.keySet().iterator();
        while(it.hasNext()) {
            if(commitRange > 0 && (count++)%commitRange==0) {
                sm.endTransaction();
                sm.startTransaction();
            }
            GlobalElement ge = (GlobalElement) it.next();
            java.util.List<ElementReference> erefs = erefMap.get(ge);
            String name = findUniqueGlobalName(
                    GlobalElement.class, ge, ge.getName());
            ge.setName(name);

            for(ElementReference eref:erefs)
                eref.setRef(eref.createReferenceTo(ge, GlobalElement.class));
        }
        
        erefMap.clear();
        erefMap = null;
        fixNamesMap.clear();
    }
    
    <T extends NameableSchemaComponent>String
            findUniqueGlobalName(Class<T> type, NameableSchemaComponent c,
            final String seed) {
        return sgh.findUniqueGlobalName(type, c, seed);
    }
    
    protected SchemaComponent getParent(final AXIComponent axiparent)
    throws IllegalArgumentException {
        return sgh.getParent(axiparent);
    }
    
    protected void setPeer(final Element element,
            final org.netbeans.modules.xml.schema.model.Element e,
            final ElementReference eref) {
        sgh.setPeer(element, e, eref);
    }
    
    protected void setPeer(final Attribute attribute,
            final org.netbeans.modules.xml.schema.model.Attribute a,
            final AttributeReference aRef) {
        sgh.setPeer(attribute, a, aRef);
    }
    
    private UniqueId createUniqueId() {
        return new UniqueId() {
            private int lastId = -1;
            public int nextId() {
                return ++lastId;
            }
        };
    }
    
    private PrimitiveCart createPrimitiveCart() {
        return new PrimitiveCart() {
            private Map<SchemaComponent, Datatype> pc = new HashMap<SchemaComponent, Datatype>();
            private Map<String, GlobalSimpleType> ptypes = new HashMap<String, GlobalSimpleType>();
            private GlobalSimpleType def = null;
            public void add(Datatype d, SchemaComponent referer) {
                pc.put(referer, d);
            }
            public Set<Map.Entry<SchemaComponent, Datatype>> getEntries() {
                return pc.entrySet();
            }
            public GlobalSimpleType getDefaultPrimitive() {
                if(def==null) {
                    def = getPrimitiveType("string");//NoI18n                    
                }
                return def;
            }
            public GlobalSimpleType getPrimitiveType(String typeName) {
                if(ptypes.isEmpty()) {
                    SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
                    Collection<GlobalSimpleType> primitives = primitiveModel.getSchema().getSimpleTypes();
                    for(GlobalSimpleType ptype: primitives){
                        ptypes.put(ptype.getName(), ptype);
                    }
                }
                return ptypes.get(typeName);
            }            
        };
    }
    
    protected void addRef(SchemaComponent referer, SchemaComponent ref) {
        sgh.addRef(referer, ref);
    }
    
    protected SchemaComponent getRef(SchemaComponent referer) {
        return sgh.getRef(referer);
    }
    
    protected void addToGlobal(SchemaComponent sc) {
        createGlobals.add(sc);
    }
    
    private void fixPrimitiveTypes() {            
        for(Map.Entry<SchemaComponent, Datatype> e: pc.getEntries()) {
            Datatype d = (Datatype) e.getValue();
            SchemaComponent referer = (SchemaComponent) e.getKey();
            String typeName = d.getName();
            if(d instanceof CustomDatatype)
                typeName = ((CustomDatatype)d).getBase().getName();
            GlobalSimpleType gst = pc.getPrimitiveType(typeName);
            NamedComponentReference<GlobalSimpleType> ref = 
                referer.createReferenceTo(gst, GlobalSimpleType.class);
            SchemaGeneratorUtil.setSimpleType(referer, ref);
        }
    }
        
    protected void clear() {
        path.clear();
        path = null;
        
        createGlobals.clear();
        createGlobals = null;
        
        fixNamesMap.clear();
        fixNamesMap = null;
        
        refMap.clear();
        refMap = null;
        
        namesMap.clear();
        namesMap = null;
    }
    
    interface SchemaGeneratorHelper {
        
        public void execute(SchemaModel sm) throws IOException;
        
        public SchemaComponent getParent(final AXIComponent axiparent)
        throws IllegalArgumentException;
        
                /*
                 * finds unique name from a bucket of global components
                 */
        public <T extends NameableSchemaComponent>String
                findUniqueGlobalName(Class<T> type, NameableSchemaComponent c,
                final String seed);
        
        public void setPeer(final Element element,
                final org.netbeans.modules.xml.schema.model.Element e,
                final ElementReference eref);
        
        public void setPeer(final Attribute attribute,
                final org.netbeans.modules.xml.schema.model.Attribute a,
                final AttributeReference aRef);
        
        public void addRef(SchemaComponent referer, SchemaComponent ref);
        
        public SchemaComponent getRef(SchemaComponent referer);
        
        public void addElement(GlobalElement ge, int index);
        
        public void addComplexType(GlobalComplexType gct, int index);
        
        public void addSimpleType(GlobalSimpleType gst, int index);
    }
    
    class UpdateHelper implements SchemaGeneratorHelper {
        
        UpdateHelper() {
        }
        
        public void execute(SchemaModel sm) throws IOException {
            DefaultSchemaGenerator.this.sm = sm;
            DefaultSchemaGenerator.this.am =
                    AXIModelFactory.getDefault().getModel(sm);
            SchemaUpdate su = SchemaGeneratorUtil.getSchemaUpdate(am);
            Collection<SchemaUpdate.UpdateUnit> us = su.getUpdateUnits();
            try {
                ((AXIModelImpl)am).disableAutoSync();
                sm.startTransaction();
                for(SchemaUpdate.UpdateUnit u:us) {
                    AXIComponent source = u.getSource();
                    if(source.getModel() != am) //skip mutating other model
                        continue;
                    SchemaUpdate.UpdateUnit.Type type = u.getType();
                    if(type == SchemaUpdate.UpdateUnit.Type.CHILD_ADDED)
                        addSchemaComponent(source, u);
                    else if(type == SchemaUpdate.UpdateUnit.Type.CHILD_DELETED)
                        SchemaGeneratorUtil.removeSchemaComponent(source, u, sm);
                    else if(type == SchemaUpdate.UpdateUnit.Type.CHILD_MODIFIED)
                        SchemaGeneratorUtil.modifySchemaComponent(source, u, sm, pc);
                }
//				addAllGlobals();             
            } finally {
                clear();
                sm.endTransaction();
                ((AXIModelImpl)am).enableAutoSync();
            }
        }
        
        protected void addSchemaComponent(AXIComponent source,
                SchemaUpdate.UpdateUnit u) {
            assert u.getNewValue() != null;
            scParent = DefaultSchemaGenerator.this.getParent(
                    ((AXIComponent)u.getNewValue()).getParent());
            assert scParent != null;
            ((AXIComponent)u.getNewValue()).accept(DefaultSchemaGenerator.this);
        }
        
        public <T extends NameableSchemaComponent>String
                findUniqueGlobalName(Class<T> type, NameableSchemaComponent c,
                final String seed) {
            return SchemaGeneratorUtil.findUniqueGlobalName(type, seed, sm);
        }
        
        public SchemaComponent getParent(final AXIComponent axiparent)
        throws IllegalArgumentException {
            SchemaComponent scParent = null;
            if(axiparent instanceof AXIDocument)
                scParent = sm.getSchema();
            else if(axiparent instanceof Element){
                SchemaComponent e = axiparent.getPeer();
                if(e instanceof ElementReference)
                    e = getRef(axiparent.getPeer());
                assert e != null;
                SchemaComponent lct = SchemaGeneratorUtil.getLocalComplexType(e);
                if(lct == null) {
                    lct = SchemaGeneratorUtil.getGlobalComplexType(e);
                    if(lct == null) {
                        if((e instanceof GlobalElement && ((GlobalElement)e).getType() != null &&
                            ((GlobalElement)e).getType().get() instanceof GlobalSimpleType) ||
                           (e instanceof LocalElement && ((LocalElement)e).getType() != null &&
                            ((LocalElement)e).getType().get() instanceof GlobalSimpleType)) {
                            SimpleContent newsc = sm.getFactory().createSimpleContent();
                            SimpleExtension se = sm.getFactory().createSimpleExtension();
                            newsc.setLocalDefinition(se);
                            GlobalType gst = null;
                            if(e instanceof GlobalElement)
                                gst = ((GlobalElement)e).getType().get();
                            else if(e instanceof LocalElement)
                                gst = ((LocalElement)e).getType().get();
                            if(gst != null)
                                se.setBase(se.createReferenceTo(gst, GlobalType.class));
                            lct = SchemaGeneratorUtil.createLocalComplexType(sm, e);
                            ((LocalComplexType)lct).setDefinition(newsc);
                        } else
                            lct = SchemaGeneratorUtil.createLocalComplexType(sm, e);
                    }
                }
                assert lct != null;
                scParent = lct;
            } else if(axiparent instanceof ContentModel){
                scParent = axiparent.getPeer();
            } else if(axiparent instanceof Compositor){
                scParent = axiparent.getPeer();
            }
            return scParent;
        }
        
        public void setPeer(final Element element,
                final org.netbeans.modules.xml.schema.model.Element e,
                final ElementReference eref) {
            if(eref != null)
                element.setPeer(eref);
            else
                element.setPeer(e);
        }
        
        public void setPeer(final Attribute attribute,
                final org.netbeans.modules.xml.schema.model.Attribute a,
                final AttributeReference aRef) {
            if(aRef != null)
                attribute.setPeer(aRef);
            else
                attribute.setPeer(a);
        }
        
        public void addElement(GlobalElement ge, int index) {
            if(index != -1)
                SchemaGeneratorUtil.addChildComponent(sm, sm.getSchema(), ge, index);
            else
                sm.getSchema().addElement((GlobalElement) ge);
        }
        
        public void addComplexType(GlobalComplexType gct, int index) {
            if(index != -1)
                SchemaGeneratorUtil.addChildComponent(sm, sm.getSchema(), gct, index);
            else
                sm.getSchema().addComplexType(gct);
        }
        
        public void addSimpleType(GlobalSimpleType gst, int index) {
            if(index != -1)
                SchemaGeneratorUtil.addChildComponent(sm, sm.getSchema(), gst, index);
            else
                sm.getSchema().addSimpleType(gst);
        }
        
        public void addRef(SchemaComponent referer, SchemaComponent ref) {
        }
        
        public SchemaComponent getRef(SchemaComponent referer) {
            if(referer instanceof ElementReference)
                return ((ElementReference)referer).getRef().get();
            else if(referer instanceof org.netbeans.modules.xml.schema.model.Element)
                if(referer instanceof GlobalElement &&
                    ((GlobalElement)referer).getType() != null &&
                    ((GlobalElement)referer).getType().get() instanceof GlobalComplexType)
                    return ((GlobalElement)referer).getType().get();
                else if(referer instanceof LocalElement &&
                    ((LocalElement)referer).getType() != null &&
                    ((LocalElement)referer).getType().get() instanceof GlobalComplexType)
                    return ((LocalElement)referer).getType().get();
            return null;
        }
        
        protected void clear() {
            DefaultSchemaGenerator.this.clear();
        }
    }
    
    class TransformHelper implements SchemaGeneratorHelper {
        
        TransformHelper() {
        }
        
        public  void execute(SchemaModel sm) throws IOException {
            DefaultSchemaGenerator.this.sm = sm;
            DefaultSchemaGenerator.this.am = AXIModelFactory.getDefault().getModel(sm);
            Schema schema = sm.getSchema();
            assert schema != null;
            try {
                HashMap<Class, Map<String, SchemaComponent>> allGlobals = 
                        new HashMap<Class, Map<String, SchemaComponent>>();
                java.util.List<Element> lrges = preTransform(schema, allGlobals);

                ((AXIModelImpl)am).disableAutoSync();
                sm.startTransaction();
                for(Element element : lrges) {
                    java.util.List<AXIComponent> pathFromRoot = new ArrayList<AXIComponent>();
                    pathFromRoot.add(element);
                    transformChildren(element, schema, pathFromRoot);
                }
                
                postTransform(schema, allGlobals);
            } finally {
                clear();
                try {
                    sm.endTransaction();
                }
                finally {
                    ((AXIModelImpl)am).enableAutoSync();
                    am.sync();
                }
            }
        }
        
        public SchemaComponent getParent(final AXIComponent axiparent)
        throws IllegalArgumentException  {
            throw new IllegalArgumentException("should not call this api during transform");
        }
        
        protected void transformChildren(AXIComponent component,
                SchemaComponent parent, java.util.List<AXIComponent> pathFromRoot) {
            //skip transforming components from other model
            if(!SchemaGeneratorUtil.fromSameSchemaModel(component.getPeer(), sm))
                return;
            assert parent != null;
            DefaultSchemaGenerator.this.scParent = parent;
            
            component.accept(DefaultSchemaGenerator.this);
            if(elementReuseList.contains(component))
                return;
            SchemaComponent cc = DefaultSchemaGenerator.this.sc;
            
            //check for cycle
            if(component instanceof Element) {
                Element orig = (Element)component;
                if(orig.isReference()) {
                    orig = SchemaGeneratorUtil.findOriginalElement(orig);
                }
                if(path.size() > 0 && path.contains(orig))
                    return;
                path.add(orig);
            }
            try {
                if(component.getChildren().size() > 0) {
                    parent = scParent;
                }
                assert parent != null;
                if(component instanceof AbstractElement) {
                    for(AbstractAttribute attr :
                        ((AbstractElement)component).getAttributes()) {
                            //skip transforming components from other model
                            if(!SchemaGeneratorUtil.fromSameSchemaModel(attr.getPeer(), sm))
                                continue;
                            DefaultSchemaGenerator.this.scParent = parent;
                            attr.accept(DefaultSchemaGenerator.this);
                        }
                }
                for(AXIComponent child: component.getChildren()) {
                    if(!(child instanceof AbstractAttribute)) {
                        if(pathFromRoot.contains(child.getOriginal()))
                            continue;
                        pathFromRoot.add(child.getOriginal());
                        transformChildren(child, parent, pathFromRoot);
                    }
                }
            } finally {
                if(component instanceof Element)
                    path.remove(path.size()-1);
            }
        }
        
        protected java.util.List<Element> preTransform(Schema schema,
                Map<Class, Map<String, SchemaComponent>> allGlobals) {
            java.util.List<Element> lrges =
                    SchemaGeneratorUtil.findMasterGlobalElements(
                    DefaultSchemaGenerator.this.am);
            
            //Now expand the AXI tree deep for some global elements from the list
            AXINonCyclicVisitor visitor = new AXINonCyclicVisitor(am);
            visitor.expand(lrges);
            
            //All saved globals
            
            SortedMap<String, SchemaComponent> ggmap = 
                    new TreeMap<String, SchemaComponent>();
            allGlobals.put(GlobalGroup.class, ggmap);
            for(GlobalGroup ag:schema.getGroups())
                ggmap.put(ag.getName(), ag);
            
            SortedMap<String, SchemaComponent> gctmap = 
                    new TreeMap<String, SchemaComponent>();
            allGlobals.put(GlobalComplexType.class, gctmap);
            for(GlobalComplexType gct:schema.getComplexTypes())
                gctmap.put(gct.getName(), gct);
            
            SortedMap<String, SchemaComponent> gagmap = 
                    new TreeMap<String, SchemaComponent>();
            allGlobals.put(GlobalAttributeGroup.class, gagmap);            
            for(GlobalAttributeGroup ag:schema.getAttributeGroups())
                gagmap.put(ag.getName(), ag);
            
            SortedMap<String, SchemaComponent> gstmap = 
                    new TreeMap<String, SchemaComponent>();
            allGlobals.put(GlobalSimpleType.class, gstmap);            
            for(GlobalType gst:schema.getSimpleTypes())
                gstmap.put(gst.getName(), gst);
            
            SortedMap<String, SchemaComponent> gemap = 
                    new TreeMap<String, SchemaComponent>();
            allGlobals.put(GlobalElement.class, gemap);            
            for(GlobalElement ge:schema.getElements())
                gemap.put(ge.getName(), ge);
            
            SortedMap<String, SchemaComponent> gamap = 
                    new TreeMap<String, SchemaComponent>();
            allGlobals.put(GlobalAttribute.class, gamap);            
            for(GlobalAttribute ga:schema.getAttributes())
                gamap.put(ga.getName(), ga);
            
            return lrges;
        }
        
        protected void postTransform(Schema schema,
                HashMap<Class, Map<String, SchemaComponent>> allGlobals) {
            //remove previous global elements, complextypes etc.,
            removeAllGlobals(schema, allGlobals);

            //add new elements, complextypes etc.,
            addAllGlobals(schema, createGlobals);

            //fix global element names, make them unique           
            fixGlobalElementNames();
            
            //fix primitive types
            fixPrimitiveTypes();
        }        
        
        //remove all previous global components
        private void removeAllGlobals(final Schema schema,
                final HashMap<Class, Map<String, SchemaComponent>> allGlobals) {            
            //remove all global simpleType
            removeGlobalSchemaComponent(GlobalSimpleType.class, allGlobals, schema);
            
            //remove all global attribute
            removeGlobalSchemaComponent(GlobalAttribute.class, allGlobals, schema);
            
            //remove all global attribute group
            removeGlobalSchemaComponent(GlobalAttributeGroup.class, allGlobals, schema);
            
            //remove all global complexType
            removeGlobalSchemaComponent(GlobalComplexType.class, allGlobals, schema);
            
            //remove all global group
            removeGlobalSchemaComponent(GlobalGroup.class, allGlobals, schema);
            
            //remove all global element
            removeGlobalSchemaComponent(GlobalElement.class, allGlobals, schema);        
        }

        private void removeGlobalSchemaComponent(final Class type, 
                final HashMap<Class, Map<String, SchemaComponent>> allGlobals, 
                final Schema schema) {
            Map<String, SchemaComponent> gmap = allGlobals.get(type);
            if(gmap == null) return;
            int count = 0;            
            for (Map.Entry entry : gmap.entrySet()) {
                SchemaComponent sc = (SchemaComponent) entry.getValue();
                commitTransaction(count);
                if(sc instanceof GlobalSimpleType)
                    schema.removeSimpleType((GlobalSimpleType) sc);
                else if(sc instanceof GlobalAttribute)
                    schema.removeAttribute((GlobalAttribute) sc);
                else if(sc instanceof GlobalAttributeGroup)
                    schema.removeAttributeGroup((GlobalAttributeGroup) sc);
                else if(sc instanceof GlobalComplexType)
                    schema.removeComplexType((GlobalComplexType) sc);
                else if(sc instanceof GlobalGroup)
                    schema.removeGroup((GlobalGroup) sc);
                else if(sc instanceof GlobalElement)
                    schema.removeElement((GlobalElement) sc);                
            }
        }

        private void commitTransaction(int count) {
            if(commitRange > 0 && (count++)%commitRange==0) {
                sm.endTransaction();
                sm.startTransaction();
            }
        }
        
        /*
         * finds unique name from a bucket of global components
         */
        public <T extends NameableSchemaComponent>String
                findUniqueGlobalName(Class<T> type, NameableSchemaComponent c,
                final String seed) {
            HashMap<String, SchemaComponent> map = namesMap.get(type);
            if(map == null) {
                map = new HashMap<String, SchemaComponent>();
                namesMap.put(type, map);
            }
            int count = 0;
            boolean found = true;
            while(found) {
                found = false;
                SchemaComponent sc =
                        map.get(count>0?(seed + String.valueOf(count)):seed);
                if(sc != null) {
                    count++;
                    found = true;
                }
            }
            String uniqueName = count>0?(seed + String.valueOf(count)):seed;
            map.put(uniqueName, c);
            return uniqueName;
        }
        
        public void setPeer(final Element element,
                final org.netbeans.modules.xml.schema.model.Element e,
                final ElementReference eref) {
            if(element.getChildren().size() > 0) {
                SchemaComponent lct = SchemaGeneratorUtil.getLocalComplexType(e);
                if(lct == null) {
                    //check type from another schema model
                    lct = SchemaGeneratorUtil.findTypeFromOtherModel(e, element, sm);
                    if(lct == null)
                        lct = SchemaGeneratorUtil.createLocalComplexType(sm, e);
                    assert lct != null;
                    scParent = lct;
                }
            } else
                scParent = e;
        }
        
        public void setPeer(final Attribute attribute,
                final org.netbeans.modules.xml.schema.model.Attribute a,
                final AttributeReference aref) {
            //FIXME
        }
        
        public void addElement(GlobalElement ge, int index) {
            //ignore index for transform, always append to schema
            addToGlobal(ge);
        }
        
        public void addComplexType(GlobalComplexType gct, int index) {
            //ignore index for transform, always append to schema
            addToGlobal(gct);
        }
        
        public void addSimpleType(GlobalSimpleType gst, int index) {
            //ignore index for transform, always append to schema
            addToGlobal(gst);
        }
        
        protected void addAllGlobals(final Schema schema,
                java.util.List<SchemaComponent> createGlobals) {
            for(int i=0;i<createGlobals.size();i++) {
                SchemaComponent sc = createGlobals.get(i);
                if(commitRange > 0 && i%commitRange==0) {
                    sm.endTransaction();
                    sm.startTransaction();
                }
                if(sc instanceof GlobalElement)
                    sm.getSchema().addElement((GlobalElement) sc);
                else if(sc instanceof GlobalComplexType)
                    sm.getSchema().addComplexType((GlobalComplexType) sc);
                if(sc instanceof GlobalSimpleType)
                    sm.getSchema().addSimpleType((GlobalSimpleType) sc);
                else if(sc instanceof GlobalGroup)
                    sm.getSchema().addGroup((GlobalGroup) sc);
                else if(sc instanceof GlobalAttributeGroup)
                    sm.getSchema().addAttributeGroup(
                            (GlobalAttributeGroup) sc);
            }
        }
        
        public void addRef(SchemaComponent referer, SchemaComponent ref) {
            refMap.put(referer, ref);
        }
        
        public SchemaComponent getRef(SchemaComponent referer) {
            return refMap.get(referer);
        }
        
        protected void clear() {
            DefaultSchemaGenerator.this.clear();
        }
    }
}
