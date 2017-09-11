/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.openide.util.lookup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import static org.junit.Assert.*;

/**
 * Test donated by Mr. Komrska. Seems to pass with 6.5.
 * @author komrska
 */
public final class KomrskaLookupTest {
    private TestLookupManager lookupManager=null;
    private StringBuffer result=null;
    
    //
    
    private void addToLookup(final TestLookupItemA object) {
        result.append('A');
        lookupManager.add(object);
    }
    private void removeFromLookup(final TestLookupItemA object) {
        result.append('A');
        lookupManager.remove(object);
    }

    private void addToLookup(final TestLookupItemB object) {
        result.append('B');
        lookupManager.add(object);
    }
    private void removeFromLookup(final TestLookupItemB object) {
        result.append('B');
        lookupManager.remove(object);
    }
    
    public String getResult() {
        return result.toString();
    }
    
    //

    @Before
    public void setUp() {
        lookupManager=new TestLookupManager();
        result=new StringBuffer();
    }
    
    @After
    public void tearDown() {
        lookupManager=null;
        result=null;
    }
    
    @Test
    public void testLookupBug() {
        TestLookupItemA itemA1=new TestLookupItemA();
        TestLookupItemB itemB1=new TestLookupItemB();
        //
        addToLookup(itemA1);
        addToLookup(itemB1);
        removeFromLookup(itemA1);
        removeFromLookup(itemB1);
        addToLookup(itemB1);
        removeFromLookup(itemB1);
        //
        addToLookup(itemA1);
        addToLookup(itemB1);
        removeFromLookup(itemA1);
        removeFromLookup(itemB1);
        addToLookup(itemB1);
        removeFromLookup(itemB1);
        //
        addToLookup(itemA1);
        addToLookup(itemB1);
        removeFromLookup(itemA1);
        removeFromLookup(itemB1);
        addToLookup(itemB1);
        removeFromLookup(itemB1);
        //
        assertEquals(getResult(),lookupManager.getResult());
    }

    public static final class TestLookupItemA {}
    public static final class TestLookupItemB {}
    public static final class TestLookupManager {
        private InstanceContent instanceContent=new InstanceContent();
        private AbstractLookup abstractLookup=new AbstractLookup(instanceContent);

        private Lookup.Result<TestLookupItemA> resultA=null;
        private Lookup.Result<TestLookupItemB> resultB=null;

        private LookupListener listenerA=new LookupListener() {
            public void resultChanged(LookupEvent event) {
                result.append('A');
            }
        };
        private LookupListener listenerB=new LookupListener() {
            public void resultChanged(LookupEvent event) {
                result.append('B');
            }
        };

        private StringBuffer result=new StringBuffer();

        //

        public TestLookupManager() {
            Lookup.Template<TestLookupItemA> templateA=
                new Lookup.Template<TestLookupItemA>(TestLookupItemA.class);
            resultA=abstractLookup.lookup(templateA);
            resultA.addLookupListener(listenerA);
            resultA.allInstances().size();
            //
            Lookup.Template<TestLookupItemB> templateB=
                new Lookup.Template<TestLookupItemB>(TestLookupItemB.class);
            resultB=abstractLookup.lookup(templateB);
            resultB.addLookupListener(listenerB);
            resultB.allInstances().size();
            // WORKAROUND
            // instanceContent.add(Boolean.TRUE);
        }

        //

        public void add(Object item) {
            instanceContent.add(item);
        }
        public void remove(Object item) {
            instanceContent.remove(item);
        }
        public String getResult() {
            return result.toString();
        }
    }

}
