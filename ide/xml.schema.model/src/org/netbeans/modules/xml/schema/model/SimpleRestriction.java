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

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This interface includes the common facet handling in restrictions.
 * @author ChrisWebster
 */
public interface SimpleRestriction extends SchemaComponent {
    public static final String ENUMERATION_PROPERTY = "enumerations";
    public static final String PATTERN_PROPERTY = "patterns";
    public static final String MIN_EXCLUSIVE_PROPERTY = "minExclusives";
    public static final String MIN_LENGTH_PROPERTY = "minLengths";
    public static final String MAX_LENGTH_PROPERTY  = "maxLengths";
    public static final String FRACTION_DIGITS_PROPERTY = "fractionDigits";
    public static final String WHITESPACE_PROPERTY = "whitespaces";
    public static final String MAX_INCLUSIVE_PROPERTY = "maxInclusives";
    public static final String TOTAL_DIGITS_PROPERTY = "totalDigits";
    public static final String LENGTH_PROPERTY = "lengths";
    public static final String MIN_INCLUSIVE_PROPERTY = "minInclusives";
    public static final String MAX_EXCLUSIVE_PROPERTY = "maxExclusives";
    public static final String BASE_PROPERTY = "base";
    public static final String INLINETYPE_PROPERTY  = "inlinetype";
    
    Collection<TotalDigits> getTotalDigits();
    void addTotalDigit(TotalDigits td);
    void removeTotalDigit(TotalDigits td);
    
    Collection<MinExclusive> getMinExclusives();
    void addMinExclusive(MinExclusive me);
    void removeMinExclusive(MinExclusive me);
    
    Collection<MinInclusive> getMinInclusives();
    void addMinInclusive(MinInclusive me);
    void removeMinInclusive(MinInclusive me);
    
    Collection<MinLength> getMinLengths();
    void addMinLength(MinLength ml);
    void removeMinLength(MinLength ml);
    
    Collection<MaxLength> getMaxLengths();
    void addMaxLength(MaxLength ml);
    void removeMaxLength(MaxLength ml);
    
    Collection<Pattern> getPatterns();
    void addPattern(Pattern p);
    void removePattern(Pattern p);
    
    Collection<MaxExclusive> getMaxExclusives();
    void addMaxExclusive(MaxExclusive me);
    void removeMaxExclusive(MaxExclusive me);
    
    Collection<MaxInclusive> getMaxInclusives();
    void addMaxInclusive(MaxInclusive me);
    void removeMaxInclusive(MaxInclusive me);
    
    Collection<Length> getLengths();
    void addLength(Length me);
    void removeLength(Length me);
    
    Collection<Whitespace> getWhitespaces();
    void addWhitespace(Whitespace me);
    void removeWhitespace(Whitespace me);
    
    Collection<FractionDigits> getFractionDigits();
    void addFractionDigits(FractionDigits fd);
    void removeFractionDigits(FractionDigits fd);
    
    Collection<Enumeration> getEnumerations();
    void addEnumeration(Enumeration fd);
    void removeEnumeration(Enumeration fd);
    
    LocalSimpleType getInlineType();
    void setInlineType(LocalSimpleType aSimpleType);
    
}
