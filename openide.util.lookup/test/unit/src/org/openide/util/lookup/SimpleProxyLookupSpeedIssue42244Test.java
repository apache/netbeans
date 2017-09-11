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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.util.lookup;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;

import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;

/**
 * @author  Petr Nejedly, adapted to test by Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #1847
public class SimpleProxyLookupSpeedIssue42244Test extends NbTestCase {

    public SimpleProxyLookupSpeedIssue42244Test (String name) {
        super (name);
    }

    public void testCompareTheSpeed () {
        String content1 = "String1";
        String content2 = "String2";
        
        Lookup fixed1 = Lookups.singleton(content1);
        Lookup fixed2 = Lookups.singleton(content2);
        
        MyProvider provider = new MyProvider();
        provider.setLookup(fixed1);
        
        Lookup top = Lookups.proxy(provider);

        Lookup.Result<String> r0 = top.lookupResult(String.class);
        r0.allInstances();

        long time = System.currentTimeMillis();
        top.lookupAll(String.class);
        long withOneResult = System.currentTimeMillis() - time;

     
        Set<Object> results = new HashSet<Object>();
        for (int i=0; i<10000; i++) {
            Lookup.Result<String> res = top.lookupResult(String.class);
            results.add (res);
            res.allInstances();
        }
        
        provider.setLookup(fixed2);

        time = System.currentTimeMillis();
        top.lookupAll(String.class);
        long withManyResults = System.currentTimeMillis() - time;
        
        // if the measurement takes less then 10ms, pretend 10ms
        if (withManyResults < 10) {
            withManyResults = 10;
        }
        if (withOneResult < 10) {
            withOneResult = 10;
        }

        if (withManyResults >= 10 * withOneResult) {
            fail ("With many results the test runs too long.\n With many: " + withManyResults + "\n With one : " + withOneResult);
        }
    }
    
    private static class MyProvider implements Lookup.Provider {
        private Lookup lookup;
        public Lookup getLookup() {
            return lookup;
        }
        
        void setLookup(Lookup lookup) {
            this.lookup = lookup;
        }
    }
    
}
