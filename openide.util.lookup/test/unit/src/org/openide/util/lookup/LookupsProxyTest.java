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

import java.io.Serializable;

import java.util.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Runs all NbLookupTest tests on ProxyLookup and adds few additional.
 */
@SuppressWarnings("unchecked") // XXX ought to be corrected, just a lot of them
public class LookupsProxyTest extends AbstractLookupBaseHid
implements AbstractLookupBaseHid.Impl {
    public LookupsProxyTest(java.lang.String testName) {
        super(testName, null);
    }

    /** Creates an lookup for given lookup. This class just returns
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    public Lookup createLookup (final Lookup lookup) {
        return org.openide.util.lookup.Lookups.proxy (
            new Lookup.Provider () {
                public Lookup getLookup () {
                    return lookup;
                }
            }
        );
    }

    public Lookup createInstancesLookup (InstanceContent ic) {
        return new AbstractLookup (ic);
    }

    public void clearCaches () {
    }



    /** Check whether setLookups method does not fire when there is no
     * change in the lookups.
     */
    public void testProxyListener () {
        Changer ch = new Changer (Lookup.EMPTY);

        Lookup lookup = Lookups.proxy(ch);
        Lookup.Result res = lookup.lookup (new Lookup.Template (Object.class));

        LL ll = new LL ();
        res.addLookupListener (ll);
        Collection allRes = res.allInstances ();

        ch.setLookup (new AbstractLookup (new InstanceContent ())); // another empty lookup
        lookup.lookup (Object.class); // does the refresh

        assertEquals("Replacing an empty by empty does not generate an event", 0, ll.getCount());

        InstanceContent content = new InstanceContent ();
        AbstractLookup del = new AbstractLookup (content);
        content.add (this);
        ch.setLookup (del);
        lookup.lookup (Object.class);

        if (ll.getCount () != 1) {
            fail ("Changing lookups with different content generates an event");
        }

        ch.setLookup (del);
        lookup.lookup (Object.class);

        if (ll.getCount () != 0) {
           fail ("Not changing the lookups does not generate any event");
        }
    }


    public void testListeningAndQueryingByTwoListenersInstancesSetLookups() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 1, false);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 1, false);
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 1, false);
    }

    public void testListeningAndQueryingByTwoListenersInstancesSetLookups2() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 2, false);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups2() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 2, false);
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups2() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 2, false);
    }

    public void testListeningAndQueryingByTwoListenersInstancesSetLookupsWithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 1, true);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookupsWithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 1, true);
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookupsWithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 1, true);
    }

    public void testListeningAndQueryingByTwoListenersInstancesSetLookups2WithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 2, true);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups2WithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 2, true);
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups2WithProxy() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 2, true);
    }

    /* XXX: these are pretty slow, seems there is a performance problem 2^22
    public void testListeningAndQueryingByTwoListenersInstancesSetLookups22() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 22);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups22() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 22);
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups22() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 22);
    }
     */

    private void doListeningAndQueryingByTwoListenersSetLookups(final int type, int depth, boolean cacheOnTop) {
        Changer orig = new Changer(Lookup.EMPTY);
        Lookup on = Lookups.proxy(orig);
        Lookup first = on;

        while (--depth > 0) {
            Changer next = new Changer(on);
            on = Lookups.proxy(next);
        }


        final Lookup lookup = cacheOnTop ? new ProxyLookup(new Lookup[] { on }) : on;

        class L implements LookupListener {
            Lookup.Result integer = lookup.lookup(new Lookup.Template(Integer.class));
            Lookup.Result number = lookup.lookup(new Lookup.Template(Number.class));
            Lookup.Result serial = lookup.lookup(new Lookup.Template(Serializable.class));

            {
                integer.addLookupListener(this);
                number.addLookupListener(this);
                serial.addLookupListener(this);
            }

            int round;

            public void resultChanged(LookupEvent ev) {
                Collection c1 = get(type, integer);
                Collection c2 = get(type, number);
                Collection c3 = get(type, serial);

                assertEquals("round " + round + " c1 vs. c2", c1, c2);
                assertEquals("round " + round + " c1 vs. c3", c1, c3);
                assertEquals("round " + round + " c2 vs. c3", c2, c3);

                round++;
            }

            private Collection get(int type, Lookup.Result res) {
                Collection c;
                switch(type) {
                    case 0: c = res.allInstances(); break;
                    case 1: c = res.allClasses(); break;
                    case 2: c = res.allItems(); break;
                    default: c = null; fail("Type: " + type); break;
                }

                assertNotNull(c);
                return new ArrayList(c);
            }
        }

        L listener = new L();
        listener.resultChanged(null);
        ArrayList arr = new ArrayList();
        for(int i = 0; i < 100; i++) {
            arr.add(new Integer(i));

            orig.lookup = Lookups.fixed(arr.toArray());
            // do the refresh
            first.lookup((Class)null);
        }

        assertEquals("3x100+1 checks", 301, listener.round);
    }


    public void testRefreshWithoutAllInstances103300 () {
        Changer ch = new Changer (Lookup.EMPTY);

        Lookup lookup = Lookups.proxy(ch);

        ch.setLookup (new AbstractLookup (new InstanceContent ())); // another empty lookup
        assertNull("Nothing there", lookup.lookup (Object.class)); // does the refresh

        InstanceContent content = new InstanceContent ();
        AbstractLookup del = new AbstractLookup (content);
        content.add (this);
        ch.setLookup (del);
        assertEquals("Can see me", this, lookup.lookup (Object.class));

        ch.setLookup (del);
        assertEquals("Still can see me", this, lookup.lookup (Object.class));

        assertEquals("I am visible", this, lookup.lookup(LookupsProxyTest.class));
    }


    private static final class Changer implements Lookup.Provider {
        private Lookup lookup;

        public Changer (Lookup lookup) {
            setLookup (lookup);
        }

        public void setLookup (Lookup lookup) {
            this.lookup = lookup;
        }

        public Lookup getLookup() {
            return lookup;
        }
    }

}
