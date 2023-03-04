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

package org.netbeans.modules.xml.axi.datatype;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.axi.datatype.Datatype.Facet;
import org.netbeans.modules.xml.schema.model.Whitespace;

/**
 *
 * @author Ayub Khan
 */
public abstract class NumberBase extends Datatype {
    
    public static final Number UNBOUNDED_VALUE = Double.MAX_VALUE;
    
    public static final String UNBOUNDED_STRING = "unbounded"; //NoI18n
    
    static List<Facet> applicableFacets;
    
    private Datatype.Kind kind;
    
    private boolean hasFacets;
    
    private boolean isList;
    
    private List<Integer> totalDigits;
    
    private List<Integer> fractionDigits;
    
    private List<String> patterns;
    
    private List<Whitespace.Treatment> whitespaces;
    
    private List<Number> enumerations;
    
    private List<Number> maxInclusives;
    
    private List<Number> maxExclusives;
    
    private List<Number> minInclusives;
    
    private List<Number> minExclusives;
    
    /** Creates a new instance of NumberBase */
    public NumberBase(Datatype.Kind kind) {
        this.kind = kind;
    }
    
    public Datatype.Kind getKind() {
        return kind;
    }
    
    public synchronized List<Facet> getApplicableFacets() {
        if(applicableFacets == null) {
            List<Facet> facets = new ArrayList<Facet>();
            facets.add(Facet.TOTATDIGITS);
            facets.add(Facet.FRACTIONDIGITS);
            facets.add(Facet.PATTERN);
            facets.add(Facet.WHITESPACE);
            facets.add(Facet.ENUMERATION);
            facets.add(Facet.MAXINCLUSIVE);
            facets.add(Facet.MAXEXCLUSIVE);
            facets.add(Facet.MININCLUSIVE);
            facets.add(Facet.MINEXCLUSIVE);
            applicableFacets = Collections.unmodifiableList(facets);
        }
        return applicableFacets;
    }
    
    public boolean hasFacets() {
        return hasFacets;
    }
    
    public boolean isList() {
        return isList;
    }
    
    public void setIsList(boolean isList) {
        this.isList = isList;
    }
    
    /*
     * returns pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @return pattern
     */
    public List<String> getPatterns() {
        return patterns;
    }
    
    /*
     * returns enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @return enumeration
     */
    public List<Number> getEnumerations() {
        return enumerations;
    }
    
    /*
     * returns whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @return whitespaces
     */
    public List<Whitespace.Treatment> getWhiteSpaces() {
        return whitespaces;
    }
    
    /*
     * returns total digits value (this corresponds to the value of totalDigits facet in schema)
     *
     * @return totalDigits
     */
    public List<Integer> getTotalDigits() {
        return totalDigits;
    }
    
    /*
     * returns fraction digits value (this corresponds to the value of fractionDigits facet in schema)
     *
     * @return fractionDigits
     */
    public List<Integer> getFractionDigits() {
        return fractionDigits;
    }
    
    /*
     * returns maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @return maxInclusive
     */
    public List<Number> getMaxInclusives() {
        return maxInclusives;
    }
    
    /*
     * returns maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @return maxExclusive
     */
    public List<Number> getMaxExclusives() {
        return maxExclusives;
    }
    
    /*
     * returns minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @return minInclusive
     */
    public List<Number> getMinInclusives() {
        return minInclusives;
    }
    
    /*
     * returns minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @return minExclusive
     */
    public List<Number> getMinExclusives() {
        return minExclusives;
    }
    
    /*
     * set pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void addPattern(String pattern) {
        if(patterns == null) {
            patterns = new ArrayList<String>(1);
            hasFacets = true;
        }
        this.patterns.add(pattern);
    }
    
    /*
     * set enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void addEnumeration(Number enumeration) {
        if(enumerations == null) {
            enumerations = new ArrayList<Number>(1);
            hasFacets = true;
        }
        this.enumerations.add(enumeration);
    }
    
    /*
     * set whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @param whitespace
     */
    public void addWhitespace(Whitespace.Treatment whitespace) {
        if(whitespaces == null) {
            whitespaces = new ArrayList<Whitespace.Treatment>(1);
            hasFacets = true;
        }
        this.whitespaces.add(whitespace);
    }
    
