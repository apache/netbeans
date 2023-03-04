/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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