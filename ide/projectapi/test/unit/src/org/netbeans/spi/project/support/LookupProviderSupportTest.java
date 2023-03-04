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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockChangeListener;

/**
 * @author mkleint
 */
public class LookupProviderSupportTest extends NbTestCase {
    
    public LookupProviderSupportTest(String testName) {
        super(testName);
    }

    private SourcesImpl createImpl(String id) {
        SourcesImpl impl0 = new SourcesImpl();
        SourceGroupImpl grp0 = new SourceGroupImpl();
        grp0.name = id;
        impl0.grpMap.put("java", Arrays.<SourceGroup>asList(grp0));
        return impl0;
    }
    
    public void testSourcesMerger() {
        SourcesImpl impl0 = createImpl("group0");
        SourcesImpl impl1 = createImpl("group1");
        SourcesImpl impl2 = createImpl("group2");
        SourcesImpl impl3 = createImpl("group3");
        
        Lookup base = Lookups.fixed(impl0, LookupProviderSupport.createSourcesMerger());
        LookupProviderImpl2 pro1 = new LookupProviderImpl2();
        LookupProviderImpl2 pro2 = new LookupProviderImpl2();
        LookupProviderImpl2 pro3 = new LookupProviderImpl2();
        
        InstanceContent provInst = new InstanceContent();
        Lookup providers = new AbstractLookup(provInst);
        provInst.add(pro1);
        provInst.add(pro2);
        
        pro1.ic.add(impl1);
        pro2.ic.add(impl2);
        pro3.ic.add(impl3);
        
        DelegatingLookupImpl del = new DelegatingLookupImpl(base, providers, "<irrelevant>");
        
        Sources srcs = del.lookup(Sources.class); 
        assertNotNull(srcs);
        SourceGroup[] grps = srcs.getSourceGroups("java");
        assertEquals(3, grps.length);
        
        //now let's add another module to the bunch and see if the new SG appears
        provInst.add(pro3);
        
        srcs = del.lookup(Sources.class); 
        assertNotNull(srcs);
        grps = srcs.getSourceGroups("java");
        assertEquals(4, grps.length);
        
        //now let's remove another module to the bunch and see if the SG disappears
        provInst.remove(pro2);
        
        srcs = del.lookup(Sources.class); 
        assertNotNull(srcs);
        grps = srcs.getSourceGroups("java");
        assertEquals(3, grps.length);
        
        //lets remove one and listen for changes...
        srcs = del.lookup(Sources.class); 
        MockChangeListener ch = new MockChangeListener();
        srcs.addChangeListener(ch);
        provInst.remove(pro1);
        
        ch.assertEvent();
        grps = srcs.getSourceGroups("java");
        assertEquals(2, grps.length);
        
        provInst.add(pro2);
        
        ch.assertEvent();
        grps = srcs.getSourceGroups("java");
        assertEquals(3, grps.length);
        
    }

