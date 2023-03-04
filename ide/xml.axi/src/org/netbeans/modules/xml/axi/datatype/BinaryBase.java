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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.axi.datatype.Datatype.Facet;
import org.netbeans.modules.xml.schema.model.Whitespace;

/**
 *
 * @author Ayub Khan
 */
public abstract class BinaryBase extends Datatype {
    
    static List<Facet> applicableFacets;
    
    private Datatype.Kind kind;
    
    protected boolean hasFacets;
    
    private boolean isList;
    
    private List<Integer> lengths;
    
    private List<Integer> minLengths;
    
    private List<Integer> maxLengths;
    
    private List<String> patterns;
    
    protected List<String> enumerations;
    
    private List<Whitespace.Treatment> whitespaces;
    
    
    /** Creates a new instance of TypeBase */
    public BinaryBase(Kind kind) {
        this.kind = kind;
    }
    
    public Kind getKind() {
        return kind;
    }
    
    public synchronized List<Facet> getApplicableFacets() {
        if(applicableFacets == null) {
            List<Facet> facets = new ArrayList<Facet>();
            facets.add(Facet.LENGTH);
            facets.add(Facet.MINLENGTH);
            facets.add(Facet.MAXLENGTH);
            facets.add(Facet.PATTERN);
            facets.add(Facet.ENUMERATION);
            facets.add(Facet.WHITESPACE);
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
     * returns length (this corresponds to the value of length facet in schema)
     *
     * @return length
     */
    public List<Integer> getLengths() {
        return lengths;
    }
    
    /*
     * returns minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @return minLength
     */
    public List<Integer> getMinLengths() {
        return minLengths;
    }
    
    /*
     * returns maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @return maxLength
     */
    public List<Integer> getMaxLengths() {
        return maxLengths;
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
    public List<String> getEnumerations() {
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
     * set length (this corresponds to the value of length facet in schema)
     *
     * @param length
     */
    public void addLength(int length) {
        if(lengths == null) {
            lengths = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.lengths.add(Integer.valueOf(length));
    }
    
    /*
     * set minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @param minLength
     */
    public void addMinLength(int minLength) {
        if(minLengths == null) {
            minLengths = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.minLengths.add(Integer.valueOf(minLength));
    }
    
    /*
     * set maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @param maxLength
     */
    public void addMaxLength(int maxLength) {
        if(maxLengths == null) {
            maxLengths = new ArrayList<Integer>(1);
            hasFacets = true;
        }
        this.maxLengths.add(Integer.valueOf(maxLength));
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
     * returns enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void addEnumeration(String enumeration) {
        if(enumerations == null) {
            enumerations = new ArrayList<String>(1);
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
     * remove length (this corresponds to the value of length facet in schema)
     *
     * @param length
     */
    public void removeLength(Number length) {
        if(lengths != null)
            lengths.remove(length);
    }
    
    /*
     * remove minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @param minLength
     */
    public void removeMinLength(Number minLength) {
        if(minLengths != null)
            minLengths.remove(minLength);
    }
    
    /*
     * set maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @param maxLength
     */
    public void removeMaxLength(Number maxLength) {
        if(maxLengths != null)
            maxLengths.remove(maxLength);
    }
    
    /*
     * set pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void removePattern(String pattern) {
        if(patterns != null)
            patterns.remove(pattern);
    }
    
    /*
     * remove enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void removeEnumeration(String enumeration) {
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
}
