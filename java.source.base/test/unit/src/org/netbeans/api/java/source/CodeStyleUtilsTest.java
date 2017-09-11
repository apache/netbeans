/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.source;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class CodeStyleUtilsTest {

    @Test
    public void testAddPrefixSuffix() {
        CharSequence name = null;
        String prefix = null;
        String suffix = null;
        String result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("", result);

        name = "name";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals(name, result);
        
        suffix = "S";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("nameS", result);
        
        prefix = "$";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("$nameS", result);
        
        prefix = "s";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("sNameS", result);
        
        name = "__name";
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("sNameS", result);
        
        name = null;
        result = CodeStyleUtils.addPrefixSuffix(name, prefix, suffix);
        assertEquals("sS", result);
    }

    @Test
    public void testRemovePrefixSuffix() {
        String prefix = null;
        String suffix = null;

        CharSequence name = "name";
        String result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals(name, result);
        
        suffix = "S";
        name = "nameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("name", result);
        
        prefix = "$";
        name = "$nameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("name", result);
        
        prefix = "s";
        name = "sNameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("name", result);
        
        prefix = "_";
        name = "__nameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("_name", result);
        
        prefix = "S";
        suffix = "s";
        name = "sNameS";
        result = CodeStyleUtils.removePrefixSuffix(name, prefix, suffix);
        assertEquals("sNameS", result);
    }

    @Test
    public void testGetCapitalizedName() {
        CharSequence name = "name";
        String result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("Name", result);
        
        name = "Name";
        result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("Name", result);
        
        name = "NAME";
        result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("NAME", result);
        
        name = "nAme";
        result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("nAme", result);
        
        name = "naMe";
        result = CodeStyleUtils.getCapitalizedName(name);
        assertEquals("NaMe", result);
    }

    @Test
    public void testGetDecapitalizedName() {
        CharSequence name = "name";
        String result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("name", result);
        
        name = "Name";
        result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("name", result);
        
        name = "NAME";
        result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("NAME", result);
        
        name = "nAme";
        result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("nAme", result);
        
        name = "NaMe";
        result = CodeStyleUtils.getDecapitalizedName(name);
        assertEquals("naMe", result);
    }
}
