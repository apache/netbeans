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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.axi.datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.*;

/**
 * Class represents a datatype in AXI model, contains a reference its peer in
 * schema ie., a simpleType (either a primitive datatype or a derived type)
 *
 * @author Ayub Khan
 */
public abstract class Datatype implements AXIType {
    
    List<String> enumerations = new ArrayList<String>();
        
    /*
     * returns the kind of this datatype
     *
     * @return qName
     */
    public abstract Kind getKind();
    
    /*
     * returns the name of this datatype
     *
     * @return name
     */
    public String getName() {
        return getKind().getName();
    }
    
    /**
     * Allows a visitor to visit this Element.
     */
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    public abstract List<Facet> getApplicableFacets();
    
    public abstract boolean hasFacets();
    
    public abstract boolean isList();
    
    public abstract void setIsList(boolean b);
    
    public boolean isPrimitive() {
        return !hasFacets() && !(this instanceof UnionType);
    }
    
    /*
     * returns pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @return pattern
     */
    public List<? extends String> getPatterns() {
        return Collections.emptyList();
    }
    
    /*
     * returns enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @return enumeration
     */
    public List<? extends Object> getEnumerations() {
        if(enumerations == null)
            return Collections.emptyList();
        return enumerations;
    }
    
    /*
     * returns whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @return whitespaces
     */
    public List<Whitespace.Treatment> getWhiteSpaces() {
        return Collections.emptyList();
    }
    
    /*
     * returns total digits value (this corresponds to the value of totalDigits facet in schema)
     *
     * @return totalDigits
     */
    public List<? extends Number> getTotalDigits() {
        return Collections.emptyList();
    }
    
    /*
     * returns fraction digits value (this corresponds to the value of fractionDigits facet in schema)
     *
     * @return fractionDigits
     */
    public List<? extends Number> getFractionDigits() {
        return Collections.emptyList();
    }
    
    /*
     * returns maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @return maxInclusive
     */
    public List<? extends Object> getMaxInclusives() {
        return Collections.emptyList();
    }
    
    /*
     * returns maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @return maxExclusive
     */
    public List<? extends Object> getMaxExclusives() {
        return Collections.emptyList();
    }
    
    /*
     * returns minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @return minInclusive
     */
    public List<? extends Object> getMinInclusives() {
        return Collections.emptyList();
    }
    
    /*
     * returns minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @return minExclusive
     */
    public List<? extends Object> getMinExclusives() {
        return Collections.emptyList();
    }
    
    /*
     * returns length (this corresponds to the value of length facet in schema)
     *
     * @return length
     */
    public List<? extends Number> getLengths() {
        return Collections.emptyList();
    }
    
    /*
     * returns minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @return minLength
     */
    public List<? extends Number> getMinLengths() {
        return Collections.emptyList();
    }
    
    /*
     * returns maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @return maxLength
     */
    public List<? extends Number> getMaxLengths() {
        return Collections.emptyList();
    }
    
    /*
     * set pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void addPattern(String pattern) {
    }
    
    /*
     * set enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void addEnumeration(Object enumeration) {
        if(!(enumeration instanceof String))
            return;
        if(enumerations == null)
            enumerations = new ArrayList<String>();
        enumerations.add((String)enumeration);
    }
    
    /*
     * set whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @param whitespace
     */
    public void addWhitespace(Whitespace.Treatment whitespace) {
    }
    
    /*
     * set total digits value (this corresponds to the value of totalDigits facet in schema)
     *
     * @param totalDigits
     */
    public void addTotalDigits(int totalDigits ) {
    }
    
    /*
     * set fraction digits value (this corresponds to the value of fractionDigits facet in schema)
     *
     * @param fractionDigits
     */
    public void addFractionDigits(int fractionDigits) {
    }
    
    /*
     * set maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @param maxInclusive
     */
    public void addMaxInclusive(Object maxInclusive) {
    }
    
    /*
     * set maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @param maxExclusive
     */
    public void addMaxExclusive(Object maxExclusive) {
    }
    
    /*
     * set minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @param minInclusive
     */
    public void addMinInclusive(Object minInclusive) {
    }
    
    /*
     * set minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @param minExclusive
     */
    public void addMinExclusive(Object minExclusive) {
    }
    
    /*
     * set length (this corresponds to the value of length facet in schema)
     *
     * @param length
     */
    public void addLength(int length) {
    }
    
    /*
     * set minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @param minLength
     */
    public void addMinLength(int minLength) {
    }
    
    /*
     * set maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @param maxLength
     */
    public void addMaxLength(int maxLength) {
    }
    
