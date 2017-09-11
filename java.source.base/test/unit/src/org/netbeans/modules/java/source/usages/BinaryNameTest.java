/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.usages;

import javax.lang.model.element.ElementKind;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class BinaryNameTest extends NbTestCase {
    
    public BinaryNameTest(final String name) {
        super(name);
    }
    
    public void testRawName() throws Exception {
        BinaryName name = BinaryName.create("java.lang.String", ElementKind.CLASS);
        assertEquals("java.lang", name.getPackage());
        assertEquals("String", name.getClassName());
        assertEquals("String", name.getSimpleName());
        name = BinaryName.create("InUnnamedPkg", ElementKind.CLASS);
        assertEquals("", name.getPackage());
        assertEquals("InUnnamedPkg", name.getClassName());
        assertEquals("InUnnamedPkg", name.getSimpleName());
        name = BinaryName.create("java.util.Map$Entry", ElementKind.INTERFACE);
        assertEquals("java.util", name.getPackage());
        assertEquals("Map$Entry", name.getClassName());
        assertEquals("Entry", name.getSimpleName());
        name = BinaryName.create("ru.$stl.Class", ElementKind.CLASS);
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("Class", name.getClassName());
        assertEquals("Class", name.getSimpleName());
        name = BinaryName.create("ru.$stl.Class$Inner", ElementKind.CLASS);
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("Class$Inner", name.getClassName());
        assertEquals("Inner", name.getSimpleName());
    }
    
    public void testIndexName() throws Exception {
        BinaryName name = BinaryName.create(
                "ru.$stl.$vector",
                ElementKind.CLASS,
                false,
                "ru.$stl.$vector".length()-"$vector".length());
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("$vector", name.getClassName());
        assertEquals("$vector", name.getSimpleName());
        name = BinaryName.create(
                "ru.$stl.$vector$iterator",
                ElementKind.CLASS,
                false,
                "ru.$stl.$vector$iterator".length()-"iterator".length());
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("$vector$iterator", name.getClassName());
        assertEquals("iterator", name.getSimpleName());
        name = BinaryName.create(
                "ru.$stl.$vector$$iterator",
                ElementKind.CLASS,
                false,
                "ru.$stl.$vector$$iterator".length()-"$iterator".length());
        assertEquals("ru.$stl", name.getPackage());
        assertEquals("$vector$$iterator", name.getClassName());
        assertEquals("$iterator", name.getSimpleName());
    }
    
}
