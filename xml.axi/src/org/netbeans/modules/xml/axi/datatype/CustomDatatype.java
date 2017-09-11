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

import java.util.List;
import org.netbeans.modules.xml.schema.model.Whitespace;

/**
 *
 * @author Ayub Khan
 */
public class CustomDatatype extends Datatype {
    
    private String name;
    private Datatype base;
    
    /** Creates a new instance of CustomDatatype */
    public CustomDatatype(String name) {
        this.name = name;
    }
    
    /** Creates a new instance of CustomDatatype */
    public CustomDatatype(String name, Datatype baseType) {
        this.name = name;
        this.base = baseType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Datatype getBase() {
        return base;
    }
    
    public void setBase(Datatype baseType) {
        this.base = baseType;
    }
    
    public Datatype.Kind getKind() {
        return getBase().getKind();
    }
    
    public List<Datatype.Facet> getApplicableFacets() {
        return getBase().getApplicableFacets();
    }
    
    public boolean hasFacets() {
        return getBase().hasFacets();
    }
    
    public boolean isList() {
        return getBase().isList();
    }
    
    public void setIsList(boolean b) {
        getBase().setIsList(b);
    }
    
    /*
     * returns pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @return pattern
     */
    public List<? extends String> getPatterns() {
        return getBase().getPatterns();
    }
    
    /*
     * returns enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @return enumeration
     */
    public List<? extends Object> getEnumerations() {
        return getBase().getEnumerations();
    }
    
    /*
     * returns whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @return whitespaces
     */
    public List<Whitespace.Treatment> getWhiteSpaces() {
        return getBase().getWhiteSpaces();
    }
    
    /*
     * returns total digits value (this corresponds to the value of totalDigits facet in schema)
     *
     * @return totalDigits
     */
    public List<? extends Number> getTotalDigits() {
        return getBase().getTotalDigits();
    }
    
    /*
     * returns fraction digits value (this corresponds to the value of fractionDigits facet in schema)
     *
     * @return fractionDigits
     */
    public List<? extends Number> getFractionDigits() {
        return getBase().getFractionDigits();
    }
    
    /*
     * returns maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @return maxInclusive
     */
    public List<? extends Object> getMaxInclusives() {
        return getBase().getMaxInclusives();
    }
    
    /*
     * returns maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @return maxExclusive
     */
    public List<? extends Object> getMaxExclusives() {
        return getBase().getMaxExclusives();
    }
    
    /*
     * returns minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @return minInclusive
     */
    public List<? extends Object> getMinInclusives() {
        return getBase().getMinInclusives();
    }
    
    /*
     * returns minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @return minExclusive
     */
    public List<? extends Object> getMinExclusives() {
        return getBase().getMinExclusives();
    }
    
    /*
     * returns length (this corresponds to the value of length facet in schema)
     *
     * @return length
     */
    public List<? extends Number> getLengths() {
        return getBase().getLengths();
    }
    
    /*
     * returns minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @return minLength
     */
    public List<? extends Number> getMinLengths() {
        return getBase().getMinLengths();
    }
    
    /*
     * returns maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @return maxLength
     */
    public List<? extends Number> getMaxLengths() {
        return getBase().getMaxLengths();
    }
    
    /*
     * set pattern value (this corresponds to the value of pattern facet in schema)
     *
     * @param pattern
     */
    public void addPattern(String pattern) {
        getBase().addPattern(pattern);
    }
    
    /*
     * set enumeration values (this corresponds to the values of enumeration facets in schema)
     *
     * @param enumeration
     */
    public void addEnumeration(Object enumeration) {
        getBase().addEnumeration(enumeration);
    }
    
    /*
     * set whitespace value (this corresponds to the value of whitespace facet in schema)
     *
     * @param whitespace
     */
    public void addWhitespace(Whitespace.Treatment whitespace) {
        getBase().addWhitespace(whitespace);
    }
    
    /*
     * set total digits value (this corresponds to the value of totalDigits facet in schema)
     *
     * @param totalDigits
     */
    public void addTotalDigits(int totalDigits ) {
        getBase().addTotalDigits(totalDigits);
    }
    
    /*
     * set fraction digits value (this corresponds to the value of fractionDigits facet in schema)
     *
     * @param fractionDigits
     */
    public void addFractionDigits(int fractionDigits) {
        getBase().addFractionDigits(fractionDigits);
    }
    
    /*
     * set maximum Inclusive value (this corresponds to the value of maxInclusive facet in schema)
     *
     * @param maxInclusive
     */
    public void addMaxInclusive(Object maxInclusive) {
        getBase().addMaxInclusive(maxInclusive);
    }
    
    /*
     * set maximum Exclusive value (this corresponds to the value of maxExclusive facet in schema)
     *
     * @param maxExclusive
     */
    public void addMaxExclusive(Object maxExclusive) {
        getBase().addMaxExclusive(maxExclusive);
    }
    
    /*
     * set minimum Inclusive value (this corresponds to the value of minInclusive facet in schema)
     *
     * @param minInclusive
     */
    public void addMinInclusive(Object minInclusive) {
        getBase().addMinInclusive(minInclusive);
    }
    
    /*
     * set minExclusive value (this corresponds to the value of minExclusive facet in schema)
     *
     * @param minExclusive
     */
    public void addMinExclusive(Object minExclusive) {
        getBase().addMinExclusive(minExclusive);
    }
    
    /*
     * set length (this corresponds to the value of length facet in schema)
     *
     * @param length
     */
    public void addLength(int length) {
        getBase().addLength(length);
    }
    
    /*
     * set minimum length value (this corresponds to the value of minlength facet in schema)
     *
     * @param minLength
     */
    public void addMinLength(int minLength) {
        getBase().addMinLength(minLength);
    }
    
    /*
     * set maximum length value (this corresponds to the value of maxlength facet in schema)
     *
     * @param maxLength
     */
    public void addMaxLength(int maxLength) {
        getBase().addMaxLength(maxLength);
    }
    
    public String toString() {
        return getName();
    }
}
