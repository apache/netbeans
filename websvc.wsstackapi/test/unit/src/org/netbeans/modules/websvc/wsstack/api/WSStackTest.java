/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
