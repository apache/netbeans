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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.axi.datatype.CustomDatatype;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.datatype.NumberBase;
import org.netbeans.modules.xml.axi.datatype.UnionType;
import org.netbeans.modules.xml.axi.visitor.AXINonCyclicVisitor;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.axi.visitor.FindUsageVisitor;
import org.netbeans.modules.xml.axi.impl.Preview;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *  SchemaGenerator Util class.
 *
 * @author Ayub Khan
 */
public class SchemaGeneratorUtil {
    
    public static NamedComponentReference<GlobalSimpleType> createPrimitiveType(
            final Datatype d, SchemaComponent referer, SchemaGenerator.PrimitiveCart pc){
        pc.add(d, referer);
        String typeName = d.getName();
        if(d instanceof CustomDatatype)
            typeName = ((CustomDatatype)d).getBase().getName();
        return referer.createReferenceTo(pc.getPrimitiveType(typeName), GlobalSimpleType.class);
    }
    
    public static boolean isPrimitiveType(Datatype d) {
        return !d.hasFacets() && !(d instanceof UnionType || d instanceof CustomDatatype);
    }
    
    public static void createGlobalSimpleType(
            final Datatype d, final SchemaModel sm, final SchemaComponent sc,
            final SchemaGenerator.UniqueId id, SchemaGenerator.PrimitiveCart pc) {
        if(d != null) {
            NamedComponentReference<GlobalSimpleType> ref =null;
            if(isPrimitiveType(d)) {
                ref = SchemaGeneratorUtil.createPrimitiveType(d, sc, pc);
            } else {
                GlobalSimpleType gst = SchemaGeneratorUtil.createGlobalSimpleType(sm);
                String typeName = d.getName();
                StringBuilder buf = new StringBuilder();
                buf.append("New"+typeName.substring(0, 1).toUpperCase()+
                        typeName.substring(1)+"Type"+String.valueOf(id.nextId()));
                String gstName = findUniqueGlobalName(GlobalSimpleType.class, buf.toString(), sm);
                gst.setName(gstName);
                sm.getSchema().addSimpleType(gst);
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
    
    public static GlobalSimpleType createGlobalSimpleType(final SchemaModel m) {
        GlobalSimpleType t = m.getFactory().createGlobalSimpleType();
        return t;
    }
    
    public static void setSimpleType(final SchemaComponent e, final NamedComponentReference<GlobalSimpleType> ref) {
        if(e instanceof GlobalElement) {
            if(((GlobalElement)e).getInlineType() != null)
                ((GlobalElement)e).setInlineType(null);
            ((GlobalElement)e).setType(ref);
        } else if(e instanceof LocalElement) {
            if(((LocalElement)e).getInlineType() != null)
                ((LocalElement)e).setInlineType(null);
            ((LocalElement)e).setType(ref);
        } else if(e instanceof GlobalAttribute) {
            if(((GlobalAttribute)e).getInlineType() != null)
                ((GlobalAttribute)e).setInlineType(null);
            ((GlobalAttribute)e).setType(ref);
        } else if(e instanceof LocalAttribute) {
            if(((LocalAttribute)e).getInlineType() != null)
                ((LocalAttribute)e).setInlineType(null);
            ((LocalAttribute)e).setType(ref);
        } else if(e instanceof SimpleTypeRestriction) {
            if(((SimpleTypeRestriction)e).getInlineType() != null) {
                ((SimpleTypeRestriction)e).setInlineType(null);                
            }
            ((SimpleTypeRestriction)e).setBase(ref);
        } else if(e instanceof Union) {
            ((Union)e).removeMemberType(ref);
            ((Union)e).addMemberType(ref);
        }
    }
    
    public static GlobalSimpleType findGlobalSimpleType(final SchemaModel sm, String name) {
        for(GlobalSimpleType gst: sm.getSchema().getSimpleTypes()) {
            String tmp = gst.getName();
            if(tmp.equals(name))
                return gst;
        }
        return null;
    }
    
    public static NamedComponentReference<? extends GlobalSimpleType> createInlineSimpleType(
            final Datatype d, final SchemaModel sm,
            final SchemaComponent sc, SchemaGenerator.PrimitiveCart pc) {
        if(sc instanceof org.netbeans.modules.xml.schema.model.Element)
            return createInlineSimpleType(d, sm, ((org.netbeans.modules.xml.schema.model.Element)sc), pc);
        else if(sc instanceof org.netbeans.modules.xml.schema.model.Attribute)
            return createInlineSimpleType(d, sm, ((org.netbeans.modules.xml.schema.model.Attribute)sc), pc);
        return null;
    }
    
    public static NamedComponentReference<? extends GlobalSimpleType> createInlineSimpleType(
            final Datatype d, final SchemaModel sm,
            final org.netbeans.modules.xml.schema.model.Element e, SchemaGenerator.PrimitiveCart pc) {
        NamedComponentReference ref = null;
        if(isPrimitiveType(d)) {
            ref = createPrimitiveType(d, e, pc);
            if(e instanceof TypeContainer) {
                if(((TypeContainer)e).getInlineType() != null)
                    ((TypeContainer)e).setInlineType(null);
                ((TypeContainer)e).setType(ref);
            }
        } else {
            LocalSimpleType lst = createLocalSimpleType(sm, e);
            if(d instanceof CustomDatatype)
                ref = populateSimpleType(((CustomDatatype)d).getBase(), sm, lst, pc);
            else
                ref = populateSimpleType(d, sm, lst, pc);
        }
        return ref;
    }
    
    public static NamedComponentReference<? extends GlobalSimpleType> createInlineSimpleType(
            final Datatype d, final SchemaModel sm,
            final org.netbeans.modules.xml.schema.model.Attribute attr, SchemaGenerator.PrimitiveCart pc) {
        NamedComponentReference ref = null;
        if(isPrimitiveType(d)) {
            ref = createPrimitiveType(d, attr, pc);
            if(attr instanceof GlobalAttribute) {
                if(((GlobalAttribute)attr).getInlineType() != null)
                    ((GlobalAttribute)attr).setInlineType(null);
                ((GlobalAttribute)attr).setType(ref);
            } else if(attr instanceof LocalAttribute) {
                if(((LocalAttribute)attr).getInlineType() != null)
                    ((LocalAttribute)attr).setInlineType(null);
                ((LocalAttribute)attr).setType(ref);
            }
        } else {
            LocalSimpleType lst = createLocalSimpleType(sm, attr);
            if(d instanceof CustomDatatype)
                ref = populateSimpleType(((CustomDatatype)d).getBase(), sm, lst, pc);
            else
                ref = populateSimpleType(d, sm, lst, pc);
        }
        return ref;
    }
    
    public static LocalSimpleType createLocalSimpleType(final SchemaModel m,
            final org.netbeans.modules.xml.schema.model.Element e) {
        LocalSimpleType t = m.getFactory().createLocalSimpleType();
        if(e instanceof TypeContainer) {
            if(((TypeContainer)e).getType() != null)
                ((TypeContainer)e).setType(null);//reset type if any
            ((TypeContainer)e).setInlineType(t);
        }
        return t;
    }
    
    public static LocalSimpleType createLocalSimpleType(final SchemaModel m,
            final org.netbeans.modules.xml.schema.model.Attribute attr) {
        LocalSimpleType t = m.getFactory().createLocalSimpleType();
        if(attr instanceof GlobalAttribute) {
            if(((GlobalAttribute)attr).getType() != null)
                ((GlobalAttribute)attr).setType(null);
            ((GlobalAttribute)attr).setInlineType(t);
        } else if(attr instanceof LocalAttribute) {
            if(((LocalAttribute)attr).getType() != null)
                ((LocalAttribute)attr).setType(null);
            ((LocalAttribute)attr).setInlineType(t);
        }
        return t;
    }
    
    public static LocalSimpleType createLocalSimpleType(final SchemaModel m,
            final Union u) {
        LocalSimpleType lst = m.getFactory().createLocalSimpleType();
        u.addInlineType(lst);
        return lst;
    }
    
    public static NamedComponentReference<? extends GlobalSimpleType> populateSimpleType(
            final Datatype d, final SchemaModel sm, final SchemaComponent st, 
            SchemaGenerator.PrimitiveCart pc) {
        NamedComponentReference ref = null;
        if(!d.hasFacets()) {
            if(d instanceof UnionType) {
/*
 * take care of
        <attribute name="remarks" use="optional">
          <simpleType>
            <union memberTypes="nonNegativeInteger positiveInteger"/>
          </simpleType>
        </attribute>
 */
                Union u = createUnion(sm, st);
                for(Datatype m:((UnionType)d).getMemberTypes()) {
                    ref = createPrimitiveType(m, u, pc);
                    u.addMemberType(ref);
                }
            } else {
                ref = createFacets(d, sm, st, pc);
            }
        } else {
            if(d instanceof UnionType) {
/*
 *take care of
        <attribute name="remarks2" use="optional">
          <simpleType>
            <union>
              <simpleType>
                <restriction base='nonNegativeInteger'/>
              </simpleType>
              <simpleType>
                <restriction base='string'>
                  <enumeration value='unbounded'/>
                </restriction>
              </simpleType>
            </union>
          </simpleType>
        </attribute>
 */
                Union u = createUnion(sm, st);
                
                for(Datatype m:((UnionType)d).getMemberTypes()) {
                    LocalSimpleType lst2 = createLocalSimpleType(sm, u);
                    //ref = createPrimitiveType(m, lst2, pc);
                    ref = createFacets(m, sm, lst2, pc);
                }
            } else {
                //ref = createPrimitiveType(d, st, pc);
                ref = createFacets(d, sm, st, pc);
            }
        }
        return ref;
    }
    
    public static NamedComponentReference createFacets(final Datatype d, final SchemaModel sm,
            final SchemaComponent lst, SchemaGenerator.PrimitiveCart pc) {        
        SimpleTypeRestriction def = createSimpleRestriction(sm, (SimpleType)lst);
        NamedComponentReference ref = createPrimitiveType(d, def, pc);
        def.setBase(ref);
        createLength(d, sm, def);
        createMinLength(d, sm, def);
        createMaxLength(d, sm, def);
        createPattern(d, sm, def);
        createEnumeration(d, sm, def);
        createWhitespace(d, sm, def);
        createTotalDigits(d, sm, def);
        createFractionDigits(d, sm, def);
        createMaxInclusive(d, sm, def);
        createMaxExclusive(d, sm, def);
        createMinInclusive(d, sm, def);
        createMinExclusive(d, sm, def);
        
        return ref;
    }
    
    public static void createMinExclusive(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Object> minExclusiveList = d.getMinExclusives();
        if(minExclusiveList != null) {
            for(Object minExclusive:minExclusiveList) {
                MinExclusive mnex = createMinExclusive(sm, def);
                mnex.setValue(String.valueOf(minExclusive));
            }
        }
    }
    
    public static void createMinInclusive(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Object> minInclusiveList = d.getMinInclusives();
        if(minInclusiveList != null) {
            for(Object minInclusive:minInclusiveList) {
                MinInclusive mnic = createMinInclusive(sm, def);
                mnic.setValue(String.valueOf(minInclusive));
            }
        }
    }
    
    public static void createMaxExclusive(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Object> maxExclusiveList = d.getMaxExclusives();
        if(maxExclusiveList != null) {
            for(Object maxExclusive:maxExclusiveList) {
                MaxExclusive mxex = createMaxExclusive(sm, def);
                mxex.setValue(String.valueOf(maxExclusive));
            }
        }
    }
    
    public static void createMaxInclusive(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Object> maxInclusiveList = d.getMaxInclusives();
        if(maxInclusiveList != null) {
            for(Object maxInclusive:maxInclusiveList) {
                MaxInclusive mxic = createMaxInclusive(sm, def);
                mxic.setValue(String.valueOf(maxInclusive));
            }
        }
    }
    
    public static void createFractionDigits(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Object> fractionDigitsList = d.getFractionDigits();
        if(fractionDigitsList != null) {
            for(Object fractionDigits:fractionDigitsList) {
                FractionDigits fdg = createFractionDigits(sm, def);
                fdg.setValue(Integer.parseInt(String.valueOf(fractionDigits)));
            }
        }
    }
    
    public static void createTotalDigits(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Object> totalDigitsList = d.getTotalDigits();
        if(totalDigitsList != null) {
            for(Object totalDigits:totalDigitsList) {
                TotalDigits tdg = createTotalDigits(sm, def);
                tdg.setValue(Integer.parseInt(String.valueOf(totalDigits)));
            }
        }
    }
    
    public static void createWhitespace(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<Whitespace.Treatment> wsList = d.getWhiteSpaces();
        if(wsList != null) {
            for(Whitespace.Treatment ws:wsList) {
                Whitespace w = createWhitespace(sm, def);
                w.setValue(ws);
            }
        }
    }
    
    public static void createEnumeration(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Object> enumList = d.getEnumerations();
        if(enumList != null) {
            for(Object en:enumList) {
                Enumeration e = createEnumeration(sm, def);
                e.setValue(String.valueOf(en));
            }
        }
    }
    
    public static void createPattern(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends String> patternList = d.getPatterns();
        if(patternList != null) {
            for(String pattern:patternList) {
                Pattern p = createPattern(sm, def);
                p.setValue(pattern);
            }
        }
    }
    
    public static void createMaxLength(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Number> maxLengthList = d.getMaxLengths();
        if(maxLengthList != null) {
            for(Number maxLength:maxLengthList) {
                MaxLength maxLen = createMaxLength(sm, def);
                maxLen.setValue(Integer.parseInt(NumberBase.toXMLString(maxLength)));
            }
        }
    }
    
    public static void createMinLength(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Number> minLengthList = d.getMinLengths();
        if(minLengthList != null) {
            for(Number minLength:minLengthList) {
                MinLength minLen = createMinLength(sm, def);
                minLen.setValue(Integer.parseInt(NumberBase.toXMLString(minLength)));
            }
        }
    }
    
    public static void createLength(final Datatype d, final SchemaModel sm,
            final SimpleTypeRestriction def) {
        List<? extends Number> lengthsList = d.getLengths();
        if(lengthsList != null) {
            for(Number length:lengthsList) {
                Length l = createLength(sm, def);
                l.setValue(Integer.parseInt(NumberBase.toXMLString(length)));
            }
        }
    }
    
    public static Sequence createSequence(
            final SchemaModel m, final ComplexContentDefinition ccd) {
        Sequence s = m.getFactory().createSequence();
        if(ccd instanceof ComplexContentRestriction)
            ((ComplexContentRestriction)ccd).setDefinition(s);
        else if(ccd instanceof ComplexExtension)
            ((ComplexExtension)ccd).setLocalDefinition(s);
        return s;
    }
    
    public static Sequence createSequence(
            final SchemaModel m, final ComplexTypeDefinition ctd, int index) {
        Sequence s = m.getFactory().createSequence();
        if(index != -1)
            addChildComponent(m, ctd, s, index);
        else {
            if(ctd instanceof Choice)
                ((Choice)ctd).addSequence(s);
            else if(ctd instanceof Sequence)
                ((Sequence)ctd).appendContent(s);
        }
        return s;
    }
    
    public static Sequence createSequence(
            final SchemaModel m, final ComplexType gct) {
        Sequence s = m.getFactory().createSequence();
        gct.setDefinition(s);
        return s;
    }
    
    public static Choice createChoice(
            final SchemaModel m, final ComplexContentDefinition ccd) {
        Choice c = m.getFactory().createChoice();
        if(ccd instanceof ComplexContentRestriction)
            ((ComplexContentRestriction)ccd).setDefinition(c);
        else if(ccd instanceof ComplexExtension)
            ((ComplexExtension)ccd).setLocalDefinition(c);
        return c;
    }
    
    public static Choice createChoice(
            final SchemaModel m, final ComplexTypeDefinition ctd, int index) {
        Choice c = m.getFactory().createChoice();
        if(index != -1)
            addChildComponent(m, ctd, c, index);
        else {
            if(ctd instanceof Choice)
                ((Choice)ctd).addChoice(c);
            else if(ctd instanceof Sequence)
                ((Sequence)ctd).appendContent(c);
        }
        return c;
    }
    
    public static Choice createChoice(
            final SchemaModel m, final ComplexType gct) {
        Choice c = m.getFactory().createChoice();
        gct.setDefinition(c);
        return c;
    }
    
    public static All createAll(
            final SchemaModel m, final ComplexContentDefinition ccd) {
        All c = m.getFactory().createAll();
        if(ccd instanceof ComplexContentRestriction)
            ((ComplexContentRestriction)ccd).setDefinition(c);
        else if(ccd instanceof ComplexExtension)
            ((ComplexExtension)ccd).setLocalDefinition(c);
        return c;
    }
    
    public static All createAll(
            final SchemaModel m, final ComplexType ct) {
        All a = m.getFactory().createAll();
        ct.setDefinition(a);
        return a;
    }
    
    public static GlobalElement findGlobalElement(final SchemaModel sm, final String eName) {
        for(GlobalElement ge:sm.getSchema().getElements())
            if(ge.getName().equals(eName))
                return ge;
        return null;
    }
    
    public static GlobalElement createGlobalElement(final SchemaModel m) {
        GlobalElement ge = m.getFactory().createGlobalElement();
        return ge;
    }
    
    public static LocalElement createLocalElement(final SchemaModel m,
            final ComplexTypeDefinition ctd, final String name, int index) {
        LocalElement le = m.getFactory().createLocalElement();
        le.setName(name);
        if(index != -1)
            addChildComponent(m, ctd, le, index);
        else {
            if(ctd instanceof Choice)
                ((Choice)ctd).addLocalElement(le);
            else if(ctd instanceof Sequence)
                ((Sequence)ctd).appendContent(le);
            else if(ctd instanceof All)
                ((All)ctd).addElement(le);
        }
        return le;
    }
    
    public static ElementReference createElementReference(SchemaModel m, SchemaComponent sc,
            GlobalElement e, int index) {
        ElementReference ref = m.getFactory().createElementReference();
        if(index != -1)
            addChildComponent(m, sc, ref, index);
        else {        
            if(sc instanceof Choice)
                ((Choice)sc).addElementReference(ref);
            else if(sc instanceof Sequence)
                ((Sequence)sc).appendContent(ref);
            else if(sc instanceof All)
                ((All)sc).addElementReference(ref);
        }
        ref.setRef(ref.createReferenceTo(e, GlobalElement.class));
        return ref;
    }
    
    public static LocalComplexType createLocalComplexType(final SchemaModel m,
            final SchemaComponent e) {
        LocalComplexType t = m.getFactory().createLocalComplexType();
        if(e instanceof TypeContainer) {
            if(((TypeContainer)e).getType() != null)
                ((TypeContainer)e).setType(null);//reset type if any
            ((TypeContainer)e).setInlineType(t);
        }
        return t;
    }
    
    public static Annotation createAnnotation(final SchemaModel m,
            final SchemaComponent p, final String s) {
        Annotation a = m.getFactory().createAnnotation();
        Documentation d = m.getFactory().createDocumentation();
        a.addDocumentation(d);  p.setAnnotation(a);
        org.w3c.dom.Element e = d.getDocumentationElement();
        m.getDocument().createTextNode(s);
        d.setDocumentationElement(e);
        return a;
    }
    
    public static SimpleTypeRestriction createSimpleRestriction(final SchemaModel m,
            final SimpleType st) {
        SimpleTypeRestriction csr = m.getFactory().createSimpleTypeRestriction();
        st.setDefinition(csr);
        return csr;
    }
    
    public static Union createUnion(final SchemaModel m, final SchemaComponent st) {
        Union u = m.getFactory().createUnion();
        ((SimpleType)st).setDefinition(u);
        return u;
    }
    
    public static LocalAttribute createLocalAttribute(final SchemaModel m,
            final String name, final SchemaComponent sc, int index) {
        LocalAttribute attr = m.getFactory().createLocalAttribute();
        attr.setName(name);
        if(index != -1)
            addChildComponent(m, sc, attr, index);
        else {
            if(sc instanceof LocalAttributeContainer)
                ((LocalAttributeContainer)sc).addLocalAttribute(attr);
        }
        return attr;
    }
    
    public static AttributeReference createAttributeReference(SchemaModel m, 
            SchemaComponent sc,
            GlobalAttribute a, int index) {
        AttributeReference ref = m.getFactory().createAttributeReference();
        if(index != -1)
            m.addChildComponent(sc, ref, index);
        else {
            if(sc instanceof LocalAttributeContainer)
                ((LocalAttributeContainer)sc).addAttributeReference(ref);
        }
        ref.setRef(ref.createReferenceTo(a, GlobalAttribute.class));
        return ref;
    }    
    
    public static LocalAttribute getLocalAttribute(final SchemaModel m,
            final String name, final SchemaComponent sc) {
        Collection<LocalAttribute> attrs = null;
        if(sc instanceof LocalAttributeContainer)
            attrs = ((LocalAttributeContainer)sc).getLocalAttributes();        
        if(attrs != null && !attrs.isEmpty()) {
            Iterator it = attrs.iterator();
            while(it.hasNext()) {
                LocalAttribute attr = (LocalAttribute) it.next();
                if(attr.getName().equals(name))
                    return attr;
            }
        }
        return null;
    }

    public static int findSchemaComponentIndex(final SchemaComponent parent, 
            final SchemaComponent child, int axiCompIndex) {
        List<SchemaComponent> allChilds = parent.getChildren();
        List<SchemaComponent> similarChilds = 
                parent.getChildren((Class<SchemaComponent>)child.getClass());
        int absIndex = axiCompIndex;
        if(axiCompIndex < 0 || similarChilds.size() == 0)
            absIndex = allChilds.size();
        else if(axiCompIndex == 0) {
            if(similarChilds.size() == 0)
                absIndex = allChilds.size();
            else {
                SchemaComponent prev = similarChilds.get(0);
                for(int i=0;i<allChilds.size();i++) {
                    SchemaComponent c = allChilds.get(i);
                    if(c == prev)
                        absIndex = i;
                }
            }
        }
        else if(axiCompIndex > 0) {
            if(axiCompIndex > similarChilds.size())
                axiCompIndex = similarChilds.size();
            SchemaComponent prev = similarChilds.get(axiCompIndex-1);
            for(int i=0;i<allChilds.size();i++) {
                SchemaComponent c = allChilds.get(i);
                if(c == prev)
                    absIndex = i+1;
            }
        }
        return absIndex;
    }
    
    public static void addChildComponent(
            final SchemaModel sm, final SchemaComponent parent, 
            final SchemaComponent child, int axiCompIndex) {
        sm.addChildComponent(parent, child, 
                findSchemaComponentIndex(parent, child, axiCompIndex));
    }
    
    public static TotalDigits createTotalDigits(final SchemaModel schemaModel,
            final SimpleTypeRestriction def) {
        TotalDigits tdg = schemaModel.getFactory().createTotalDigits();
        def.addTotalDigit(tdg);
        return tdg;
    }
    
    public static FractionDigits createFractionDigits(final SchemaModel schemaModel,
            final SimpleTypeRestriction def) {
        FractionDigits fdg = schemaModel.getFactory().createFractionDigits();
        def.addFractionDigits(fdg);
        return fdg;
    }
    
    public static Pattern createPattern(final SchemaModel schemaModel,
            final SimpleTypeRestriction def) {
        Pattern p = schemaModel.getFactory().createPattern();
        def.addPattern(p);
        return p;
    }
    
    public static Whitespace createWhitespace(final SchemaModel schemaModel,
            final SimpleTypeRestriction def) {
        Whitespace w = schemaModel.getFactory().createWhitespace();
        def.addWhitespace(w);
        return w;
    }
    
    public static Length createLength(final SchemaModel sm,
            final SimpleTypeRestriction def) {
        Length l = sm.getFactory().createLength();
        def.addLength(l);
        return l;
    }
    
    public static MinLength createMinLength(final SchemaModel sm,
            final SimpleTypeRestriction def) {
        MinLength minLen = sm.getFactory().createMinLength();
        def.addMinLength(minLen);
        return minLen;
    }
    
    public static MaxLength createMaxLength(final SchemaModel sm,
            final SimpleTypeRestriction def) {
        MaxLength maxLen = sm.getFactory().createMaxLength();
        def.addMaxLength(maxLen);
        return maxLen;
    }
    
    public static Enumeration createEnumeration(final SchemaModel sm,
            final SimpleTypeRestriction def) {
        Enumeration e = sm.getFactory().createEnumeration();
        def.addEnumeration(e);
        return e;
    }
    
    public static MaxInclusive createMaxInclusive(final SchemaModel sm,
            final SimpleTypeRestriction def) {
        MaxInclusive mxic = sm.getFactory().createMaxInclusive();
        def.addMaxInclusive(mxic);
        return mxic;
    }
    
    public static MaxExclusive createMaxExclusive(final SchemaModel sm,
            final SimpleTypeRestriction def) {
        MaxExclusive mxex = sm.getFactory().createMaxExclusive();
        def.addMaxExclusive(mxex);
        return mxex;
    }
    
    public static MinInclusive createMinInclusive(final SchemaModel sm,
            final SimpleTypeRestriction def) {
        MinInclusive mnic = sm.getFactory().createMinInclusive();
        def.addMinInclusive(mnic);
        return mnic;
    }
    
    public static MinExclusive createMinExclusive(final SchemaModel sm,
            final SimpleTypeRestriction def) {
        MinExclusive mnex = sm.getFactory().createMinExclusive();
        def.addMinExclusive(mnex);
        return mnex;
    }
    
    public static SchemaUpdate getSchemaUpdate(final AXIModel am) {
        SchemaUpdate su = new SchemaUpdate();
        
        List<PropertyChangeEvent> pcEvents =
                ((AXIModelImpl)am).getPropertyChangeListener().getEvents();
        for(PropertyChangeEvent ev:pcEvents) {
            SchemaUpdate.UpdateUnit.Type type = null;
            if(ev.getOldValue() != null) {
                if(ev.getNewValue() != null)
                    type = SchemaUpdate.UpdateUnit.Type.CHILD_MODIFIED;
                else {
                    //if axi components, then it has to be a delete
                    if(ev.getOldValue() instanceof AXIComponent)
                        type = SchemaUpdate.UpdateUnit.Type.CHILD_DELETED;
                    else
                        type = SchemaUpdate.UpdateUnit.Type.CHILD_MODIFIED;
                }
            } else if(ev.getNewValue() != null) {
                //if axi components, then it has to be a add
                if(!(ev.getNewValue() instanceof AXIComponent) ||
                        (ev.getSource() instanceof Element &&
                        ev.getNewValue() instanceof ContentModel))
                    type = SchemaUpdate.UpdateUnit.Type.CHILD_MODIFIED;
                else
                    type = SchemaUpdate.UpdateUnit.Type.CHILD_ADDED;
            }
            assert type != null;
            
            SchemaUpdate.UpdateUnit unit =
                    su.createUpdateUnit(type, (AXIComponent) ev.getSource(),
                    ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
            if(unit != null)
                su.addUpdateUnit(unit);
        }
        return su;
    }
    
    public static void replacePeer(final Element ae, final SchemaModel sm,
            final GlobalElement ge) {
        GlobalElement oldPeer = (GlobalElement) ae.getPeer();
        ae.setPeer(ge);
        
        if(oldPeer != null) {
            int count = sm.getSchema().getElements().size();
            sm.getSchema().removeElement((GlobalElement) oldPeer);
            assert count == (sm.getSchema().getElements().size() + 1);
        }
    }
    
    public static void removeSchemaComponent(final AXIComponent source,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm) {
        if(source instanceof AXIDocument) {
            if(u.getOldValue() instanceof Element) {
                removeGlobalElement(u, sm);//remove global element
            } else if(u.getOldValue() instanceof ContentModel) {
                removeContentModel(u, sm);//remove Content Model
            }
        } else if(source instanceof Element) {
            checkPopulate(source);
            ComplexType ct = (LocalComplexType)getLocalComplexType(
                    ((Element)source).getPeer());
            if(ct == null)
                ct = (GlobalComplexType)getGlobalComplexType(
                        ((Element)source).getPeer());
            if(ct == null) return;
            if(u.getOldValue() instanceof Attribute) {
                if(((Attribute)u.getOldValue()).getPeer() instanceof LocalAttribute)
                    removeLocalAttribute(((Attribute)u.getOldValue()), sm, ct);
                else if(((Attribute)u.getOldValue()).getPeer() instanceof AttributeReference)
                    removeAttributeRef(((Attribute)u.getOldValue()), sm, ct);
            } else if(u.getOldValue() instanceof Compositor)
                removeComplexTypeDefinition(u, sm, ct);
        } else if(source instanceof ContentModel) {
            checkPopulate(source);
            if(source.getPeer() instanceof GlobalComplexType) {
                GlobalComplexType gct =
                        (GlobalComplexType) ((ContentModel)source).getPeer();
                if(u.getOldValue() instanceof Compositor)
                    removeComplexTypeDefinition(u, sm, gct);
                else if(u.getOldValue() instanceof Attribute) {
                    if(u.getOldValue() instanceof Attribute) {
                        if(((Attribute)u.getOldValue()).getPeer() instanceof LocalAttribute)
                            removeLocalAttribute(((Attribute)u.getOldValue()), sm, gct);
                        else if(((Attribute)u.getOldValue()).getPeer() instanceof AttributeReference)
                            removeAttributeRef(((Attribute)u.getOldValue()), sm, gct);
                    }
                }
            } else if(source.getPeer() instanceof GlobalAttributeGroup) {
                if(u.getOldValue() instanceof Attribute) {
                    if(((Attribute)u.getOldValue()).getPeer() instanceof LocalAttribute)
                        removeLocalAttribute(((Attribute)u.getOldValue()), sm,
                                (GlobalAttributeGroup)source.getPeer());
                    else if(((Attribute)u.getOldValue()).getPeer() instanceof AttributeReference)
                        removeAttributeRef(((Attribute)u.getOldValue()), sm,
                                (GlobalAttributeGroup)source.getPeer());
                }
            }
        } else if(source instanceof Compositor) {
            ComplexTypeDefinition ctd = (ComplexTypeDefinition) ((Compositor)source).getPeer();
            if(ctd == null) return;
            if(u.getOldValue() instanceof Element) {
                if(((Element)u.getOldValue()).getPeer() instanceof LocalElement)
                    removeLocalElement(u, sm, ctd);
                else if(((Element)u.getOldValue()).getPeer() instanceof ElementReference)
                    removeElementRef(u, sm, ctd);
            } else if(u.getOldValue() instanceof Compositor)
                removeComplexTypeDefinition(u, sm, ctd);
        }
    }
    
    public static void removeContentModel(final SchemaUpdate.UpdateUnit u,
            final SchemaModel sm) {
        ContentModel cm = (ContentModel) u.getOldValue();
        assert cm != null;
        if(cm.getPeer() instanceof GlobalComplexType) {
            GlobalComplexType gct = (GlobalComplexType) cm.getPeer();
            Schema s = sm.getSchema();
            if(s.getComplexTypes().contains(gct))
                s.removeComplexType(gct);
        }
    }
    
    public static void removeGlobalElement(final SchemaUpdate.UpdateUnit u,
            final SchemaModel sm) {
        Element ae = (Element) u.getOldValue();
        assert ae != null;
        if(ae.getPeer() instanceof GlobalElement) {
            GlobalElement ge = (GlobalElement) ae.getPeer();
            Schema s = sm.getSchema();
            if(s.getElements().contains(ge))
                s.removeElement(ge);
        }
    }
    
    public static void removeLocalElement(final SchemaUpdate.UpdateUnit u,
            final SchemaModel sm, final ComplexTypeDefinition ctd) {
        if(ctd instanceof Choice)
            ((Choice)ctd).removeLocalElement(
                    (LocalElement) ((Element)u.getOldValue()).getPeer());
        else if(ctd instanceof Sequence)
            ((Sequence)ctd).removeContent(
                    (LocalElement) ((Element)u.getOldValue()).getPeer());
        else if(ctd instanceof All)
            ((All)ctd).removeElement(
                    (LocalElement) ((Element)u.getOldValue()).getPeer());
    }
    
    public static void removeElementRef(final SchemaUpdate.UpdateUnit u,
            final SchemaModel sm, final ComplexTypeDefinition ctd) {
        if(ctd instanceof Choice)
            ((Choice)ctd).removeElementReference(
                    (ElementReference) ((Element)u.getOldValue()).getPeer());
        else if(ctd instanceof Sequence)
            ((Sequence)ctd).removeContent(
                    (ElementReference) ((Element)u.getOldValue()).getPeer());
        else if(ctd instanceof All)
            ((All)ctd).removeElementReference(
                    (ElementReference) ((Element)u.getOldValue()).getPeer());
    }
    
    public static void removeLocalAttribute(final Attribute attribute,
            final SchemaModel sm, final SchemaComponent sc) {
        if(!(attribute.getPeer() instanceof LocalAttribute)) return;
        LocalAttribute attr = (LocalAttribute) attribute.getPeer();
        SchemaComponent attrParent = attr.getParent();
        if(attrParent instanceof LocalAttributeContainer)
            ((LocalAttributeContainer)attrParent).removeLocalAttribute(attr);
        else if(attrParent instanceof SimpleExtension)
            ((SimpleExtension)attrParent).removeLocalAttribute(attr);
    }
    
    public static void removeAttributeRef(final Attribute attribute,
            final SchemaModel sm, final SchemaComponent sc) {
        if(!(attribute.getPeer() instanceof AttributeReference)) return;
        AttributeReference ref = (AttributeReference) attribute.getPeer();
        SchemaComponent attrParent = ref.getParent();
        if(attrParent instanceof LocalAttributeContainer)
            ((LocalAttributeContainer)attrParent).removeAttributeReference(ref);
        else if(attrParent instanceof SimpleExtension)
            ((SimpleExtension)attrParent).removeAttributeReference(ref);
    }
    
    public static void removeComplexTypeDefinition(final SchemaUpdate.UpdateUnit u,
            final SchemaModel sm, final ComplexType ct) {
        if(!(u.getOldValue() instanceof Compositor)) return;
        Compositor c = (Compositor) u.getOldValue();
        ComplexTypeDefinition ctd = (ComplexTypeDefinition) ((Compositor)u.getOldValue()).getPeer();
        if(c.getType() == Compositor.CompositorType.CHOICE &&
                ctd instanceof Choice) {
            ct.setDefinition(null);
        } else if(c.getType() == Compositor.CompositorType.SEQUENCE &&
                ctd instanceof Sequence) {
            ct.setDefinition(null);
        } else if(c.getType() == Compositor.CompositorType.ALL &&
                ctd instanceof All) {
            ct.setDefinition(null);
        }
    }
    
    public static void removeComplexTypeDefinition(final SchemaUpdate.UpdateUnit u,
            final SchemaModel sm, final ComplexTypeDefinition ctd) {
        if(!(u.getOldValue() instanceof Compositor)) return;
        Compositor c = (Compositor) u.getOldValue();
        if(ctd instanceof Choice) {
            if(c.getType() == Compositor.CompositorType.CHOICE)
                ((Choice)ctd).removeChoice(
                        (Choice) ((Compositor)u.getOldValue()).getPeer());
            else if(c.getType() == Compositor.CompositorType.SEQUENCE)
                ((Choice)ctd).removeSequence(
                        (Sequence) ((Compositor)u.getOldValue()).getPeer());
        } else if(ctd instanceof Sequence) {
            if(c.getType() == Compositor.CompositorType.CHOICE)
                ((Sequence)ctd).removeContent(
                        (Choice) ((Compositor)u.getOldValue()).getPeer());
            else if(c.getType() == Compositor.CompositorType.SEQUENCE)
                ((Sequence)ctd).removeContent(
                        (Sequence) ((Compositor)u.getOldValue()).getPeer());
        } else if(ctd instanceof All) {
            if(u.getOldValue() instanceof LocalElement)
                ((All)ctd).removeElement(
                        (LocalElement) ((Compositor)u.getOldValue()).getPeer());
            else if(u.getOldValue() instanceof ElementReference)
                ((All)ctd).removeElementReference(
                        (ElementReference) ((Compositor)u.getOldValue()).getPeer());
        }
    }
    
    public static void removeComplexContentDefinition(final SchemaUpdate.UpdateUnit u,
            final SchemaModel sm, final ComplexContentDefinition ctd) {
        if(!(u.getOldValue() instanceof Compositor)) return;
        Compositor c = (Compositor) u.getOldValue();
        if(ctd instanceof Choice) {
            if(c.getType() == Compositor.CompositorType.CHOICE)
                ((Choice)ctd).removeChoice(
                        (Choice) ((Compositor)u.getOldValue()).getPeer());
            else if(c.getType() == Compositor.CompositorType.SEQUENCE)
                ((Choice)ctd).removeSequence(
                        (Sequence) ((Compositor)u.getOldValue()).getPeer());
        } else if(ctd instanceof Sequence) {
            if(c.getType() == Compositor.CompositorType.CHOICE)
                ((Sequence)ctd).removeContent(
                        (Choice) ((Compositor)u.getOldValue()).getPeer());
            else if(c.getType() == Compositor.CompositorType.SEQUENCE)
                ((Sequence)ctd).removeContent(
                        (Sequence) ((Compositor)u.getOldValue()).getPeer());
        }
    }
    
    public static void modifySchemaComponent(final AXIComponent source,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm, SchemaGenerator.PrimitiveCart pc) {
        if(source instanceof AXIDocument) {
            modifyAXIDocument((AXIDocument)source, u, sm);
        } else if(source instanceof Element) {
            if(((Element)source).getPeer() instanceof LocalElement)
                modifyLocalElement(source, u, sm, pc);
            else if(((Element)source).getPeer() instanceof GlobalElement)
                modifyGlobalElement(((Element)source), u, sm, pc);
            else if(source instanceof ElementRef &&
                    ((Element)source).getPeer() instanceof ElementReference)
                modifyElementRef(((ElementRef)source), u, sm);
        } else if(source instanceof ContentModel) {
            //Schema All, Choice, Sequence
            modifyContentModel((ContentModel) source, u, sm);
        } else if(source instanceof Compositor) {
            //Schema All, Choice, Sequence
            modifyCompositor((Compositor) source, u, sm);
        } else if(source instanceof Attribute) {
            if(((Attribute)source).getPeer() instanceof LocalAttribute)
                modifyLocalAttribute(source, u, sm, pc);
            else if(((Attribute)source).getPeer() instanceof GlobalAttribute)
                modifyGlobalAttribute(source, u, sm, pc);
            else if(((Attribute)source).getPeer() instanceof AttributeReference)
                modifyAttributeRef(source, u, sm);
        } else if(source instanceof AnyElement) {
            if(((AnyElement)source).getPeer() instanceof
                    org.netbeans.modules.xml.schema.model.AnyElement)
                modifyAnyElement(((AnyElement)source), u, sm);
        }
    }
    
    public static void modifyAttributeProperties(
            final org.netbeans.modules.xml.schema.model.Attribute a,
            final String property, final Object newValue) {
        if(property.equals(Element.PROP_DEFAULT)) {
            a.setDefault((String) newValue);
        } else if(property.equals(Element.PROP_FIXED)) {
            a.setFixed((String) newValue);
        }
    }
    
    public static void modifyLocalAttribute(final AXIComponent source,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm, SchemaGenerator.PrimitiveCart pc) {
        LocalAttribute la = (LocalAttribute) ((Attribute)source).getPeer();
        String propertyName = u.getPropertyName();
        Object newValue = u.getNewValue();
        
        //check, if any modify fixed, default, nillable properties
        modifyAttributeProperties(la, propertyName, newValue);
        
        if(propertyName.equals(Attribute.PROP_NAME)) {
            la.setName((String)u.getNewValue());//change local attribute name
        } else if(propertyName.equals(Attribute.PROP_FORM)) {
            la.setForm((Form) newValue);
        } else if(propertyName.equals(Attribute.PROP_USE)) {
            la.setUse((Use) newValue);
        } else if(propertyName.equals(Attribute.PROP_TYPE)) {
            modifyDatatype(sm, la, (Datatype) u.getNewValue(), pc);
        }
    }
    
    public static void modifyAttributeRef(final AXIComponent source,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm) {
        String propertyName = u.getPropertyName();
        Object newValue = u.getNewValue();
        AttributeReference la = (AttributeReference) ((Attribute)source).getPeer();
        if(la.getRef().get().getName().equals(u.getOldValue())) {
            String attrName = findUniqueGlobalName(GlobalAttribute.class, (String)newValue, sm);
            la.getRef().get().setName(attrName);//change local attribute name
        }
        
        //check, if any modify fixed, default, nillable properties
        modifyAttributeProperties(la, propertyName, newValue);
        
        if(propertyName.equals(Attribute.PROP_FORM)) {
            la.setForm((Form) newValue);
        } else if(propertyName.equals(Attribute.PROP_USE)) {
            la.setUse((Use) newValue);
        } else if(propertyName.equals(AttributeRef.PROP_ATTRIBUTE_REF)) {
            GlobalAttribute ga = (GlobalAttribute)((Attribute)newValue).getPeer();
            NamedComponentReference ncr = la.getModel().getFactory().
                    createGlobalReference(ga, GlobalAttribute.class, la);
            la.setRef(ncr);
        }
    }
    
    public static void modifyGlobalAttribute(final AXIComponent source,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm, SchemaGenerator.PrimitiveCart pc) {
        GlobalAttribute ga = (GlobalAttribute) ((Attribute)source).getPeer();
        String propertyName = u.getPropertyName();
        Object newValue = u.getNewValue();
        
        //check, if any modify fixed, default, nillable properties
        modifyAttributeProperties(ga, propertyName, newValue);
        
        if(propertyName.equals(Attribute.PROP_NAME)) {
            String attrName = findUniqueGlobalName(GlobalAttribute.class, (String)newValue, sm);
            ga.setName(attrName);
        } else if(propertyName.equals(Attribute.PROP_TYPE)) {
            modifyDatatype(sm, ga, (Datatype) u.getNewValue(), pc);
        }
    }
    
    private static void modifyDatatype(final SchemaModel sm,
            final SchemaComponent component, final Datatype d, SchemaGenerator.PrimitiveCart pc) {
        if(d == null)
            return;
        String typeName = d.getName();
        if(d instanceof CustomDatatype)
            typeName = ((CustomDatatype)d).getName();
        if(typeName != null) {
            if(component instanceof GlobalAttribute &&
                    ((GlobalAttribute)component).getInlineType() != null) {
                createInlineSimpleType(d, sm, ((GlobalAttribute)component), pc);
                return;
            } else if(component instanceof LocalAttribute &&
                    ((LocalAttribute)component).getInlineType() != null) {
                createInlineSimpleType(d, sm, ((LocalAttribute)component), pc);
                return;
            } else if(component instanceof GlobalElement &&
                    ((GlobalElement)component).getInlineType() != null) {
                createInlineSimpleType(d, sm, ((GlobalElement)component), pc);
                return;
            } else if(component instanceof LocalElement &&
                    ((LocalElement)component).getInlineType() != null) {
                createInlineSimpleType(d, sm, ((LocalElement)component), pc);
                return;
            } else {
                NamedComponentReference<GlobalSimpleType> ref = null;
                for(GlobalSimpleType gst: sm.getSchema().getSimpleTypes())
                    if(gst.getName().equals(typeName)) {
                    ref = component.createReferenceTo(gst, GlobalSimpleType.class);
                    break;
                    }
                if(ref == null) {
                    SchemaGenerator.UniqueId id =
                            new SchemaGenerator.UniqueId() {
                        private int lastId = -1;
                        public int nextId() {
                            return ++lastId;
                        }
                    };
                    createGlobalSimpleType(d, sm, component, id, pc);
                } else
                    setSimpleType(component, ref);
            }
        }
    }
    
    public static void modifyElementProperties(
            final org.netbeans.modules.xml.schema.model.Element e,
            final String property, final Object newValue) {
        if(property.equals(Element.PROP_DEFAULT)) {
            e.setDefault((String) newValue);
        } else if(property.equals(Element.PROP_FIXED)) {
            e.setFixed((String) newValue);
        } else if(property.equals(Element.PROP_NILLABLE)) {
            e.setNillable((Boolean) newValue);
        }
    }
    
    public static void modifyCardinality(final SchemaComponent sc,
            final String property, final Object newValue) {
        if(property.equals(Element.PROP_MINOCCURS)) {
            if(sc instanceof All) {
                if(newValue == null)//restore default
                    ((All)sc).setMinOccurs(Occur.ZeroOne.ONE);
                else if(newValue instanceof Occur.ZeroOne)
                    ((All)sc).setMinOccurs((Occur.ZeroOne) newValue);
            } else {
                try {
                    int minOccurs = 1;//default value
                    if(newValue != null)
                        minOccurs = Integer.parseInt((String) newValue);
                    if(sc instanceof LocalElement)
                        ((LocalElement)sc).setMinOccurs(minOccurs);
                    else if(sc instanceof ElementReference)
                        ((ElementReference)sc).setMinOccurs(minOccurs);
                    else if(sc instanceof Sequence)
                        ((Sequence)sc).getCardinality().setMinOccurs(minOccurs);
                    else if(sc instanceof Choice)
                        ((Choice)sc).getCardinality().setMinOccurs(minOccurs);
                    else if(sc instanceof
                            org.netbeans.modules.xml.schema.model.AnyElement)
                        ((org.netbeans.modules.xml.schema.model.AnyElement)sc).
                                setMinOccurs(minOccurs);
                } catch(Throwable th) {
                }
            }
        } else if(property.equals(Element.PROP_MAXOCCURS)) {
            if(sc instanceof LocalElement)
                ((LocalElement)sc).setMaxOccurs((String) newValue);
            else if(sc instanceof ElementReference)
                ((ElementReference)sc).setMaxOccurs((String) newValue);
            else if(sc instanceof Sequence)
                ((Sequence)sc).getCardinality().setMaxOccurs((String) newValue);
            else if(sc instanceof Choice)
                ((Choice)sc).getCardinality().setMaxOccurs((String) newValue);
            else if(sc instanceof org.netbeans.modules.xml.schema.model.AnyElement)
                ((org.netbeans.modules.xml.schema.model.AnyElement)sc).
                        setMaxOccurs((String) newValue);
        }
    }
    
    public static void modifyLocalElement(final AXIComponent source,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm, SchemaGenerator.PrimitiveCart pc) {
        checkPopulate(source);
        LocalElement le = (LocalElement) ((Element)source).getPeer();
        String propertyName = u.getPropertyName();
        Object newValue = u.getNewValue();
        
        //check, if any modify fixed, default, nillable properties
        modifyElementProperties(le, propertyName, newValue);
        
        //check, if any modify minoccurs and maxoccurs
        modifyCardinality(le, propertyName, newValue);
        
        if(propertyName.equals(Element.PROP_NAME)) {
            le.setName((String)newValue);//change local element name
        } else if(propertyName.equals(Element.PROP_FORM)) {
            le.setForm((Form) newValue);
        } else if(propertyName.equals(Element.PROP_TYPE)) {
            if(newValue instanceof ContentModel) {
                //remove any inline type
                LocalComplexType lct = (LocalComplexType) le.getInlineType();
                if(lct != null)
                    le.setInlineType(null);
                
                //now set type
                le.setType(le.createReferenceTo(
                        (GlobalComplexType)((ContentModel)newValue).getPeer(),
                        GlobalComplexType.class));
            } else if(newValue instanceof Datatype) {
                modifyDatatype(sm, le, (Datatype) newValue, pc);
            } else {
                le.setInlineType(null);                
            }
        }
    }
    
    public static void modifyElementRef(final ElementRef element,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm) {
        checkPopulate(element);
        ElementReference eref = (ElementReference) element.getPeer();
        assert eref != null;
        String propertyName = u.getPropertyName();
        Object newValue = u.getNewValue();
        Object oldValue = u.getOldValue();
        
        //check, if any modify minoccurs and maxoccurs
        modifyCardinality(eref, propertyName, newValue);
        
        if(propertyName.equals(ElementRef.PROP_ELEMENT_REF)) {
            GlobalElement ge = (GlobalElement)((Element)newValue).getPeer();
            NamedComponentReference ncr = eref.getModel().getFactory().
                    createGlobalReference(ge, GlobalElement.class, eref);
            eref.setRef(ncr);
        }
    }
    
    public static void modifyGlobalElement(final Element element,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm, SchemaGenerator.PrimitiveCart pc) {
        checkPopulate(element);
        if(u.getPropertyName().equals(Element.PROP_MINOCCURS) ||
                u.getPropertyName().equals(Element.PROP_MAXOCCURS)) {
            return;//cannot set min or max occurs for global
        }
        GlobalElement ge = (GlobalElement) element.getPeer();
        assert ge != null;
        String propertyName = u.getPropertyName();
        Object newValue = u.getNewValue();
        
        //check, if any modify fixed, default, nillable properties
        modifyElementProperties(ge, propertyName, newValue);
        
        if(propertyName.equals(Element.PROP_NAME)) {
            String eName = findUniqueGlobalName(GlobalElement.class, (String)newValue, sm);
            ge.setName(eName);
            refactorRenameElement(element, (String)newValue, ge);
        } else if(propertyName.equals(Element.PROP_TYPE)) {
            if(newValue instanceof ContentModel) {
                //remove any inline type
                LocalType lt = (LocalType) ge.getInlineType();
                if(lt != null)
                    ge.setInlineType(null);
                
                //now set type
                ge.setType(ge.createReferenceTo(
                        (GlobalComplexType)((ContentModel)newValue).getPeer(),
                        GlobalComplexType.class));
            } else if(newValue instanceof Datatype) {
                modifyDatatype(sm, ge, (Datatype) newValue, pc);
            } else {
                ge.setInlineType(null);                
            }
        }
    }
    
    public static void refactorRenameElement(final Element element,
            final String newValue, GlobalElement ge) {
        //Now expand the AXI tree deep for some global elements from the list
        AXINonCyclicVisitor visitor = new AXINonCyclicVisitor(element.getModel());
        visitor.expand(element.getModel().getRoot());
        
        if(element.getRefSet() == null)
            return;
        assert ge != null;
        for(AXIComponent ref : element.getRefSet()) {
            if(ref instanceof Element) {
                if(ref instanceof ElementRef) {
                    ElementReference eref = (ElementReference) ref.getPeer();
                    assert eref != null;
                    eref.setRef(eref.createReferenceTo(ge, GlobalElement.class));
                } else
                    ((Element)ref).setName((String)newValue);
            }
        }
    }
    
    public static void refactorRenameType(final ContentModel cm,
            final String newValue, GlobalComplexType gct) {
        //Now expand the AXI tree deep for some global elements from the list
        AXINonCyclicVisitor visitor = new AXINonCyclicVisitor(cm.getModel());
        visitor.expand(cm.getModel().getRoot());
        
        if(cm.getRefSet() == null)
            return;
        assert gct != null;
        for(AXIComponent ref : cm.getRefSet()) {
            if(ref instanceof Element) {
                org.netbeans.modules.xml.schema.model.Element eref =
                        (org.netbeans.modules.xml.schema.model.Element) ref.getPeer();
                assert eref != null;
                setType(eref, gct);
            }
        }
    }
    
    public static <T extends NameableSchemaComponent>String
            findUniqueGlobalName(Class<T> type, final String seed,
            final SchemaModel sm) {
        int count = 0;
        boolean found = true;
        while(found) {
            found = false;
            for(T sc:sm.getSchema().getChildren(type)) {
                if(sc.getName().equals(count>0?(seed + String.valueOf(count)):seed)) {
                    count++;
                    found = true;
                }
            }
        }
        return count>0?(seed + String.valueOf(count)):seed;
    }
    
    public static void modifyAnyElement(final AnyElement element,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm) {
        checkPopulate(element);
        org.netbeans.modules.xml.schema.model.AnyElement ae =
                (org.netbeans.modules.xml.schema.model.AnyElement) element.getPeer();
        String propertyName = u.getPropertyName();
        Object newValue = u.getNewValue();
        
        //check, if any modify minoccurs and maxoccurs
        modifyCardinality(ae, propertyName, newValue);
        
        if(propertyName.equals(AnyElement.PROP_PROCESSCONTENTS)) {
            ae.setProcessContents((org.netbeans.modules.xml.schema.model.AnyElement.ProcessContents)newValue);
        }
    }
    
    public static void modifyAXIDocument(final AXIDocument document,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm) {
        Schema schema = (Schema)document.getPeer();
        if(u.getPropertyName().equals(AXIDocument.PROP_TARGET_NAMESPACE)) {
            schema.setTargetNamespace((String)u.getNewValue());//change name
        } else if(u.getPropertyName().equals(AXIDocument.PROP_ATTRIBUTE_FORM_DEFAULT)) {
            schema.setAttributeFormDefault((Form)u.getNewValue());//change attr form default
        } else if(u.getPropertyName().equals(AXIDocument.PROP_ELEMENT_FORM_DEFAULT)) {
            schema.setElementFormDefault((Form)u.getNewValue());//change element form default
        } else if(u.getPropertyName().equals(AXIDocument.PROP_VERSION)) {
            schema.setVersion((String)u.getNewValue());//change version
        }
    }
    
    public static void modifyContentModel(final ContentModel conentModel,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm) {
        checkPopulate(conentModel);
        GlobalComplexType gct = (GlobalComplexType) ((ContentModel)conentModel).getPeer();
        if(u.getPropertyName().equals(Element.PROP_NAME)) {
            String typeName = findUniqueGlobalName(GlobalComplexType.class,
                    (String)u.getNewValue(), sm);
            gct.setName(typeName);//change name
            refactorRenameType(conentModel, typeName, gct);
        }
    }
    
    public static void modifyCompositor(final Compositor compositor,
            final SchemaUpdate.UpdateUnit u, final SchemaModel sm) {
        checkPopulate(compositor);
        ComplexTypeDefinition oldc = (ComplexTypeDefinition) ((Compositor)compositor).getPeer();
        String propertyName = u.getPropertyName();
        Object newValue = u.getNewValue();
        
        //check, if any modify minoccurs and maxoccurs
        modifyCardinality(oldc, propertyName, newValue);
        
        //removing old schema compositor and add new one
        if(propertyName.equals(Compositor.PROP_TYPE)) {
            int index = compositor.getIndex();
            if(index == -1) index = 0;
            SchemaComponent ctd = compositor.getPeer();
            SchemaComponent ctdParent = ctd.getParent();
            SchemaComponent ctdCopy = (SchemaComponent) ctd.copy(ctd.getParent());
            ComplexTypeDefinition newctd = null;
            if(newValue.equals(CompositorType.SEQUENCE))
                newctd = sm.getFactory().createSequence();
            else if(newValue.equals(CompositorType.CHOICE))
                newctd = sm.getFactory().createChoice();
            else if(newValue.equals(CompositorType.ALL))
                newctd = sm.getFactory().createAll();
            //populate min, max occurs, annotation etc., from old compositor
            SchemaGeneratorUtil.populateCompositor(newctd, compositor);
            int count = 0;
            for(SchemaComponent sc: ctdCopy.getChildren()) {
                sm.addChildComponent(newctd, sc, count++);
            }
            sm.removeChildComponent(ctd);
            sm.addChildComponent(ctdParent, newctd, index);
        }
    }
    
    public static GlobalAttributeGroup createGlobalAttributeGroup(final SchemaModel m) {
        GlobalAttributeGroup t = m.getFactory().createGlobalAttributeGroup();
//		m.getSchema().addAttributeGroup(t);
        return t;
    }
    
    public static GlobalAttributeGroup createGlobalAttributeGroup(final SchemaModel sm,
            final String name) {
        GlobalAttributeGroup gag = createGlobalAttributeGroup(sm);
        if(gag != null) {
            String agName = findUniqueGlobalName(GlobalAttributeGroup.class, name, sm);
            gag.setName(agName);//NoI18n
        }
        return gag;
    }

    public static GlobalComplexType findTypeFromOtherModel(
            org.netbeans.modules.xml.schema.model.Element e,
            Element element, SchemaModel sm) {
        //check type from another schema model
        GlobalComplexType gct = null;
        org.netbeans.modules.xml.schema.model.Element oldE =
                (org.netbeans.modules.xml.schema.model.Element) element.getPeer();
        NamedComponentReference ref = null;
        if(oldE instanceof GlobalElement)
            ref = ((GlobalElement)oldE).getType();
        else if(oldE instanceof LocalElement)
            ref = ((LocalElement)oldE).getType();
        else if(oldE instanceof ElementReference)
            ref = ((ElementReference)oldE).getRef();
        if(ref != null && ref.get() instanceof GlobalComplexType &&
                !fromSameSchemaModel((GlobalComplexType)ref.get(), sm)) {
            gct = (GlobalComplexType) ref.get();
            if(e instanceof LocalElement) {//overwrite type if present
                if(((LocalElement)e).getInlineType() != null)
                    ((LocalElement)e).setInlineType(null);
                ((LocalElement)e).setType(e.createReferenceTo(gct,
                        GlobalComplexType.class));
            } else if(e instanceof GlobalElement) {//overwrite type if present
                if(((GlobalElement)e).getInlineType() != null)
                    ((GlobalElement)e).setInlineType(null);
                ((GlobalElement)e).setType(e.createReferenceTo(gct,
                        GlobalComplexType.class));
            }
        }
        return gct;
    }
    
    public static GlobalType getGlobalComplexType(final SchemaComponent sc) {
        GlobalComplexType gct = null;
        if(sc instanceof TypeContainer && ((TypeContainer)sc).getType() != null &&
                ((TypeContainer)sc).getType().get() instanceof GlobalComplexType)
            gct = (GlobalComplexType) ((TypeContainer)sc).getType().get();
        else if(sc instanceof GlobalComplexType)
            return (GlobalType) sc;
        return gct;
    }
    
    public static GlobalComplexType createGlobalComplexType(final SchemaModel m) {
        GlobalComplexType t = m.getFactory().createGlobalComplexType();
        return t;
    }
    
    public static void setType(
            final org.netbeans.modules.xml.schema.model.Element e,
            final GlobalComplexType gct) {
        if(e instanceof TypeContainer) {//overwrite type if present
            if(((TypeContainer)e).getInlineType() != null)
                ((TypeContainer)e).setInlineType(null);
            ((TypeContainer)e).setType(e.createReferenceTo(gct, GlobalComplexType.class));
        }
    }
    
    public static LocalType getLocalComplexType(final SchemaComponent sc) {
        LocalType lct = null;
        if(sc instanceof TypeContainer)
            lct = ((TypeContainer)sc).getInlineType();
        else if(sc instanceof LocalComplexType)
            lct = (LocalType) sc;
        return lct;
    }
    
    public static NamedComponentReference<? extends GlobalType>  getType(
            final SchemaComponent sc) {
        if(sc instanceof TypeContainer)
            return  ((TypeContainer)sc).getType();
        return null;
    }
    
    public static Element findGlobalElement(final AXIComponent c) {
        AXIComponent parent = c.getParent();
        if(parent == null || parent instanceof AXIDocument) {
            //true only for an AXIDocument.
            return (Element)c;
        }
        
        return findGlobalElement(c.getParent());
    }
    
    public static SchemaGenerator.Pattern inferDesignPattern(AXIModel am) {
        SchemaGenerator.Pattern dp = null;
//		java.util.List<Element> lrges = findMasterGlobalElements(am);
        SchemaModel sm = am.getSchemaModel();
        int geCount = sm.getSchema().getElements().size();
        int ctCount = sm.getSchema().getComplexTypes().size();
        if(ctCount > 0)
            if(geCount > 1)
                dp = SchemaGenerator.Pattern.GARDEN_OF_EDEN;
            else {
            GlobalElement ge = null;
            Iterator it = sm.getSchema().getElements().iterator();
            if(it.hasNext())
                ge = (GlobalElement) it.next();
            if(ge != null && ge.getType() != null &&
                    ge.getType().get() instanceof GlobalComplexType)
                dp = SchemaGenerator.Pattern.GARDEN_OF_EDEN;
            else
                dp = SchemaGenerator.Pattern.VENITIAN_BLIND;
            } else
//			if(lrges.size() > 1 || geCount > 1)
                if(geCount > 1)
                    dp = SchemaGenerator.Pattern.SALAMI_SLICE;
                else if(geCount == 1)
                    dp = SchemaGenerator.Pattern.RUSSIAN_DOLL;
        return dp;
    }
    
    public static java.util.List<Element> findMasterGlobalElements(final AXIModel am) {
        //least referenced global elements
        java.util.List<Element> lrges = new ArrayList<Element>();
        
        Preview p = new FindUsageVisitor(am).findUsages(am.getRoot());
        if(p == null) return lrges;
        //get the reverse usage map (ie., B usedBy A) for all global elements
        Map<AXIComponent, java.util.List<AXIComponent>> revmap = p.getReverseUsages();
        
        List<GlobalElement> refges = new ArrayList<GlobalElement>();
        List<Element> ges = am.getRoot().getElements();
        for(Element e : ges) {
            java.util.List<AXIComponent> useList = revmap.get(e);
            if(useList.size() > 1) {
                for(int i=1;i<useList.size();i++) {
                    AXIComponent c = useList.get(i);
                    if(c instanceof Element) {
                        SchemaComponent peer = c.getPeer();
                        if(peer instanceof ElementReference) {
                            peer = ((ElementReference)peer).getRef().get();
                            for(Element e1 : ges) {
                                if(e1.getPeer() == peer &&
                                        peer != e.getPeer()) {
                                    refges.add((GlobalElement)peer);
                                    break;
                                }
                            }
                        }
                    }
                }
                lrges.add(e);
            }
        }
        //add
        for(Element e : am.getRoot().getElements()) {
            if(!refges.contains(e.getPeer()) && !lrges.contains(e))
                lrges.add(e);
        }
        //find remove global elements
        List<Integer> removeList = new ArrayList<Integer>();
        for(int i=0;i<lrges.size();i++) {
            Element e = lrges.get(i);
            if(refges.contains(e.getPeer())) {
                removeList.add(Integer.valueOf(i));
            } else if(!SchemaGeneratorUtil.fromSameSchemaModel(
                    e.getPeer(), am.getSchemaModel()))
                removeList.add(Integer.valueOf(i));
        }
        //finally remove unnecessary global elements from visit list
        for(int i=removeList.size()-1;i>=0;i--) {
            lrges.remove(removeList.get(i).intValue());
        }
        return lrges;
    }
    
    public static Element findOriginalElement(Element e) {
        if(e instanceof ElementRef)
            return ((ElementRef)e).getReferent();
        else if(e instanceof ElementProxy) {
            return findOriginalElement((Element) (((ElementProxy)e).getOriginal()));
        } else
            return e;
    }
    
    public static void checkPopulate(final AXIComponent source)
    throws IllegalArgumentException {
        if(source instanceof Attribute) return;
        if((source).getPeer() == null) {
            String name = (source instanceof Element)?((Element)source).getName():
                ((source instanceof ContentModel)?((ContentModel)source).getName():source.toString());
            throw new IllegalArgumentException("Component "+name+
                    " needs to be added to its parent, before its children can be populated");
        }
    }
    
    public static void populateElement(
            org.netbeans.modules.xml.schema.model.Element e, Element element) {
        if(e instanceof GlobalElement) {
            if(element.getAbstract())
                ((GlobalElement)e).setAbstract(Boolean.valueOf(element.getAbstract()));
            if(element.getPeer() instanceof GlobalElement &&
                    ((GlobalElement)element.getPeer()).getFinalEffective() != null &&
                    !((GlobalElement)element.getPeer()).getFinalEffective().isEmpty())
                ((GlobalElement)e).setFinal(((GlobalElement)element.getPeer()).getFinalEffective());
        }
        if(e instanceof LocalElement) {
            if(element.getForm() != null &&
                    element.getModel().getRoot().getElementFormDefault() !=
                    element.getForm())
                ((LocalElement)e).setForm(element.getForm());
        }
        if(e instanceof LocalElement ||
                e instanceof ElementReference) {
            if(element.getMinOccurs() != null &&
                    Integer.parseInt(element.getMinOccurs()) != 1) {
                if(e instanceof LocalElement)
                    ((LocalElement)e).setMinOccurs(
                            Integer.parseInt(element.getMinOccurs()));
                else if(e instanceof ElementReference)
                    ((ElementReference)e).setMinOccurs(
                            Integer.parseInt(element.getMinOccurs()));
            }
            if(element.getMaxOccurs() != null) {
                String maxValue = element.getMaxOccurs();
                if(maxValue.equals(NumberBase.UNBOUNDED_STRING) ||
                        Integer.parseInt(element.getMaxOccurs()) != 1) {
                    if(e instanceof LocalElement)
                        ((LocalElement)e).setMaxOccurs(element.getMaxOccurs());
                    else if(e instanceof ElementReference)
                        ((ElementReference)e).setMaxOccurs(element.getMaxOccurs());
                }
            }
        }
        if(e instanceof LocalElement ||
                e instanceof GlobalElement) {
            if(element.getFixed() != null)
                e.setFixed(element.getFixed());
            if(element.getDefault() != null)
                e.setDefault(element.getDefault());
            if(element.getNillable())
                e.setNillable(element.getNillable());
            if(element.getPeer() instanceof GlobalElement)
                if(e instanceof GlobalElement &&
                    ((GlobalElement)element.getPeer()).getBlockEffective() != null &&
                    !((GlobalElement)element.getPeer()).getBlockEffective().isEmpty())
                    ((GlobalElement)e).setBlock(((GlobalElement)element.getPeer()).getBlockEffective());
                else if(e instanceof LocalElement &&
                    ((GlobalElement)element.getPeer()).getBlockEffective() != null &&
                    !((GlobalElement)element.getPeer()).getBlockEffective().isEmpty())
                    ((LocalElement)e).setBlock(((GlobalElement)element.getPeer()).getBlockEffective());
                else if(element.getPeer() instanceof LocalElement)
                    if(e instanceof GlobalElement &&
                    ((LocalElement)element.getPeer()).getBlockEffective() != null &&
                    !((LocalElement)element.getPeer()).getBlockEffective().isEmpty())
                        ((GlobalElement)e).setBlock(((LocalElement)element.getPeer()).getBlockEffective());
                    else if(e instanceof LocalElement &&
                    ((LocalElement)element.getPeer()).getBlockEffective() != null &&
                    !((LocalElement)element.getPeer()).getBlockEffective().isEmpty())
                        ((LocalElement)e).setBlock(((LocalElement)element.getPeer()).getBlockEffective());
        }
        org.netbeans.modules.xml.schema.model.Element old =
                (org.netbeans.modules.xml.schema.model.Element) element.getPeer();
        if(old != null) {
            if(old.getAnnotation() != null) {
                Annotation a = copyAnnotation(old.getAnnotation());
                e.setAnnotation(a);
            }
            Collection<Constraint> constraints = old.getConstraints();//key, keyref, unique
            if(constraints != null)
                for(Constraint c:constraints) {
                    Constraint copy = copyConstraint((Constraint)c);
                    e.addConstraint(copy);
                }
        }
    }
    
    public static Annotation copyAnnotation(final Annotation old) {
        //workaround for pretty print
        List<SchemaComponent> childs = old.getChildren();
        List<SchemaComponent> copies = new ArrayList<SchemaComponent>();
        Annotation a = old.getModel().getFactory().createAnnotation();
        for(SchemaComponent child:childs) {
            copies.add((SchemaComponent)child.copy(a));
        }
        for(SchemaComponent child:copies) {
            if(child instanceof AppInfo)
                a.addAppInfo((AppInfo) child);
            else if(child instanceof Documentation)
                a.addDocumentation((Documentation) child);
        }
        return a;
    }
    
    public static Constraint copyConstraint(final Constraint old) {
        //workaround for pretty print
        if(old == null) return null;
        List<SchemaComponent> childs = old.getChildren();
        List<SchemaComponent> copies = new ArrayList<SchemaComponent>();
        Constraint c = null;
        if(old instanceof Unique)
            c = old.getModel().getFactory().createUnique();
        else if(old instanceof Key)
            c = old.getModel().getFactory().createKey();
        else if(old instanceof KeyRef)
            c = old.getModel().getFactory().createKeyRef();
        assert c != null;
        for(SchemaComponent child:childs) {
            copies.add((SchemaComponent)child.copy(c));
        }
        for(SchemaComponent child:copies) {
            if(child instanceof Selector)
                c.setSelector((Selector)child);
            else if(child instanceof Field)
                c.addField((Field) child);
        }
        return c;
    }
    
    public static void populateAttribute(
            org.netbeans.modules.xml.schema.model.Attribute attr, Attribute attribute) {
        if(attr instanceof LocalAttribute) {
            if(attribute.getForm() != null &&
                    attribute.getModel().getRoot().getAttributeFormDefault() !=
                    attribute.getForm())
                ((LocalAttribute)attr).setForm(attribute.getForm());
            if(attribute.getUse() != null &&
                    attribute.getUse() !=
                    org.netbeans.modules.xml.schema.model.Attribute.Use.OPTIONAL)
                ((LocalAttribute)attr).setUse(attribute.getUse());
        }
        if(attribute.getFixed() != null)
            attr.setFixed(attribute.getFixed());
        if(attribute.getDefault() != null)
            attr.setDefault(attribute.getDefault());
        org.netbeans.modules.xml.schema.model.Attribute old =
                (org.netbeans.modules.xml.schema.model.Attribute) attribute.getPeer();
        if(old != null && old.getAnnotation() != null) {
            Annotation a = copyAnnotation(old.getAnnotation());
            attr.setAnnotation(a);
        }
    }
    
    public static void populateCompositor(ComplexTypeDefinition ctd,
            Compositor compositor) {
        //set minoccurs
        if(compositor.getMinOccurs() != null &&
                Integer.parseInt(compositor.getMinOccurs()) != 1) {
            if(ctd instanceof Sequence) {
                Sequence seq = (Sequence) ctd;
                if(seq.getCardinality() != null)
                    seq.getCardinality().setMinOccurs(
                            Integer.valueOf(compositor.getMinOccurs()));
            } else if(ctd instanceof Choice) {
                Choice c = (Choice) ctd;
                if(c.getCardinality() != null)
                    c.getCardinality().setMinOccurs(Integer.valueOf(compositor.getMinOccurs()));
            } else if(ctd instanceof All) {
                All a = (All) ctd;
                a.setMinOccurs(Occur.ZeroOne.valueOfNumeric(
                        a.toString(), compositor.getMinOccurs()));
            }
        }
        
        //set maxoccurs
        if(compositor.getMaxOccurs() != null &&
                (compositor.getMaxOccurs().equals(NumberBase.UNBOUNDED_STRING) ||
                Integer.parseInt(compositor.getMaxOccurs()) != 1)) {
            if(ctd instanceof Sequence) {
                Sequence seq = (Sequence) ctd;
                if(seq.getCardinality() != null)
                    seq.getCardinality().setMaxOccurs(compositor.getMaxOccurs());
            } else if(ctd instanceof Choice) {
                Choice c = (Choice) ctd;
                if(c.getCardinality() != null)
                    c.getCardinality().setMaxOccurs(compositor.getMaxOccurs());
            }
        }
        ComplexTypeDefinition old = (ComplexTypeDefinition) compositor.getPeer();
        if(old != null && old.getAnnotation() != null) {
            Annotation a = copyAnnotation(old.getAnnotation());
            ctd.setAnnotation(a);
        }
    }
    
    public static void populateContentModel(SchemaComponent sc, ContentModel cm) {
        SchemaComponent old = cm.getPeer();
        if(old != null && old.getAnnotation() != null) {
            Annotation a = copyAnnotation(old.getAnnotation());
            sc.setAnnotation(a);
        }
    }
    
    public static boolean isSimpleElement(Element element) {
        return element.getType() instanceof Datatype ||
                (element.getType() == null && element.getChildren().size() == 0);
    }
    
    public static boolean isSimpleElementStructure(Element e) {
        if(e.getAttributes().size() > 0 ||
                e.getChildren(Compositor.class).size() > 1 ||
                (e.getCompositor() != null &&
                e.getCompositor().getChildren().size() > 1))
            return false;
        else
            return true;
    }
    
    public static boolean isGlobalElement(AXIComponent axiparent) {
        return axiparent instanceof Element &&
                axiparent.getPeer() instanceof GlobalElement;
    }
    
    public static boolean isIdentical(SchemaComponent sc1, SchemaComponent sc2) {
        return compareElement(sc1.getPeer(), sc2.getPeer(), true) &&
                sc1.getChildren().size() == 0 &&
                (sc1.getChildren().size() == sc2.getChildren().size());
    }
    
    public static boolean hasProxyChild(Element element) {
        return element.getType() instanceof ContentModel;
    }
    
    public static boolean compareElement(org.w3c.dom.Element n1,
            org.w3c.dom.Element n2, boolean identical) {
        String qName1 = n1.getLocalName();
        String qName2 = n2.getLocalName();
        String ns1 = ((Node)n1).getNamespaceURI();
        String ns2 = ((Node)n2).getNamespaceURI();
        
        if (qName1.intern() !=  qName2.intern())
            return false;
        if (!(ns1 == null && ns2 == null) &&
                !(ns1 != null && ns2 != null && ns1.intern() == ns2.intern()))
            return false;
        
        return compareAttr(n1, n2, identical);
    }
    
    public static boolean compareAttr(org.w3c.dom.Element n1,
            org.w3c.dom.Element n2, boolean identical) {
        NamedNodeMap attrs1 = n1.getAttributes();
        NamedNodeMap attrs2 = n2.getAttributes();
        
        List<String> nameSet = new ArrayList<String>();
        nameSet.add( "id" );
        nameSet.add( "name" );
        nameSet.add( "ref" );
        
        if(nameSet.isEmpty())
            return true;
        else if(attrs1.getLength() == 0 && attrs2.getLength() == 0)
            return true;
        else if(identical && attrs1.getLength() != attrs2.getLength())
            return false;
        
        int matchCount = 0;
        int unmatchCount = 0;
        for(String name:nameSet) {
            Node attr1 = (Node) attrs1.getNamedItem(name);
            Node attr2 = (Node) attrs2.getNamedItem(name);
            if(attr1 == null && attr2 == null)
                continue;
            else if(attr1 != null && attr2 != null){
                if(attr2.getNodeValue().intern() != attr1.getNodeValue().intern())
                    unmatchCount++;
                else
                    matchCount++;
            } else
                unmatchCount++;
            //check for exact match
            if(matchCount == 1)
                return true;
            
            //check for rename
            if(unmatchCount == 1 && attrs1.getLength() == attrs2.getLength())
                return false;
        }
        
        //no attributes in attrs1 and attrs2 that match nameSet
        if ( matchCount == 0 && unmatchCount == 0 )
            return true;
        
        return false;
    }
    
    public static boolean fromSameSchemaModel(AXIComponent c, SchemaModel sm) {
        return c.getPeer()!=null?
            fromSameSchemaModel(c.getPeer(), sm):
            c.getModel().getSchemaModel() == sm;
    }
    
    public static boolean fromSameSchemaModel(SchemaComponent c, SchemaModel sm) {
        return c.getModel() == sm;
    }    
}
