/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SimpleRestriction;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.FractionDigits;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Length;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.MaxExclusive;
import org.netbeans.modules.xml.schema.model.MaxInclusive;
import org.netbeans.modules.xml.schema.model.MaxLength;
import org.netbeans.modules.xml.schema.model.MinExclusive;
import org.netbeans.modules.xml.schema.model.MinInclusive;
import org.netbeans.modules.xml.schema.model.MinLength;
import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TotalDigits;
import org.netbeans.modules.xml.schema.model.Whitespace;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public abstract class CommonSimpleRestrictionImpl extends SchemaComponentImpl implements SimpleRestriction{
    
    /** Creates a new instance of CommonSimpleRestrictionImpl */
    public CommonSimpleRestrictionImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }
    
    public void addEnumeration(Enumeration fd) {
        addBefore(ENUMERATION_PROPERTY, fd, getAttributeClasses());
    }
    
    public void removeEnumeration(Enumeration fd) {
        removeChild(ENUMERATION_PROPERTY, fd);
    }
    
    public Collection<Enumeration> getEnumerations() {
        return getChildren(Enumeration.class);
    }
    
    public void removePattern(Pattern p) {
        removeChild(PATTERN_PROPERTY, p);
    }
    
    public void addPattern(Pattern p) {
        addBefore(PATTERN_PROPERTY, p, getAttributeClasses());
    }
    
    public Collection<Pattern> getPatterns() {
        return getChildren(Pattern.class);
    }
    
    public void removeMinExclusive(MinExclusive me) {
        removeChild(MIN_EXCLUSIVE_PROPERTY, me);
    }
    
    public void addMinExclusive(MinExclusive me) {
        addBefore(MIN_EXCLUSIVE_PROPERTY, me, getAttributeClasses());
    }
    
    public Collection<MinExclusive> getMinExclusives() {
        return getChildren(MinExclusive.class);
    }
    
    public void removeMinLength(MinLength ml) {
        removeChild(MIN_LENGTH_PROPERTY, ml);
    }
    
    public void addMinLength(MinLength ml) {
        addBefore(MIN_LENGTH_PROPERTY, ml, getAttributeClasses());
    }
    
    public Collection<MinLength> getMinLengths() {
        return getChildren(MinLength.class);
    }
    
    public void removeMaxLength(MaxLength ml) {
        removeChild(MAX_LENGTH_PROPERTY, ml);
    }
    
    public void addMaxLength(MaxLength ml) {
        addBefore(MAX_LENGTH_PROPERTY, ml, getAttributeClasses());
    }
    
    public Collection<MaxLength> getMaxLengths() {
        return getChildren(MaxLength.class);
    }
    
    public void removeFractionDigits(FractionDigits fd) {
        removeChild(FRACTION_DIGITS_PROPERTY, fd);
    }
    
    public void addFractionDigits(FractionDigits fd) {
        addBefore(FRACTION_DIGITS_PROPERTY, fd, getAttributeClasses());
    }
    
    public Collection<FractionDigits> getFractionDigits() {
        return getChildren(FractionDigits.class);
    }
    
    public void removeWhitespace(Whitespace me) {
        removeChild(WHITESPACE_PROPERTY, me);
    }
    
    public void addWhitespace(Whitespace me) {
        addBefore(WHITESPACE_PROPERTY, me, getAttributeClasses());
    }
    
    public Collection<Whitespace> getWhitespaces() {
        return getChildren(Whitespace.class);
    }
    
    
    public void removeMaxInclusive(MaxInclusive me) {
        removeChild(MAX_INCLUSIVE_PROPERTY, me);
    }
    
    public void addMaxInclusive(MaxInclusive me) {
        addBefore(MAX_INCLUSIVE_PROPERTY, me, getAttributeClasses());
    }
    
    public Collection<MaxInclusive> getMaxInclusives() {
        return getChildren(MaxInclusive.class);
    }
    
    public void removeTotalDigit(TotalDigits td) {
        removeChild(TOTAL_DIGITS_PROPERTY, td);
    }
    
    public void addTotalDigit(TotalDigits td) {
        addBefore(TOTAL_DIGITS_PROPERTY, td, getAttributeClasses());
    }
    
    public Collection<TotalDigits> getTotalDigits() {
        return getChildren(TotalDigits.class);
    }
    
    public void removeLength(Length me) {
        removeChild(LENGTH_PROPERTY , me);
    }
    
    public void addLength(Length me) {
        addBefore(LENGTH_PROPERTY, me, getAttributeClasses());
    }
    
    public Collection<Length> getLengths() {
        return getChildren(Length.class);
    }
    
    public void removeMinInclusive(MinInclusive me) {
        removeChild(MIN_INCLUSIVE_PROPERTY, me);
    }
    
    public void addMinInclusive(MinInclusive me) {
        addBefore(MIN_INCLUSIVE_PROPERTY, me, getAttributeClasses());
    }
    
    public Collection<MinInclusive> getMinInclusives() {
        return getChildren(MinInclusive.class);
    }
    
    public void removeMaxExclusive(MaxExclusive me) {
        removeChild(MAX_EXCLUSIVE_PROPERTY, me);
    }
    
    public void addMaxExclusive(MaxExclusive me) {
        addBefore(MAX_EXCLUSIVE_PROPERTY, me, getAttributeClasses());
    }
    
    public Collection<MaxExclusive> getMaxExclusives() {
        return getChildren(MaxExclusive.class);
    }
    
    public void setInlineType(LocalSimpleType aSimpleType) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        setChild(LocalSimpleType.class, INLINETYPE_PROPERTY, aSimpleType, list);
    }
    
    public LocalSimpleType getInlineType() {
        Collection<LocalSimpleType> elements = getChildren(LocalSimpleType.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        return null;
    }
    
    private List<Class<? extends SchemaComponent>> getAttributeClasses(){
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
        list.add(Attribute.class);
        list.add(AttributeGroupReference.class);
        list.add(AnyAttribute.class);
        
        return list;
    }
}
