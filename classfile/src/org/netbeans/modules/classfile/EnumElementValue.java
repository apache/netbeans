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
 *
 */
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

/**
 * EnumElementValue:  a single annotation on a program element for
 * those annotations that are enum constants.
 *
 * @author  Thomas Ball
 */
public final class EnumElementValue extends ElementValue {
    String enumType;
    String enumName;

    EnumElementValue(ConstantPool pool, int iEnumType, int iEnumName) {
	enumType = ((CPName)pool.get(iEnumType)).getName();
	enumName = ((CPName)pool.get(iEnumName)).getName();
    }

    // for 1.5 beta1 classfile incompatibility
    EnumElementValue(String type, String name) {
	enumType = type;
	enumName = name;
    }

    /**
     * Returns the enum type as a string, rather than a ClassName.
     * This is necessary because an enum may have a primitive type.
     */
    public final String getEnumType() {
	return enumType;
    }

    /**
     * Returns the name of the enum constant for this annotation
     * component.
     */
    public final String getEnumName() {
	return enumName;
    }

    @Override
    public String toString() {
	return enumType + "." + enumName;
    }
}
