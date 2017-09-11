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

package org.netbeans.modules.bugtracking.commons;

import org.netbeans.modules.bugtracking.commons.FindTypesSupport;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author tomas
 */
public class FindTypesSupportTest extends TestCase {

    public void testIsCamelCase() {
        String str = "Camel";
        List<Integer> l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("Camel", str.substring(l.get(0).intValue(), l.get(1).intValue()));
                
        str = "CamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "CamelCase.";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals("CamelCase".length(), l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "TripleCamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("TripleCamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "org.CamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("org.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "org.camel.CamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals(str.length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = " CamelCase ";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(1, l.get(0).intValue());
        assertEquals("CamelCase".length() + 1, l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "\tCamelCase\t";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(1, l.get(0).intValue());
        assertEquals("CamelCase".length() + 1, l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "\nCamelCase\n";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(1, l.get(0).intValue());
        assertEquals("CamelCase".length()+1, l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = " org.camel.CamelCase ";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(1, l.get(0).intValue());
        assertEquals("org.camel.CamelCase".length() + 1, l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        String prefix = " '";
        String sufix = "' ";
        str = prefix + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));

        prefix = " \"";
        sufix = "\" ";
        str = prefix + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        prefix = ".";
        sufix = ".";
        str = prefix + "CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "CamelCase").length(), l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        prefix = ".";
        sufix = ".";
        str = prefix + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(2, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        
        str = "CamelCase CamelCase";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(4, l.size());
        assertEquals(0, l.get(0).intValue());
        assertEquals("CamelCase".length(), l.get(1).intValue());
        assertEquals("CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        assertEquals("CamelCase ".length(), l.get(2).intValue());
        assertEquals(str.length(), l.get(3).intValue());
        assertEquals("CamelCase", str.substring(l.get(2).intValue(), l.get(3).intValue()));
        
        prefix = " ";
        sufix = " ";
        String mid = " ";
        str = prefix + "org.camel.CamelCase" + mid + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(4, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        assertEquals((prefix + "org.camel.CamelCase" + mid).length(), l.get(2).intValue());
        assertEquals(str.length() - sufix.length(), l.get(3).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(2).intValue(), l.get(3).intValue()));
        
        prefix = " a ";
        sufix = " a ";
        mid = " a a a a ";
        str = prefix + "org.camel.CamelCase" + mid + "org.camel.CamelCase" + sufix;
        l = FindTypesSupport.getHighlightOffsets(str);
        assertEquals(4, l.size());
        assertEquals(prefix.length(), l.get(0).intValue());
        assertEquals((prefix + "org.camel.CamelCase").length(), l.get(1).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(0).intValue(), l.get(1).intValue()));
        assertEquals((prefix + "org.camel.CamelCase" + mid).length(), l.get(2).intValue());
        assertEquals(str.length() - sufix.length(), l.get(3).intValue());
        assertEquals("org.camel.CamelCase", str.substring(l.get(2).intValue(), l.get(3).intValue()));
        
        
        str = "camel";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
        str = " camel camel ";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
        str = " camel.camel ";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
        str = "camel.camel";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
        str = ".camel.camel.";
        l = FindTypesSupport.getHighlightOffsets(str);
        assertTrue(l.isEmpty());
        
    }

}
