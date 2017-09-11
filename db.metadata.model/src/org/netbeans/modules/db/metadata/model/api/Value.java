/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.api;

import org.netbeans.modules.db.metadata.model.spi.ValueImplementation;

/**
 * Defines a value used when working with the database.  It can be
 * a column in a table, a column in a result set, or a parameter in a procedure
 * or a function.
 *
 * @author David Van Couvering
 */
public class Value extends MetadataElement {
    private final ValueImplementation impl;

    Value(ValueImplementation impl) {
        this.impl = impl;
    }

    @Override
    public MetadataElement getParent() {
        return impl.getParent();
    }

    @Override
    public String getName() {
        return impl.getName();
    }
    
    /**
     * Return the SQL type of this value
     *
     * @return the SQL type of this value
     */
    public SQLType getType() {
        return impl.getType();
    }

    /**
     * Return the precision for this value.  Precision is defined as the total
     * number of possible digits for this value
     *
     * @return the precision for this value or 0 if precision does nt apply to this type
     */
    public int getPrecision() {
        return impl.getPrecision();
    }

    /**
     * Return the length of this value for variable length values such as
     * characters.  Length has no meaning for fixed-length types like numerics.
     *
     * @return the length of this value or 0 if length does not apply to this type.
     */
    public int getLength() {
        return impl.getLength();
    }

    /**
     * Return the scale for this value.  This is the number of digits to the
     * right of the decimal point.
     *
     * @return the scale for this value or 0 if scale does not apply to this type
     */
    public short getScale() {
        return impl.getScale();
    }

    /**
     * Return the radix for this value, where applicable.  This is defined number of digits
     * used in expressing a number.  For example, binary numbers have a radix
     * of 2, hex numbers have a radix of 16 and decimal numbers have a radix
     * of 10.  Non-numeric values have a radix of 0.
     *
     * @return the radix for this value, or 0 if a radix does not apply to this type
     */
    public short getRadix() {
        return impl.getRadix();
    }

    /**
     * Return whether this value is nullable or not
     *
     * @return whether this value is nullable
     */
    public Nullable getNullable() {
        return impl.getNullable();
    }

    /**
     * Return database specific name of data type
     *
     * @return
     */
    public String getTypeName() {
        return impl.getTypeName();
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}
