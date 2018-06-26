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
package org.netbeans.modules.html.angular.model;

import org.netbeans.junit.NbTestCase;
import static org.netbeans.modules.html.angular.model.DirectiveConvention.*;

/**
 *
 * @author marekfukala
 */
public class DirectiveConventionTest extends NbTestCase {
    
    public DirectiveConventionTest(String name) {
        super(name);
    }

    public void testGetConvention() {
        assertEquals(base_dash, DirectiveConvention.getConvention("ng-app"));
        assertEquals(base_underscore, DirectiveConvention.getConvention("ng_app"));
        assertEquals(base_colon, DirectiveConvention.getConvention("ng:app"));
        
        assertEquals(data_dash, DirectiveConvention.getConvention("data-ng-app"));
        assertEquals(data_underscore, DirectiveConvention.getConvention("data-ng_app"));
        assertEquals(data_colon, DirectiveConvention.getConvention("data-ng:app"));
        
        assertEquals(x_dash, DirectiveConvention.getConvention("x-ng-app"));
        assertEquals(x_underscore, DirectiveConvention.getConvention("x-ng_app"));
        assertEquals(x_colon, DirectiveConvention.getConvention("x-ng:app"));
        
        assertNull(DirectiveConvention.getConvention("ng"));
        assertNull(DirectiveConvention.getConvention("x-ng"));
        assertNull(DirectiveConvention.getConvention("foo"));
        assertNull(DirectiveConvention.getConvention("data-foo"));
        assertNull(DirectiveConvention.getConvention("x-foo"));
        assertNull(DirectiveConvention.getConvention("ng@binf"));
        assertNull(DirectiveConvention.getConvention("x-ng@binf"));
        
        
    }
}