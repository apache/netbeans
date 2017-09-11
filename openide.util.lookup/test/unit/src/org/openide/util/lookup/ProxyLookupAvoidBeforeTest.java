/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.openide.util.lookup;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProxyLookupAvoidBeforeTest extends NbTestCase implements LookupListener {

    public ProxyLookupAvoidBeforeTest(String name) {
        super(name);
    }
    
    public void testDontCallBeforeLookup() throws Exception {
        InstanceContent ic = new InstanceContent();
        ic.add(1);
        ic.add(2L);
        
        ABefore one = new ABefore(ic);
        
        ProxyLookup lkp = new ProxyLookup(one);
        
        Result<Long> longResult = lkp.lookupResult(Long.class);
        longResult.addLookupListener(this);
        Result<Integer> intResult = lkp.lookupResult(Integer.class);
        intResult.addLookupListener(this);
        Result<Number> numResult = lkp.lookupResult(Number.class);
        numResult.addLookupListener(this);

        one.queryAllowed = true;
        assertEquals("Two", Long.valueOf(2L), longResult.allInstances().iterator().next());
        assertEquals("One", Integer.valueOf(1), intResult.allInstances().iterator().next());
        assertEquals("Two numbers", 2, numResult.allInstances().size());
        assertEquals("Two number items", 2, numResult.allItems().size());
        one.queryAllowed = false;
        
        NoBefore nob = new NoBefore();
        lkp.setLookups(one, nob);
        
        nob.queryAllowed = true;
        one.queryAllowed = true;
        assertEquals("Again Two", Long.valueOf(2L), lkp.lookup(Long.class));
        assertEquals("Again One", Integer.valueOf(1), lkp.lookup(Integer.class));
        assertEquals("Again Two numbers", 2, lkp.lookupAll(Number.class).size());
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        fail("No changes expected");
    }
    
    class NoBefore extends ProxyLookup {
        boolean queryAllowed;

        public NoBefore(Lookup... lookups) {
            super(lookups);
        }

        @Override
        protected void beforeLookup(Template<?> template) {
            assertTrue("Please don't call beforeLookup from changes: " + template.getType(), queryAllowed);
        }
    }
    
    class ABefore extends AbstractLookup {
        boolean queryAllowed;

        public ABefore(Content content) {
            super(content);
        }
        
        @Override
        protected void beforeLookup(Template<?> template) {
            assertTrue("Please don't call beforeLookup from changes: " + template.getType(), queryAllowed);
        }
        
    }
}
