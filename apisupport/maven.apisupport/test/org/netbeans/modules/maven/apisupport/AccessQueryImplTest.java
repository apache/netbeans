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

package org.netbeans.modules.maven.apisupport;

import org.netbeans.modules.maven.apisupport.AccessQueryImpl;
import java.util.List;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 *
 * @author mkleint
 */
public class AccessQueryImplTest extends TestCase {
    
    public AccessQueryImplTest(String testName) {
        super(testName);
    }


    public void testGetPublicPackagesPatterns() {
        List<Pattern> result = AccessQueryImpl.preparePublicPackagesPatterns("org.milos.*");
        assertNotNull(result);
        assertTrue(check(result, new String[] {
            "org.milos"
        }));
        assertFalse(check(result, new String[] {
            "org.milos.xxx",
            "milos.org.nic",
            "net.tomas.milos"
        }));
        
        result = AccessQueryImpl.preparePublicPackagesPatterns("org.milos.**");
        assertNotNull(result);
        assertTrue(check(result, new String[] {
            "org.milos",
            "org.milos.lenka.petr.katerina",
            "org.milos.xxx"
        }));
        assertFalse(check(result, new String[] {
            "milos.org.nic",
            "net.tomas.milos",
            "asi.org.milos"
        }));
        
    }
    
    private boolean check(List<Pattern> patt, String[] vals) {
        for (int i = 0; i < vals.length; i++) {
            boolean matches = false;
            for (Pattern pattern : patt) {
                matches = pattern.matcher(vals[i]).matches();
                if (matches) {
                    break;
                }
            }
            if (! matches) {
                return false;
            }
        }
        return true;
    }
    
    
}
