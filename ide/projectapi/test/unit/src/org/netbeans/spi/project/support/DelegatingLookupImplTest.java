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

package org.netbeans.spi.project.support;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class DelegatingLookupImplTest extends NbTestCase {

    public DelegatingLookupImplTest(String name) {
        super(name);
    }

    public void testCreateCompositeLookup() {
        LookupMergerImpl merger = new LookupMergerImpl();
        Lookup base = Lookups.fixed(new JButton(), new JComboBox(), merger);
        LookupProviderImpl pro1 = new LookupProviderImpl();
        LookupProviderImpl pro2 = new LookupProviderImpl();
        LookupProviderImpl pro3 = new LookupProviderImpl();

        InstanceContent provInst = new InstanceContent();
        Lookup providers = new AbstractLookup(provInst);
        provInst.add(pro1);
        provInst.add(pro2);

        pro1.ic.add(new JTextField());
        pro2.ic.add(new JTextArea());

        DelegatingLookupImpl del = new DelegatingLookupImpl(base, providers, "<irrelevant>");

        assertNotNull(del.lookup(JTextArea.class));
        assertNotNull(del.lookup(JComboBox.class));

        // test merger..
        JButton butt = del.lookup(JButton.class);
        assertNotNull(butt);
        assertEquals("CORRECT", butt.getText());
        assertEquals(1, del.lookupAll(JButton.class).size());
        assertEquals(1, merger.expectedCount);

        pro3.ic.add(new JButton());
        pro3.ic.add(new JRadioButton());
        provInst.add(pro3);
        assertNotNull(del.lookup(JRadioButton.class));

        // test merger..
        assertEquals(butt, del.lookup(JButton.class));
        assertEquals(1, del.lookupAll(JButton.class).size());
        assertEquals(2, merger.expectedCount);

        pro1.ic.add(new JButton());

        // test merger..
        assertEquals(butt, del.lookup(JButton.class));
        assertEquals(1, del.lookupAll(JButton.class).size());
        assertEquals(3, merger.expectedCount);

    }
    
    public void testNestedComposites() throws Exception { // #200711
        final AtomicInteger count = new AtomicInteger();
        final Runnable orig = new Runnable() {
            @Override public void run() {
                count.incrementAndGet();
            }
        };
        class RunnableMerger implements LookupMerger<Runnable> {
            @Override public Class<Runnable> getMergeableClass() {return Runnable.class;}
            @Override public Runnable merge(final Lookup lookup) {
                return new Runnable() {
                    @Override public void run() {
//                        orig.run(); jglick: this means each or the mergers calls this no matter what..
                        //but the correct invocation stack is eg. merger2->merger1->orig
                        for (Runnable r : lookup.lookupAll(Runnable.class)) {
//                            assertFalse(r == orig);
                            assertFalse(r == this);
                            r.run();
                        }
                    }
                };
            }
        }
        //merger in base, runnable in base
        Lookup base = Lookups.fixed(new RunnableMerger(), orig);
        Lookup nested1 = new DelegatingLookupImpl(base, Lookup.EMPTY, null);
        assertEquals(1, nested1.lookupAll(Runnable.class).size());
        Lookup nested2 = new DelegatingLookupImpl(nested1, Lookup.EMPTY, null);
        Collection<? extends Runnable> rs = nested2.lookupAll(Runnable.class);
        assertEquals(1, rs.size());
        rs.iterator().next().run();
        assertEquals(1, count.get());

        //runnable in base, merger in delegate1
        base = Lookups.fixed(orig);
        nested1 = new DelegatingLookupImpl(base, Lookups.fixed(new RunnableMerger()), null);
        assertEquals(1, nested1.lookupAll(Runnable.class).size());
        nested1.lookupAll(Runnable.class).iterator().next().run();
        assertEquals(2, count.get());

        nested2 = new DelegatingLookupImpl(nested1, Lookup.EMPTY, null);
        rs = nested2.lookupAll(Runnable.class);
        assertEquals(1, rs.size());
        rs.iterator().next().run();
        assertEquals(3, count.get());
        
        
        //runnable in delegate1, merger in base
        base = Lookups.fixed(new RunnableMerger());
        nested1 = new DelegatingLookupImpl(base, Lookups.fixed(new LookupProvider() {
            @Override
            public Lookup createAdditionalLookup(Lookup baseContext) {
                return Lookups.fixed(orig);
            }
        }), null);
        assertEquals(1, nested1.lookupAll(Runnable.class).size());
        nested1.lookupAll(Runnable.class).iterator().next().run();
        assertEquals(4, count.get());
        
        nested2 = new DelegatingLookupImpl(nested1, Lookup.EMPTY, null);
        rs = nested2.lookupAll(Runnable.class);
        assertEquals(1, rs.size());
        rs.iterator().next().run();
        assertEquals(5, count.get());
        
        //runnable in delegate2, merger in base
        base = Lookups.fixed(new RunnableMerger());
        nested1 = new DelegatingLookupImpl(base, Lookup.EMPTY, null);
        assertEquals(1, nested1.lookupAll(Runnable.class).size());
        nested1.lookupAll(Runnable.class).iterator().next().run();
        assertEquals(5, count.get()); //no change
        
        nested2 = new DelegatingLookupImpl(nested1, Lookups.fixed(new LookupProvider() {
            @Override
            public Lookup createAdditionalLookup(Lookup baseContext) {
                return Lookups.fixed(orig);
            }
        }), null);
        rs = nested2.lookupAll(Runnable.class);
        assertEquals(1, rs.size());
        rs.iterator().next().run();
        assertEquals(6, count.get());
        
        
    }    

    private static class LookupMergerImpl implements LookupMerger<JButton> {

        int expectedCount;

        @Override public Class<JButton> getMergeableClass() {
            return JButton.class;
        }

        @Override public JButton merge(final Lookup lookup) {
            expectedCount = lookup.lookupAll(JButton.class).size();
            lookup.lookupResult(JButton.class).addLookupListener(new LookupListener() {
                public @Override void resultChanged(LookupEvent ev) {
                    expectedCount = lookup.lookupAll(JButton.class).size();
                }
            });
            return new JButton("CORRECT");
        }

    }

    private static class LookupProviderImpl implements LookupProvider {
        InstanceContent ic = new InstanceContent();
        boolean wasAlreadyCalled = false;
        @Override public Lookup createAdditionalLookup(Lookup baseContext) {
            assertNotNull(baseContext.lookup(JButton.class));
            assertNull(baseContext.lookup(JCheckBox.class));
            assertFalse(wasAlreadyCalled);
            wasAlreadyCalled = true;
            return new AbstractLookup(ic);
        }
    }

}
