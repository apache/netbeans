/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup.Storage;

public class ProxyLookup173975Test {

    public ProxyLookup173975Test() {
    }

    boolean called = false;

    @Test
    public void testAbstractLookupWithoutAllInstances() {
        registerLookupListenerAndAddSomething(false, false, false);
    }

    @Test
    public void testAbstractLookupWithAllInstances() {
        registerLookupListenerAndAddSomething(false, true, false);
    }

    @Test
    public void testAbstractLookupInheritanceTreeWithoutAllInstances() {
        registerLookupListenerAndAddSomething(false, false, true);
    }

    @Test
    public void testAbstractLookupInheritanceTreeWithAllInstances() {
        registerLookupListenerAndAddSomething(false, true, true);
    }

    @Test
    public void testProxyLookupWithoutAllInstances() {
        registerLookupListenerAndAddSomething(true, false, false);
    }

    @Test
    public void testProxyLookupWithAllInstances() {
        registerLookupListenerAndAddSomething(true, true, false);
    }

    @Test
    public void testProxyLookupInheritanceTreeWithoutAllInstances() {
        registerLookupListenerAndAddSomething(true, false, true);
    }

    @Test
    public void testProxyLookupInheritanceTreeWithAllInstances() {
        registerLookupListenerAndAddSomething(true, true, true);
    }

    private void registerLookupListenerAndAddSomething(boolean useProxy, boolean callAllInstances, boolean inheritanceTree) {
        called = false;
        InstanceContent aInstanceContent = new InstanceContent();
        Storage<?> s = inheritanceTree ? new InheritanceTree() : new ArrayStorage();
        Lookup aLookup = new AbstractLookup(aInstanceContent, s);
        if (useProxy) {
            aLookup = new ProxyLookup(aLookup);
        }
        Lookup.Result<ObjectInLookup> result = aLookup.lookupResult(ObjectInLookup.class);
        if (callAllInstances) {
            result.allInstances(); // TO GET SUCCESS
        }
        result.addLookupListener(new LookupListener() {

            public void resultChanged(LookupEvent ev) {
                Lookup.Result aResult = (Lookup.Result) ev.getSource();
                Collection c = aResult.allInstances();
                if (!c.isEmpty()) {
                    called = true;
                }
            }
        });

        aInstanceContent.set(Collections.singleton(
                new ObjectInLookup("Set Object in Lookup)")), null);
        Assert.assertTrue("Listener was notified", called);
    }

    public class ObjectInLookup {

        private final String name;

        public ObjectInLookup(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return "objectinlookup:" + getName();
        }
    }
}
