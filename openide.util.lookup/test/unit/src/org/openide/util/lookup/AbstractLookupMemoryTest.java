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

import java.util.Arrays;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.implspi.ActiveQueue;

/** Testing memory consumption of various AbstractLookup aspects.
 */
public class AbstractLookupMemoryTest extends NbTestCase {
    public AbstractLookupMemoryTest(java.lang.String testName) {
        super(testName);
    }

    public void testEmptySize () {
        AbstractLookup instanceLookup = new AbstractLookup ();
        assertSize ("Empty lookup should be small", 16, instanceLookup);

        InstanceContent ic = new InstanceContent ();
        instanceLookup = new AbstractLookup (ic);
        assertSize ("Lookup with InstanceContent should be small as well", 16, instanceLookup);
    }

    public void testPairSize () {
        AbstractLookup.Pair pair = new EmptyPair ();
        assertSize ("Pair occupies only 16 bytes", 16, pair);
    }

    public void testPairWithOnePointerSize () {
        AbstractLookup.Pair pair = new OneItemPair ();
        assertSize ("Pair occupies only 16 bytes", 16, pair);
    }

    public void testLookupWithPairs () {
        Lookup.Template<Object> t = new Lookup.Template<Object>(Object.class);
        class L implements org.openide.util.LookupListener {
            public int cnt;
            public void resultChanged (org.openide.util.LookupEvent ev) {
                cnt++;
            }
        }
        L listener = new L ();
        L listener2 = new L ();

        EmptyPair[] pairs = {
            new EmptyPair(),
            new EmptyPair(),
            new EmptyPair(),
            new EmptyPair(),
        };
        Object[] ignore = {
            pairs[0],
            pairs[1],
            pairs[2],
            pairs[3],
            t,
            ActiveQueue.queue(),
            listener,
            listener2,
            new Integer (11) // trashhold is shared
        };

        AbstractLookup.Content c = new AbstractLookup.Content ();
        AbstractLookup l = new AbstractLookup (c, (Integer)ignore[ignore.length - 1]);

        c.addPair ((EmptyPair)ignore[0]);
        assertSize ("Should be really small (not counting the pair sizes)", Collections.singleton (l), 56, ignore);

        c.addPair ((EmptyPair)ignore[1]);
        assertSize ("Is bigger I guess (not counting the pair sizes)", Collections.singleton (l), 56, ignore);

        c.setPairs(Arrays.asList(pairs).subList(0, 3));
        assertSize ("Even bigger (not counting the pair sizes)", Collections.singleton (l), 64, ignore);

        c.setPairs(Arrays.asList(pairs).subList(0, 4));
        assertSize ("Now not that much(not counting the pair sizes)", Collections.singleton (l), 64, ignore);

        Lookup.Result res = l.lookup (t);

        assertSize ("After creating a result", Collections.singleton (l), 120, ignore);

        res.addLookupListener (listener);

        assertSize ("And attaching one listener", Collections.singleton (l), 120, ignore);

        res.addLookupListener (listener2);
        assertSize ("Second listener makes the situation much worse", Collections.singleton (l), 200, ignore);
        res.removeLookupListener(listener2);
        assertSize ("But removing it returns us back to original size", Collections.singleton (l), 120, ignore);


        assertEquals ("Current for pairs are in", res.allItems ().size (), 4); // also activates the listener
        assertSize ("and making the listener to work", Collections.singleton (l), 120, ignore);

        c.removePair ((EmptyPair)ignore[0]);
        assertEquals ("A changes has been delivered", 1, listener.cnt);
    }

    /** Simple pair with no data */
    static class EmptyPair extends AbstractLookup.Pair {
        protected boolean creatorOf(Object obj) { return false; }
        public String getDisplayName() { return ""; }
        public String getId() { return ""; }
        public Object getInstance() { return null; }
        public Class getType() { return Object.class; }
        protected boolean instanceOf(Class c) { return c == getType (); }
    } // end of EmptyPair

    /** Pair with one item (like InstanceContent.Pair) */
    private static class OneItemPair extends EmptyPair {
        private Object pointer;
    }
}
