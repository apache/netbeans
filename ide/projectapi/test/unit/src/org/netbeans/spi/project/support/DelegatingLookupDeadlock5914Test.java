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

public class DelegatingLookupDeadlock5914Test extends NbTestCase {

    public DelegatingLookupDeadlock5914Test(String name) {
        super(name);
    }

    public void testDontHoldLockWhenNotifyingChanges() {
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
        class LL implements LookupListener {
            int cnt;

            @Override
            public void resultChanged(LookupEvent ev) {
                assertFalse("Cannot hold lock when notifying changes!", del.holdsLock());
                cnt++;
            }
        }
        LL jbuttonListener = new LL();
        Lookup.Result<JButton> jbuttonResult = del.lookupResult(JButton.class);
        jbuttonResult.addLookupListener(jbuttonListener);
        assertEquals("One button", 1, jbuttonResult.allInstances().size());

        Lookup.Result<JRadioButton> jradioButtonResult = del.lookupResult(JRadioButton.class);
        LL jradioButtonListener = new LL();
        jradioButtonResult.addLookupListener(jradioButtonListener);
        assertEquals("No radio button", 0, jradioButtonResult.allInstances().size());

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

        assertEquals("A change delivered", 1, jradioButtonListener.cnt);
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
