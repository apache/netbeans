/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.lib.api.elements;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;

/**
 *
 * @author marekfukala
 */
public class ElementsIteratorTest extends NbTestCase {

    public ElementsIteratorTest(String name) {
        super(name);
    }

    //Bug 211134 - AssertionError: element length must be positive!
    public void testIssue211134() {
        //if the comment element is longer than Short.MAX_VALUE (32k)
        //the cast to short will cause the AE
        //Such situation may happen if one starts to type comment
        //delimiter at the beginning of a longer file so the whole
        //file is lexed as comment until the end delimiter is written.
        
        StringBuilder code = new StringBuilder();
        code.append("<!--");
        for(int i = 0; i < Short.MAX_VALUE; i++) {
            code.append("X");
        }
        
        HtmlSource source = new HtmlSource(code);
        ElementsIterator itr = new ElementsIterator(source);
        
        assertTrue(itr.hasNext());
        
        Element e = itr.next();
        assertNotNull(e);
        assertEquals(ElementType.COMMENT, e.type());
        assertEquals(0, e.from());
        assertEquals(code.length(), e.to());
        
    }
    
    //Bug 211222 - AssertionError at org.netbeans.modules.html.editor.lib.api.elements.ElementsIterator.tag
    public void testIssue211222() {
        //attribute name length > 127bytes
        StringBuilder code = new StringBuilder();
        code.append("<div ");
        for(int i = 0; i < Byte.MAX_VALUE + 10; i++) {
            code.append("a");
        }
        
        HtmlSource source = new HtmlSource(code);
        ElementsIterator itr = new ElementsIterator(source);
        
        assertTrue(itr.hasNext());
        assertNotNull(itr.next());
        
    }
    
}
