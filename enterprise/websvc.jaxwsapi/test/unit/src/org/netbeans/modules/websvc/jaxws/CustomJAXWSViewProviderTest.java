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

package org.netbeans.modules.websvc.jaxws;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.jaxws.api.JAXWSView;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomJAXWSViewProviderTest extends NbTestCase {
    
    static {
        CustomJAXWSViewProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    /** Creates a new instance of CustomJAXWSViewProviderTest */
    public CustomJAXWSViewProviderTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testProviders() throws Exception {
        Lookup.Result<JAXWSViewProvider> res = Lookup.getDefault().lookup(new Lookup.Template<JAXWSViewProvider>(JAXWSViewProvider.class));
        assertEquals("there should be 1 instance - from websvc/jaxwsapi", 1, res.allInstances().size());
    }
    
    public void testGetJAXWSView() {
        JAXWSView view = JAXWSView.getJAXWSView();
        assertNotNull("found view support", view);
    }
    
}