    /*
     * set total digits value (this corresponds to the value of totalDigits facet in schema)
     *
     * @param totalDigits
     */
    public void addTotalDigits(int totalDigits ) {
        if(this.totalDigits == null) {
            this.totalDigits = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.totalDigits.add(Integer.valueOf(totalDigits));
    }
    
    /*
     * set fraction digits value (this corresponds to the value of fractionDigits facet in schema)
     *
     * @param fractionDigits
     */
    public void addFractionDigits(int fractionDigits) {
        if(this.fractionDigits == null) {
            this.fractionDigits = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.fractionDigits.add(Integer.valueOf(fractionDigits));
    }
    
    /*
     * set maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @param maxInclusive
     */
    public void addMaxInclusive(Number maxInclusive) {
        if(maxInclusives == null) {
            maxInclusives = new ArrayList<Number>(1);
            hasFacets = true;
        }
        this.maxInclusives.add(maxInclusive);
    }
    
    /*
     * set maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @param maxExclusive
     */
    public void addMaxExclusive(Number maxExclusive) {
        if(maxExclusives == null) {
            maxExclusives = new ArrayList<Number>(1);
            hasFacets = true;
        }
        this.maxExclusives.add(maxExclusive);
    }
    
    /*
     * set minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @param minInclusive
     */
    public void addMinInclusive(Number minInclusive) {
        if(minInclusives == null) {
            minInclusives = new ArrayList<Number>(1);
            hasFacets = true;
        }
        this.minInclusives.add(minInclusive);
    }
    
    /*
     * set minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @param minExclusive
     */
    public void addMinExclusive(Number minExclusive) {
        if(minExclusives == null) {
            minExclusives = new ArrayList<Number>(1);
            hasFacets = true;
        }
        this.minExclusives.add(minExclusive);
    }
    
    /*
     * remove pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void removePattern(String pattern) {
        if(patterns != null)
            patterns.remove(pattern);
    }
    
    /*
     * set enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void removeEnumeration(Number enumeration) {
        if(enumerations != null)
            enumerations.remove(enumeration);
    }
    
    /*
     * set whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @param whitespace
     */
    public void removeWhitespace(Whitespace.Treatment whitespace) {
        if(whitespaces != null)
            whitespaces.remove(whitespace);
    }
    
    /*
     * set total digits value (this corresponds to the value of totalDigits facet in schema)
     *
     * @param totalDigits
     */
    public void removeTotalDigits(Number totalDigits ) {
        if(this.totalDigits != null)
            this.totalDigits.remove(totalDigits);
    }
    
    /*
     * set fraction digits value (this corresponds to the value of fractionDigits facet in schema)
     *
     * @param fractionDigits
     */
    public void removeFractionDigits(Number fractionDigits) {
        if(this.fractionDigits != null)
            this.fractionDigits.remove(fractionDigits);
    }
    
    /*
     * set maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @param maxInclusive
     */
    public void removeMaxInclusive(Number maxInclusive) {
        if(maxInclusives != null)
            maxInclusives.remove(maxInclusive);
    }
    
    /*
     * set maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @param maxExclusive
     */
    public void removeMaxExclusive(Number maxExclusive) {
        if(maxExclusives != null)
            maxExclusives.remove(maxExclusive);
    }
    
    /*
     * set minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @param minInclusive
     */
    public void removeMinInclusive(Number minInclusive) {
        if(minInclusives != null)
            minInclusives.remove(minInclusive);
    }
    
    /*
     * set minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @param minExclusive
     */
    public void removeMinExclusive(Number minExclusive) {
        if(minExclusives != null)
            minExclusives.remove(minExclusive);
    }
    
    public static Number toNumber(String value) {
        Number n = null;
        if(value.equals(UNBOUNDED_STRING))
            n = UNBOUNDED_VALUE;
        else {
            try{
                n = new BigDecimal(value);
            } catch(Throwable th) {
                n = 0;
            }
        }
        return n;
    }
    
    public static String toXMLString(Number val) {
        if(val == UNBOUNDED_VALUE)
            return UNBOUNDED_STRING;
        else
            return String.valueOf(val);
    }
}