    /*
     * remove pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void removePattern(String pattern) {
    }
    
    /*
     * remove enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void removeEnumeration(Object enumeration) {
    }
    
    /*
     * remove whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @param whitespace
     */
    public void removeWhitespace(Whitespace.Treatment whitespace) {
    }
    
    /*
     * remove total digits value (this corresponds to the value of totalDigits facet in schema)
     *
     * @param totalDigits
     */
    public void removeTotalDigits(Number totalDigits ) {
    }
    
    /*
     * remove fraction digits value (this corresponds to the value of fractionDigits facet in schema)
     *
     * @param fractionDigits
     */
    public void removeFractionDigits(Number fractionDigits) {
    }
    
    /*
     * remove maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @param maxInclusive
     */
    public void removeMaxInclusive(Object maxInclusive) {
    }
    
    /*
     * remove maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @param maxExclusive
     */
    public void removeMaxExclusive(Object maxExclusive) {
    }
    
    /*
     * remove minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @param minInclusive
     */
    public void removeMinInclusive(Object minInclusive) {
    }
    
    /*
     * remove minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @param minExclusive
     */
    public void removeMinExclusive(Object minExclusive) {
    }
    
    /*
     * remove length (this corresponds to the value of length facet in schema)
     *
     * @param length
     */
    public void removeLength(Number length) {
    }
    
    /*
     * remove minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @param minLength
     */
    public void removeMinLength(Number minLength) {
    }
    
    /*
     * remove maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @param maxLength
     */
    public void removeMaxLength(Number maxLength) {
    }
    
    public String toString() {
        return getName();
    }
    
    public enum Kind {
        STRING("string"),
        NORMALIZED_STRING("normalizedString"),
        TOKEN("token"),
        LANGUAGE("language"),
        NAME("Name"),
        NMTOKEN("NMTOKEN"),
        NCNAME("NCName"),
        NMTOKENS("NMTOKENS"),
        ID("ID"),
        IDREF("IDREF"),
        ENTITY("ENTITY"),
        IDREFS("IDREFS"),
        ENTITIES("ENTITIES"),
        DECIMAL("decimal"),
        INTEGER("integer"),
        NON_POSITIVE_INTEGER("nonPositiveInteger"),
        LONG("long"),
        NON_NEGATIVE_INTEGER("nonNegativeInteger"),
        NEGATIVE_INTEGER("negativeInteger"),
        INT("int"),
        SHORT("short"),
        BYTE("byte"),
        UNSIGNED_LONG("unsignedLong"),
        UNSIGNED_INT("unsignedInt"),
        UNSIGNED_SHORT("unsignedShort"),
        UNSIGNED_BYTE("unsignedByte"),
        POSITIVE_INTEGER("positiveInteger"),
        DURATION("duration"),
        DATE_TIME("dateTime"),
        TIME("time"),
        DATE("date"),
        G_YEAR_MONTH("gYearMonth"),
        G_YEAR("gYear"),
        G_MONTH_DAY("gMonthDay"),
        G_DAY("gDay"),
        G_MONTH("gMonth"),
        BOOLEAN("boolean"),
        BASE64_BINARY("base64Binary"),
        HEX_BINARY("hexBinary"),
        FLOAT("float"),
        DOUBLE("double"),
        ANYURI("anyURI"),
        ANYTYPE("anyType"),
        QNAME("QName"),
        NOTATION("NOTATION"),
        UNION("UNION");
        
        String name;
        
        Kind(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }        
    }
    
    public enum Facet {
        LENGTH("length", Length.class),
        MINLENGTH("minLength", MinLength.class),
        MAXLENGTH("maxLength", MaxLength.class),
        PATTERN("pattern", Pattern.class),
        ENUMERATION("enumeration", Enumeration.class),
        WHITESPACE("whiteSpace", Whitespace.class),
        TOTATDIGITS("totalDigits", TotalDigits.class),
        FRACTIONDIGITS("fractionDigits", FractionDigits.class),
        MAXINCLUSIVE("maxInclusive", MaxInclusive.class),
        MAXEXCLUSIVE("maxExclusive", MaxExclusive.class),
        MININCLUSIVE("minInclusive", MinInclusive.class),
        MINEXCLUSIVE("minExclusive", MinExclusive.class);
        
        String name;
        Class<? extends SchemaComponent> type;
        
        Facet(String name, Class<? extends SchemaComponent> type) {
            this.name = name;
            this.type = type;
        }
        
        public String getName() {
            return name;
        }
        
        public Class<? extends SchemaComponent> getComponentType() {
            return type;
        }
    }
    
}
