/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.glassfish.common.registration;

import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;

/**
 *
 * @author pepness
 */
public class AutomaticRegistrationTest {
    
    @Test
    public void testRegistrationGF8() {
        GlassFishVersion version = GlassFishVersion.GF_8_0_0;
        if (GlassFishVersion.ge(version, GlassFishVersion.GF_8_0_0)) {
            assertTrue("Success!", true);
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
            fail("GF_6");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
            fail("GF_5_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
            fail("GF_5");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
            fail("GF_3_1");
        }
    }
    
    @Test
    public void testRegistrationGF7() {
        GlassFishVersion version = GlassFishVersion.GF_7_0_11;
        if (GlassFishVersion.ge(version, GlassFishVersion.GF_7_0_0)) {
            assertTrue("Success!", true);
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
            fail("GF_6");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
            fail("GF_5_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
            fail("GF_5");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
            fail("GF_3_1");
        }
    }
    
    @Test
    public void testRegistrationGF625() {
        GlassFishVersion version = GlassFishVersion.GF_6_2_5;
        if (GlassFishVersion.ge(version, GlassFishVersion.GF_6_1_0)) {
            assertTrue("Success!", true);
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
            fail("GF_6");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
            fail("GF_5_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
            fail("GF_5");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
            fail("GF_3_1");
        }
    }
    
    @Test
    public void testRegistrationGF6() {
        GlassFishVersion version = GlassFishVersion.GF_6;
        if (GlassFishVersion.ge(version, GlassFishVersion.GF_6_1_0)) {
            fail("GF_6_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
            assertTrue("Success!", true);
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
            fail("GF_5_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
            fail("GF_5");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
            fail("GF_3_1");
        }
    }
    
    @Test
    public void testRegistrationGF510() {
        GlassFishVersion version = GlassFishVersion.GF_5_1_0;
        if (GlassFishVersion.ge(version, GlassFishVersion.GF_6_1_0)) {
            fail("GF_6_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
            fail("GF_6");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
            assertTrue("Success!", true);
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
            fail("GF_5");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
            fail("GF_3_1");
        }
    }
    
    @Test
    public void testRegistrationGF5() {
        GlassFishVersion version = GlassFishVersion.GF_5;
        if (GlassFishVersion.ge(version, GlassFishVersion.GF_6_1_0)) {
            fail("GF_6_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
            fail("GF_6");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
            fail("GF_5_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
            assertTrue("Success!", true);
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
            fail("GF_3_1");
        }
    }
    
    @Test
    public void testRegistrationGF4() {
        GlassFishVersion version = GlassFishVersion.GF_4_1_2;
        if (GlassFishVersion.ge(version, GlassFishVersion.GF_6_1_0)) {
            fail("GF_6_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
            fail("GF_6");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
            fail("GF_5_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
            fail("GF_5");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_4)) {
            assertTrue("Success!", true);
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
            fail("GF_3_1");
        }
    }
    
    @Test
    public void testRegistrationGF31() {
        GlassFishVersion version = GlassFishVersion.GF_3_1;
        if (GlassFishVersion.ge(version, GlassFishVersion.GF_6_1_0)) {
            fail("GF_6_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
            fail("GF_6");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
            fail("GF_5_1_0");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
            fail("GF_5");
        } else if (GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
            assertTrue("Success!", true);
        }
    }
    
}
