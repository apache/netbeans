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

package org.netbeans.modules.hibernate.hyperlink;

import org.junit.Test;
import org.netbeans.modules.hibernate.completion.HibernateCompletionTestBase;
import static org.junit.Assert.*;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateMappingHyperlinkProviderTest extends HibernateCompletionTestBase {

    public HibernateMappingHyperlinkProviderTest(String name) {
        super(name);
    }

    
    /**
     * Test of isHyperlinkPoint method, of class HibernateMappingHyperlinkProvider.
     */
    @Test
    public void testIsHyperlinkPointOnClass() throws Exception {
        System.out.println("testIsHyperlinkPointOnClass");
        setupCompletion("resources/Person.hbm.xml", null);
        HibernateMappingHyperlinkProvider instance = new HibernateMappingHyperlinkProvider();
        
        boolean hyperpointNot = instance.isHyperlinkPoint(instanceDocument, 212);
        assertTrue(!hyperpointNot);
        
        boolean hyperpointYes = instance.isHyperlinkPoint(instanceDocument, 219);
        assertTrue(hyperpointYes);
    }
    
    /**
     * Test of getHyperlinkSpan method, of class HibernateMappingHyperlinkProvider.
     */
    @Test
    public void testGetHyperlinkSpanOnClass() throws Exception {
        System.out.println("testGetHyperlinkSpanClass");
        setupCompletion("resources/Person.hbm.xml", null);
        HibernateMappingHyperlinkProvider instance = new HibernateMappingHyperlinkProvider();
        instance.isHyperlinkPoint(instanceDocument, 219);
        int[] hyperpointSpan = instance.getHyperlinkSpan(instanceDocument, 219);
        int[] expected = new int[]{216, 229};
        assertEquals(hyperpointSpan[0], expected[0]);
        assertEquals(hyperpointSpan[1], expected[1]);
    }
    
    /**
     * Test of isHyperlinkPoint method, of class HibernateMappingHyperlinkProvider.
     */
    @Test
    public void testIsHyperlinkPointOnProperty() throws Exception {
        System.out.println("testIsHyperlinkPointProperty");
        setupCompletion("resources/Person.hbm.xml", null);
        HibernateMappingHyperlinkProvider instance = new HibernateMappingHyperlinkProvider();
        
        boolean hyperpointNot = instance.isHyperlinkPoint(instanceDocument, 351);
        assertTrue(!hyperpointNot);
        
        boolean hyperpointYes = instance.isHyperlinkPoint(instanceDocument, 363);
        assertTrue(hyperpointYes);
    }
    
    /**
     * Test of getHyperlinkSpan method, of class HibernateMappingHyperlinkProvider.
     */
    @Test
    public void testGetHyperlinkSpanOnProperty() throws Exception {
        System.out.println("testGetHyperlinkSpanOnProperty");
        setupCompletion("resources/Person.hbm.xml", null);
        HibernateMappingHyperlinkProvider instance = new HibernateMappingHyperlinkProvider();
        instance.isHyperlinkPoint(instanceDocument, 363);
        int[] hyperpointSpan = instance.getHyperlinkSpan(instanceDocument, 363);
        int[] expected = new int[]{362, 366};
        assertEquals(hyperpointSpan[0], expected[0]);
        assertEquals(hyperpointSpan[1], expected[1]);
    }
}
