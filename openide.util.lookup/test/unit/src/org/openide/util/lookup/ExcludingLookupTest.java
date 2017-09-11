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

import java.util.*;
import org.openide.util.Lookup;

/** Runs all NbLookupTest tests on ProxyLookup and adds few additional.
 */
@SuppressWarnings("unchecked") // XXX ought to be corrected, just a lot of them
public class ExcludingLookupTest extends AbstractLookupBaseHid
implements AbstractLookupBaseHid.Impl {
    public ExcludingLookupTest(java.lang.String testName) {
        super(testName, null);
    }

    public Lookup createLookup (final Lookup lookup) {
        return Lookups.exclude (lookup, new Class[0]);
    }
    
    public Lookup createInstancesLookup (InstanceContent ic) {
        return new AbstractLookup (ic);
    }

    public void clearCaches () {
    }    
    
    public void testWeCanRemoveInteger () throws Exception {
        doBasicFilteringTest (Integer.class, Integer.class, 0);
    }
    
    public void testWeCanRemoveIntegersEvenByAskingForRemoveOfAllNumbers () throws Exception {
        doBasicFilteringTest (Number.class, Integer.class, 0);
    }
    public void testFunWithInterfaces () throws Exception {
        doBasicFilteringTest (java.io.Serializable.class, Integer.class, 0);
    }
    
    public void testWeCanGetInstanceOfSerializableEvenItIsExcludedIfWeAskForClassNotExtendingIt () throws Exception {
        Lookup lookup = Lookups.exclude (this.instanceLookup, new Class[] { java.io.Serializable.class });
        Lookup.Template t = new Lookup.Template (Object.class);
        Lookup.Result res = lookup.lookup (t);
        
        LL ll = new LL ();
        res.addLookupListener (ll);
        assertEquals ("Nothing is there", 0, res.allItems ().size ());
        
        Object inst = new Integer (3);
        ic.add (inst);
        
        assertEquals ("Not Filtered out", inst, lookup.lookup (Object.class));
        assertEquals ("Not Filtered out2", inst, lookup.lookupItem (t).getInstance ());
        assertEquals ("One is there - 2", 1, res.allItems ().size ());
        assertEquals ("One is there - 2a", 1, res.allInstances ().size ());
        assertEquals ("One is there - 2b", 1, res.allClasses ().size ());
        assertEquals ("Right # of events", 1, ll.getCount ());
        
        ic.remove (inst);
        assertEquals ("Filtered out3", null, lookup.lookupItem (t));
        assertEquals ("Nothing is there - 3", 0, res.allItems ().size ());
        assertEquals ("Nothing is there - 3a", 0, res.allInstances ().size ());
        assertEquals ("Nothing is there - 3b", 0, res.allClasses ().size ());
        assertEquals ("Of course it is not there", null, lookup.lookup (Object.class));
        assertEquals ("Right # of events", 1, ll.getCount ());
    }
    
    public void testIntegersQueriedThruObject () throws Exception {
        doBasicFilteringTest (Number.class, Object.class, 1);
    }
    
    private void doBasicFilteringTest (Class theFilter, Class theQuery, int numberOfExcpectedEventsAfterOneChange) throws Exception {
        Lookup lookup = Lookups.exclude (this.instanceLookup, new Class[] { theFilter });
        Lookup.Template t = new Lookup.Template (theQuery);
        Lookup.Result res = lookup.lookup (t);
        
        LL ll = new LL ();
        res.addLookupListener (ll);
        assertEquals ("Nothing is there", 0, res.allItems ().size ());
        
        Object inst = new Integer (3);
        ic.add (inst);
        
        assertEquals ("Filtered out", null, lookup.lookup (theQuery));
        assertEquals ("Filtered out2", null, lookup.lookupItem (t));
        assertEquals ("Nothing is there - 2", 0, res.allItems ().size ());
        assertEquals ("Nothing is there - 2a", 0, res.allInstances ().size ());
        assertEquals ("Nothing is there - 2b", 0, res.allClasses ().size ());
        assertEquals ("Right # of events", numberOfExcpectedEventsAfterOneChange, ll.getCount ());
        
        ic.remove (inst);
        assertEquals ("Filtered out3", null, lookup.lookupItem (t));
        assertEquals ("Nothing is there - 3", 0, res.allItems ().size ());
        assertEquals ("Nothing is there - 3a", 0, res.allInstances ().size ());
        assertEquals ("Nothing is there - 3b", 0, res.allClasses ().size ());
        assertEquals ("Of course it is not there", null, lookup.lookup (theQuery));
        assertEquals ("Right # of events", numberOfExcpectedEventsAfterOneChange, ll.getCount ());
        
    }
    
    public void testSizeOfTheLookup () throws Exception {
        Class exclude = String.class;
        
        Lookup lookup = Lookups.exclude (this.instanceLookup, new Class[] { exclude });

        assertSize ("Should be pretty lightweight", Collections.singleton (lookup), 24, 
                new Object[] { this.instanceLookup, exclude });
    }
    public void testSizeOfTheLookupForMultipleFiltersIsHigher () throws Exception {
        Class exclude = String.class;
        Class exclude2 = Integer.class;
        Class[] arr = new Class[] { exclude, exclude2 };
        
        Lookup lookup = Lookups.exclude (this.instanceLookup, arr);

        assertSize ("Is fatter", Collections.singleton (lookup), 48, 
                new Object[] { this.instanceLookup, exclude, exclude2 });
        assertSize ("But only due to the array", Collections.singleton (lookup), 24, 
                new Object[] { this.instanceLookup, exclude, exclude2, arr });
    }
    
    public void testFilteringOfSomething () throws Exception {
        doFilteringOfSomething (Runnable.class, java.io.Serializable.class, 1);
    }
    
    private void doFilteringOfSomething (Class theFilter, Class theQuery, int numberOfExcpectedEventsAfterOneChange) throws Exception {
        Lookup lookup = Lookups.exclude (this.instanceLookup, new Class[] { theFilter });
        Lookup.Template t = new Lookup.Template (theQuery);
        Lookup.Result res = lookup.lookup (t);
        
        LL ll = new LL ();
        res.addLookupListener (ll);
        assertEquals ("Nothing is there", 0, res.allItems ().size ());
        
        Object inst = new Integer (3);
        ic.add (inst);
        
        assertEquals ("Accepted", inst, lookup.lookup (theQuery));
        assertNotNull ("Accepted too", lookup.lookupItem (t));
        assertEquals ("One is there - 2", 1, res.allItems ().size ());
        assertEquals ("One is there - 2a", 1, res.allInstances ().size ());
        assertEquals ("One is there - 2b", 1, res.allClasses ().size ());
        assertEquals ("Right # of events", numberOfExcpectedEventsAfterOneChange, ll.getCount ());

        Object inst2 = new Thread (); // implements Runnable
        ic.add (inst2);
        assertEquals ("Accepted - 2", inst, lookup.lookup (theQuery));
        assertNotNull ("Accepted too -2", lookup.lookupItem (t));
        assertEquals ("One is there - 3", 1, res.allItems ().size ());
        assertEquals ("One is there - 3a", 1, res.allInstances ().size ());
        assertEquals ("One is there - 3b", 1, res.allClasses ().size ());
        assertEquals ("Right # of events", 0, ll.getCount ());
        
        
        ic.remove (inst);
        assertEquals ("Filtered out3", null, lookup.lookupItem (t));
        assertEquals ("Nothing is there - 3", 0, res.allItems ().size ());
        assertEquals ("Nothing is there - 3a", 0, res.allInstances ().size ());
        assertEquals ("Nothing is there - 3b", 0, res.allClasses ().size ());
        assertEquals ("Of course it is not there", null, lookup.lookup (theQuery));
        assertEquals ("Right # of events", numberOfExcpectedEventsAfterOneChange, ll.getCount ());
    }

    public void testTheBehaviourAsRequestedByDavidAndDescribedByJesse () throws Exception {
        class C implements Runnable, java.io.Serializable {
            public void run () {}
        }
        Object c = new C();
        Lookup l1 = Lookups.singleton(c);
        Lookup l2 = Lookups.exclude(l1, new Class[] {Runnable.class});
        assertNull(l2.lookup(Runnable.class));
        assertEquals(c, l2.lookup(java.io.Serializable.class));
    }
    
    public void testTheBehaviourAsRequestedByDavidAndDescribedByJesseWithUsageOfResult () throws Exception {
        class C implements Runnable, java.io.Serializable {
            public void run () {}
        }
        Object c = new C();
        Lookup l1 = Lookups.singleton(c);
        Lookup l2 = Lookups.exclude(l1, new Class[] {Runnable.class});
        
        Lookup.Result run = l2.lookup (new Lookup.Template (Runnable.class));
        Lookup.Result ser = l2.lookup (new Lookup.Template (java.io.Serializable.class));
        
        assertEquals ("Runnables filtered out", 0, run.allItems ().size ());
        assertEquals ("One serialiazble", 1, ser.allItems ().size ());
        assertEquals ("And it is c", c, ser.allInstances ().iterator ().next ());
    }
    
    public void testQueryImplClass() throws Exception {
        class Excl implements Runnable {
            @Override
            public void run() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        class C extends Excl {
        }
        
        Object c = new C();
        Lookup l1 = Lookups.singleton(c);
        Lookup l2 = Lookups.exclude(l1, new Class[] {Excl.class});

        Runnable run = l2.lookup(Runnable.class);
        C res = l2.lookup(C.class);
        assertEquals("Consistent with subtypes of an super interface", run, res);
        
        assertNull("The Runnable is excluded", run);
        assertNull("That is why the impl has to be excluded as well", res);
    }
}
