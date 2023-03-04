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

package org.netbeans.modules.masterfs;

import junit.framework.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Radek Matous
 */
public class GlobalVisibilityQueryImplTest extends TestCase {
    private static GlobalVisibilityQueryImpl vq = new GlobalVisibilityQueryImpl() {
        @Override
        protected String getIgnoredFiles() {
            return "^^(CVS|SCCS|vssver.?\\.scc|#.*#|%.*%|_svn)$|~$|^\\.(?!(htaccess|git.+|hgignore)$).*$";//NOI18N
        }
    };
    
    static {
        System.setProperty("org.openide.util.Lookup", GlobalVisibilityQueryImplTest.TestLookup.class.getName());
    }
    
    public GlobalVisibilityQueryImplTest (String testName) {
        super(testName);
    }
    
    public void testVisibility() {        
        assertFalse(vq.isVisible(".#telnetrc"));
        assertFalse(vq.isVisible("._telnetrc"));
        assertFalse(vq.isVisible(".#_telnetrc"));
        assertFalse(vq.isVisible(".cvsignore"));
        assertFalse(vq.isVisible("CVS"));                
        assertFalse(vq.isVisible(".svn"));
        assertFalse(vq.isVisible("_svn"));
        
        assertFalse(vq.isVisible(".telnetrc"));                                
        assertFalse(vq.isVisible(".git"));
        assertTrue(vq.isVisible(".gitignore"));
        assertTrue(vq.isVisible(".gitkeep"));
        assertFalse(vq.isVisible(".hg"));
        assertTrue(vq.isVisible(".hgignore"));
    }
            
    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            setLookups(new Lookup[] {getInstanceLookup()});
        }
        
        private Lookup getInstanceLookup() {
            InstanceContent instanceContent = new InstanceContent();
            instanceContent.add(vq);
            Lookup instanceLookup = new AbstractLookup(instanceContent);
            return instanceLookup;
        }        
    }    
}
