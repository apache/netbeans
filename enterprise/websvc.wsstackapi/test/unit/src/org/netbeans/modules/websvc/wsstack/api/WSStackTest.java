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
package org.netbeans.modules.websvc.wsstack.api;

import java.io.IOException;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;
import org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation;

/**
 *
 * @author mkuchtiak
 */
public class WSStackTest extends NbTestCase {

    public WSStackTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /** Test service model for AddNumbers service
     */
    public void testServiceModel() throws IOException {
        final JaxWs jaxWs = new JaxWs("Sample data"); 
        
        WSStackImplementation<JaxWs> jdkStackImpl = new WSStackImplementation<JaxWs>() {

            public WSStackVersion getVersion() {
                return WSStackFactory.createWSStackVersion("1.2.1-beta1");
            }

            public WSTool getWSTool(WSStack.Tool toolId) {
                if (toolId == JaxWs.Tool.WSIMPORT) {
                    return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSIMPORT));
                } else if (toolId == JaxWs.Tool.WSGEN) {
                    return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSGEN));
                }
                return null;
            }

            public boolean isFeatureSupported(WSStack.Feature feature) {
                return false;
            }

            public JaxWs get() {
                return jaxWs;
            }
            
        };
        
        WSStack<JaxWs> jdkStack = WSStackFactory.createWSStack(JaxWs.class, jdkStackImpl , WSStack.Source.JDK);
        assertNotNull(jdkStack);
        assertEquals(jdkStack.getVersion().getMajor(), 1);
        assertEquals(jdkStack.getVersion().getMinor(),2);
        assertEquals(jdkStack.getVersion().compareTo(WSStackVersion.valueOf(1,2,1,0)), 0);
        assertEquals(jdkStack.getVersion().compareTo(WSStackVersion.valueOf(1,2,1,1)), -1);
        assertTrue(jdkStack.getVersion().equals(WSStackVersion.valueOf(1,2,1,0)));
        assertEquals(jdkStack.getSource(),WSStack.Source.JDK);
        assertFalse(jdkStack.isFeatureSupported(JaxWs.Feature.WSIT));
        assertFalse(jdkStack.isFeatureSupported(JaxWs.Feature.JSR109));
        assertNotNull(jdkStack.getWSTool(JaxWs.Tool.WSGEN));
        assertEquals(jdkStack.get().getData(),"Sample data");
    }
    
    static class JaxWs {
        
        String data;
        
        JaxWs(String data) {
            this.data = data;
        }
        
        static enum Tool implements WSStack.Tool {
            WSIMPORT,
            WSGEN;

            public String getName() {
                return name();
            }
        }
        static enum Feature implements WSStack.Feature {
            WSIT,
            JSR109;

            public String getName() {
                return name();
            }
        }
        
        public String getData() {
            return data;
        } 
    }
    
    private class JaxWsTool implements WSToolImplementation {
        JaxWs.Tool tool;
        JaxWsTool(JaxWs.Tool tool) {
            this.tool = tool;
        }

        public String getName() {
            return tool.getName();
        }

        public URL[] getLibraries() {
            return new URL[]{};
        }
        
    }
}
