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

package org.netbeans.api.autoupdate;

import java.util.List;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Radek Matous
 */
public class DeclarationOfUpdateUnitProviderTest extends NbTestCase {
    private UpdateUnitProvider enabled;
    private UpdateUnitProvider disabled;
    private UpdateUnitProvider beta;    
    private UpdateUnitProvider fallback;
    static {
        String[] layers = new String[]{"org/netbeans/api/autoupdate/mf-layer.xml"}; //NOI18N
        Object[] instances = new Object[]{};
        IDEInitializer.setup(layers, instances);
    }

    public DeclarationOfUpdateUnitProviderTest(String testName) {
        super(testName);                
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        List<UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(false);
        for (UpdateUnitProvider updateUnitProvider : providers) {
            String name = updateUnitProvider.getName();
            if (name.equals("UC_ENABLED")) {
                enabled = updateUnitProvider;
            } else if (name.equals("UC_DISABLED")) {
                disabled = updateUnitProvider;
            } else if (name.equals("UC_BETA")) {
                beta = updateUnitProvider;
            } else if (name.equals("UC_FALLBACK_CATEGORY")) {
                fallback = updateUnitProvider;
            }
        }
        assertNotSame(enabled, disabled);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEnabledDisabled() {
        assertTrue(enabled.isEnabled());
        assertFalse(disabled.isEnabled());        
    }
    
    public void testCategory() {
        assertEquals(CATEGORY.STANDARD, enabled.getCategory());
        assertEquals(CATEGORY.STANDARD, disabled.getCategory());
        assertEquals(CATEGORY.BETA, beta.getCategory());
        assertEquals(CATEGORY.COMMUNITY, fallback.getCategory());//fallback if no declaration                        
    }
}
