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