    public void testActionProviderMerger() throws Exception {
        final ActionProviderImpl ap1 = new ActionProviderImpl(new LinkedHashMap<String,Boolean>(){
            {
                put(ActionProvider.COMMAND_CLEAN,Boolean.TRUE);
                put(ActionProvider.COMMAND_BUILD,Boolean.TRUE);
                put(ActionProvider.COMMAND_REBUILD,Boolean.TRUE);
                put(ActionProvider.COMMAND_COMPILE_SINGLE,Boolean.FALSE);
            }
        });
        final ActionProviderImpl ap2 = new ActionProviderImpl(new LinkedHashMap<String,Boolean>(){
            {
                put(ActionProvider.COMMAND_RUN,Boolean.TRUE);
                put(ActionProvider.COMMAND_TEST,Boolean.TRUE);
                put(ActionProvider.COMMAND_DEBUG,Boolean.FALSE);
                put(ActionProvider.COMMAND_COMPILE_SINGLE,Boolean.TRUE);
            }
        });

        final LookupMerger<ActionProvider> merger = LookupProviderSupport.createActionProviderMerger();
        assertEquals(ActionProvider.class,merger.getMergeableClass());
        final ActionProvider ap = merger.merge(Lookups.fixed(ap1,ap2));
        assertEquals(Arrays.asList(new String[] {
                ActionProvider.COMMAND_CLEAN,
                ActionProvider.COMMAND_BUILD,
                ActionProvider.COMMAND_REBUILD,
                ActionProvider.COMMAND_COMPILE_SINGLE,
                ActionProvider.COMMAND_RUN,
                ActionProvider.COMMAND_TEST,
                ActionProvider.COMMAND_DEBUG,
            }),
            Arrays.asList(ap.getSupportedActions()));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_CLEAN, Lookup.EMPTY));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_BUILD, Lookup.EMPTY));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_REBUILD, Lookup.EMPTY));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, Lookup.EMPTY));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_RUN, Lookup.EMPTY));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_TEST, Lookup.EMPTY));
        assertFalse(ap.isActionEnabled(ActionProvider.COMMAND_DEBUG, Lookup.EMPTY));
        try {
            ap.isActionEnabled(ActionProvider.COMMAND_MOVE, Lookup.EMPTY);
            throw new AssertionError("IAE should be thrown");   //NOI18N
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains(ActionProvider.COMMAND_MOVE));
        }
        ap.invokeAction(ActionProvider.COMMAND_CLEAN, Lookup.EMPTY);
        assertEquals(ActionProvider.COMMAND_CLEAN,ap1.cleanInvokedTarget());
        assertNull(ap2.cleanInvokedTarget());
        ap.invokeAction(ActionProvider.COMMAND_BUILD, Lookup.EMPTY);
        assertEquals(ActionProvider.COMMAND_BUILD,ap1.cleanInvokedTarget());
        assertNull(ap2.cleanInvokedTarget());
        ap.invokeAction(ActionProvider.COMMAND_REBUILD, Lookup.EMPTY);
        assertEquals(ActionProvider.COMMAND_REBUILD,ap1.cleanInvokedTarget());
        assertNull(ap2.cleanInvokedTarget());
        ap.invokeAction(ActionProvider.COMMAND_COMPILE_SINGLE, Lookup.EMPTY);
        assertNull(ap1.cleanInvokedTarget());
        assertEquals(ActionProvider.COMMAND_COMPILE_SINGLE,ap2.cleanInvokedTarget());
        ap.invokeAction(ActionProvider.COMMAND_RUN, Lookup.EMPTY);
        assertNull(ap1.cleanInvokedTarget());
        assertEquals(ActionProvider.COMMAND_RUN,ap2.cleanInvokedTarget());
        ap.invokeAction(ActionProvider.COMMAND_TEST, Lookup.EMPTY);
        assertNull(ap1.cleanInvokedTarget());
        assertEquals(ActionProvider.COMMAND_TEST,ap2.cleanInvokedTarget());
        try {
            ap.invokeAction(ActionProvider.COMMAND_DEBUG, Lookup.EMPTY);
            throw new AssertionError("IAE should be thrown");   //NOI18N
        } catch (IllegalArgumentException iae) {
            assertEquals(ActionProvider.COMMAND_DEBUG, iae.getMessage());
        }
        try {
            ap.invokeAction(ActionProvider.COMMAND_MOVE, Lookup.EMPTY);
            throw new AssertionError("IAE should be thrown");   //NOI18N
        } catch (IllegalArgumentException iae) {
            assertEquals(ActionProvider.COMMAND_MOVE, iae.getMessage());
        }
    }

    public void testNonexistentPath() throws Exception {
        // #87544: don't choke on a nonexistent path! Just leave it empty.
        Lookup l = LookupProviderSupport.createCompositeLookup(Lookup.EMPTY, "nowhere");
        assertEquals(Collections.<Object>emptySet(), new HashSet<Object>(l.lookupAll(Object.class)));
    }

    public void testNestedComposites() throws Exception {
        SourcesImpl impl1 = createImpl("group1");
        SourcesImpl impl2 = createImpl("group2");
        SourcesImpl impl3 = createImpl("group3");
        Lookup base = Lookups.fixed(impl1, LookupProviderSupport.createSourcesMerger());
        class Prov implements LookupProvider {
            final SourcesImpl instance;
            Prov(SourcesImpl instance) {
                this.instance = instance;
            }
            public @Override Lookup createAdditionalLookup(Lookup baseContext) {
                return Lookups.singleton(instance);
            }
        }
        Lookup inner = new DelegatingLookupImpl(base, Lookups.fixed(new Prov(impl2)), null);
        Lookup outer = new DelegatingLookupImpl(inner, Lookups.fixed(new Prov(impl3)), null);
        List<String> names = new ArrayList<String>();
        for (SourceGroup g : outer.lookup(Sources.class).getSourceGroups("java")) {
            names.add(g.getName());
        }
        Collections.sort(names);
        assertEquals("[group1, group2, group3]", names.toString());
    }



    public void testSharabilityQueryMerger() throws IOException {
        final File wd = getWorkDir();
        final File f1 = new File (wd, "f1");    //NOI18N
        final File f2 = new File (wd, "f2");    //NOI18N
        final File f3 = new File (wd, "f3");    //NOI18N
        final File f4 = new File (wd, "f4");    //NOI18N
        final File f5 = new File (wd, "f5");    //NOI18N

        final SharabilityQueryImpl impl1 = new SharabilityQueryImpl(Collections.singletonMap(f1.toURI(), SharabilityQuery.Sharability.SHARABLE));
        final SharabilityQueryImpl impl2 = new SharabilityQueryImpl(Collections.singletonMap(f2.toURI(), SharabilityQuery.Sharability.NOT_SHARABLE));
        final SharabilityQueryImpl impl3 = new SharabilityQueryImpl(new HashMap<URI, SharabilityQuery.Sharability>(){{
            put(f3.toURI(),SharabilityQuery.Sharability.SHARABLE);
            put(f4.toURI(),SharabilityQuery.Sharability.NOT_SHARABLE);
        }});
        final SharabilityQueryImpl impl4 = new SharabilityQueryImpl(Collections.singletonMap(f2.toURI(), SharabilityQuery.Sharability.SHARABLE));
        Lookup base = Lookups.fixed(impl1, LookupProviderSupport.createSharabilityQueryMerger());
        LookupProviderImpl2 pro2 = new LookupProviderImpl2();
        LookupProviderImpl2 pro3 = new LookupProviderImpl2();
        LookupProviderImpl2 pro4 = new LookupProviderImpl2();

        InstanceContent provInst = new InstanceContent();
        Lookup providers = new AbstractLookup(provInst);
        provInst.add(pro2);
        provInst.add(pro3);
        provInst.add(pro4);
        pro2.ic.add(impl2);
        pro3.ic.add(impl3);
        pro4.ic.add(impl4);
        DelegatingLookupImpl del = new DelegatingLookupImpl(base, providers, "<irrelevant>");
        
        SharabilityQueryImplementation2 sharability = del.lookup(SharabilityQueryImplementation2.class);
        assertNotNull(sharability);
        assertEquals(SharabilityQuery.Sharability.SHARABLE, sharability.getSharability(f1.toURI()));
        assertEquals(SharabilityQuery.Sharability.NOT_SHARABLE, sharability.getSharability(f2.toURI()));
        assertEquals(SharabilityQuery.Sharability.SHARABLE, sharability.getSharability(f3.toURI()));
        assertEquals(SharabilityQuery.Sharability.NOT_SHARABLE, sharability.getSharability(f4.toURI()));
        assertEquals(SharabilityQuery.Sharability.UNKNOWN, sharability.getSharability(f5.toURI()));
    }

    private class LookupProviderImpl2 implements LookupProvider {
        InstanceContent ic = new InstanceContent();
        AbstractLookup l;
        public Lookup createAdditionalLookup(Lookup baseContext) {
            if (l == null) {
                l = new AbstractLookup(ic);
            }
            return l;
        }
    }
    
    private static class SourcesImpl implements Sources {
        public Map<String,List<SourceGroup>> grpMap = new HashMap<String,List<SourceGroup>>();
        
        public SourceGroup[] getSourceGroups(String type) {
            return grpMap.get(type).toArray(new SourceGroup[0]);
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }

        public @Override String toString() {
            return grpMap.toString();
        }
    }
    
    private static class SourceGroupImpl implements SourceGroup {

        String name;

        String displayName;
        public FileObject getRootFolder() {
            return null;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override public boolean contains(FileObject file) {
            return false;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public @Override String toString() {
            return name;
        }
    }

    private static class ActionProviderImpl implements ActionProvider {
        private final Map<String,Boolean> supportedCommands;
        private String invokedTarget;

        public ActionProviderImpl (final Map<String,Boolean> supportedCommands) {
            this.supportedCommands = supportedCommands;
        }

        @Override
        public String[] getSupportedActions() {
            return supportedCommands.keySet().toArray(new String[supportedCommands.size()]);
        }

        @Override
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            invokedTarget = command;
        }

        @Override
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            final Boolean res = supportedCommands.get(command);
            if (res == null) {
                throw new IllegalArgumentException(command);
            }
            return res;
        }

        String cleanInvokedTarget() {
            final String res = invokedTarget;
            invokedTarget = null;
            return res;
        }
    }

    private static final class SharabilityQueryImpl implements SharabilityQueryImplementation2 {

        private final Map<URI,SharabilityQuery.Sharability> sharability;

        SharabilityQueryImpl(@NonNull final Map<URI,SharabilityQuery.Sharability> sharability) {
            this.sharability = sharability;
        }

        @Override
        @NonNull
        public SharabilityQuery.Sharability getSharability(@NonNull final URI uri) {
            SharabilityQuery.Sharability res = sharability.get(uri);
            if (res == null) {
                res = SharabilityQuery.Sharability.UNKNOWN;
            }
            return res;
        }
    }
}
