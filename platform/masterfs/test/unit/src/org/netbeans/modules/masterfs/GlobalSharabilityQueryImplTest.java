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

import java.io.File;
import junit.framework.*;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Radek Matous
 */
public class GlobalSharabilityQueryImplTest extends TestCase {
    private static SharabilityQueryImplementation2 sq = new GlobalSharabilityQueryImpl();
    private static GlobalVisibilityQueryImpl vq = new GlobalVisibilityQueryImpl() {
        protected String getIgnoredFiles() {
            return "^(CVS|SCCS|vssver.?\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store)|_svn)$|~$|^\\..*$";//NOI18N
        }
    };
    
    static {
        System.setProperty("org.openide.util.Lookup", GlobalSharabilityQueryImplTest.TestLookup.class.getName());
    }
    
    public GlobalSharabilityQueryImplTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of getSharability method, of class org.netbeans.modules.masterfs.GlobalSharabilityQueryImpl.
     */
    public void testGetSharability() {
        File[] all = new File[] {
            new File("/myroot/mydirectory/myfile.java"),
                    new File("/myroot/mydirectory/myfile.class"),
                    new File("/myroot/mydirectory/myfile.xml"),
                    new File("/myroot/mydirectory/.cvsignore"),
                    new File("/myroot/mydirectory/CVS"),
                    new File("/myroot/mydirectory/.DS_Store"),
                    new File("/myroot/mydirectory/.svn"),
                    new File("/myroot/mydirectory/_svn")
        };
        
        for (int i = 0; i < all.length; i++) {
            boolean isNotSharable = sq.getSharability(Utilities.toURI(all[i])) == SharabilityQuery.Sharability.NOT_SHARABLE;
            boolean isNotVisible = !vq.isVisible(all[i].getName());
            assertEquals(isNotSharable, isNotVisible);
        }
    }
    
    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            setLookups(new Lookup[] {getInstanceLookup()});
        }
        
        private Lookup getInstanceLookup() {
            InstanceContent instanceContent = new InstanceContent();
            instanceContent.add(sq);
            instanceContent.add(vq);
            Lookup instanceLookup = new AbstractLookup(instanceContent);
            return instanceLookup;
        }        
    }    
}
