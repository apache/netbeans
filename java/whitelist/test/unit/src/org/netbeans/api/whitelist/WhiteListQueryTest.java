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
package org.netbeans.api.whitelist;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.whitelist.WhiteListQuery.Operation;
import org.netbeans.api.whitelist.WhiteListQuery.Result;
import org.netbeans.api.whitelist.WhiteListQuery.RuleDescription;
import org.netbeans.api.whitelist.WhiteListQuery.WhiteList;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.whitelist.WhiteListQueryImplementationMerged;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tomas Zezula
 */
public class WhiteListQueryTest extends NbTestCase {

    public WhiteListQueryTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }

    public void testSingleQUeryTest() throws IOException {
        MockServices.setServices(CustomizableWLQuery.class);
        final File wd = getWorkDir();
        final FileObject root1 = FileUtil.createFolder(new File (wd,"src1"));   //NOI18N
        final FileObject root2 = FileUtil.createFolder(new File (wd,"src2"));   //NOI18N
        CustomizableWLQuery.customize(
                    Collections.singleton(root1),
                    "CORBA",                //NOI18N
                    "CORBA-Disabled",       //NOI18N
                    "No CORBA allowed",     //NOI18N
                    "org.omg.CORBA",        //NOI18N
                    "org.omg.CORBA_2_3"     //NOI18N
                );
        //Test for root1 (CORBA white list should be enabled)
        WhiteList wl = WhiteListQuery.getWhiteList(root1);
        assertNotNull(wl);
        Result res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.lang.String.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertTrue(res.isAllowed());
        assertTrue(res.getViolatedRules().isEmpty());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, org.omg.CORBA.BAD_OPERATION.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
        assertEquals(1,res.getViolatedRules().size());
        assertEquals("CORBA-Disabled", res.getViolatedRules().iterator().next().getRuleName()); //NOI18N

        //Test for root2 (no white list should be enabled)
        wl = WhiteListQuery.getWhiteList(root2);
        assertNull(wl);
    }

    public void testMergedWhiteListForExclusiveType() throws IOException {
        MockServices.setServices(CustomizableWLQuery.class, RMIBLQuery.class);
        final File wd = getWorkDir();
        final FileObject root1 = FileUtil.createFolder(new File (wd,"src1"));   //NOI18N
        final FileObject root2 = FileUtil.createFolder(new File (wd,"src2"));   //NOI18N
        CustomizableWLQuery.customize(
                    Collections.singleton(root1),
                    "CORBA",                //NOI18N
                    "CORBA-Disabled",       //NOI18N
                    "No CORBA allowed",     //NOI18N
                    "org.omg.CORBA",        //NOI18N
                    "org.omg.CORBA_2_3"     //NOI18N
                );
        //Test for root1 (both whitelists are enabled)
        WhiteList wl = WhiteListQuery.getWhiteList(root1);
        assertNotNull(wl);
        Result res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.lang.String.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertTrue(res.isAllowed());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.rmi.Remote.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
        assertEquals(1,res.getViolatedRules().size());
        assertEquals("RMI-Disabled", res.getViolatedRules().iterator().next().getRuleName());   //NOI18N
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, org.omg.CORBA.BAD_OPERATION.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
        assertEquals(1,res.getViolatedRules().size());
        assertEquals("CORBA-Disabled", res.getViolatedRules().iterator().next().getRuleName()); //NOI18N

        //Test for root2 (only RMI whitelist should be enabled)
        wl = WhiteListQuery.getWhiteList(root2);
        assertNotNull(wl);
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.lang.String.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertTrue(res.isAllowed());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.rmi.Remote.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
        assertEquals(1,res.getViolatedRules().size());
        assertEquals("RMI-Disabled", res.getViolatedRules().iterator().next().getRuleName());   //NOI18N
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, org.omg.CORBA.BAD_OPERATION.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertTrue(res.isAllowed());
    }

    public void testMergedWhiteListForSameType() throws IOException {
        MockServices.setServices(CustomizableWLQuery.class, RMIBLQuery.class);
        final File wd = getWorkDir();
        final FileObject root1 = FileUtil.createFolder(new File (wd,"src1"));   //NOI18N
        final FileObject root2 = FileUtil.createFolder(new File (wd,"src2"));   //NOI18N
        CustomizableWLQuery.customize(
                    Collections.singleton(root1),
                    "RMI-2",            //NOI18N
                    "RMI-Disabled-2",   //NOI18N
                    "No RMI Allowed",   //NOI18N
                    "java.rmi"          //NOI18N
                );
        //Test for root1 (both whitelists are enabled)
        WhiteList wl = WhiteListQuery.getWhiteList(root1);
        assertNotNull(wl);
        Result res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.lang.String.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertTrue(res.isAllowed());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.rmi.Remote.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
        assertViolations(new String[]{"RMI-Disabled","RMI-Disabled-2"},res.getViolatedRules());     //NOI18N

        //Test for root2 (only RMI whitelist should be enabled)
        wl = WhiteListQuery.getWhiteList(root2);
        assertNotNull(wl);
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.lang.String.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertTrue(res.isAllowed());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.rmi.Remote.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
        assertEquals(1,res.getViolatedRules().size());
        assertEquals("RMI-Disabled", res.getViolatedRules().iterator().next().getRuleName());   //NOI18N
    }

    public void testWhiteListListening_changeInWhiteListImpl() throws Exception {
        MockServices.setServices(CustomizableWLQuery.class);

        final File wd = getWorkDir();
        final FileObject root1 = FileUtil.createFolder(new File (wd,"src1"));   //NOI18N
        CustomizableWLQuery.customize(
                    Collections.singleton(root1),
                    "CORBA",                //NOI18N
                    "CORBA-Disabled",       //NOI18N
                    "No CORBA allowed",     //NOI18N
                    "org.omg.CORBA"         //NOI18N
                );
        final WhiteList wl = WhiteListQuery.getWhiteList(root1);
        assertNotNull(wl);
        WhiteListQuery.Result res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, org.omg.CORBA.BAD_OPERATION.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, org.omg.CORBA_2_3.ORB.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertTrue(res.isAllowed());


        final AtomicInteger called = new AtomicInteger();
        wl.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                called.incrementAndGet();
            }
        });
        CustomizableWLQuery.customize(
                Collections.singleton(root1),
                    "CORBA",                //NOI18N
                    "CORBA-Disabled",       //NOI18N
                    "No CORBA allowed",     //NOI18N
                    "org.omg.CORBA",        //NOI18N
                    "org.omg.CORBA_2_3"     //NOI18N
                );
        assertEquals(1, called.get());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, org.omg.CORBA.BAD_OPERATION.class.getName()), Operation.USAGE);
        assertFalse(res.isAllowed());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, org.omg.CORBA_2_3.ORB.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
    }

    public void testWhiteListListening_changeInLookup() throws Exception {
        final File wd = getWorkDir();
        final FileObject root1 = FileUtil.createFolder(new File (wd,"src1"));   //NOI18N
        class L extends ProxyLookup {
            public void setServices(Object... services) {
                setLookups(Lookups.fixed(services));
            }
        }
        final L lkp = new L();
        final WhiteListQueryImplementationMerged mergedWLQ = new WhiteListQueryImplementationMerged(lkp);
        assertNull(mergedWLQ.getWhiteList(root1));
        final RMIBLQuery q1 = new RMIBLQuery();
        final CustomizableWLQuery q2 = new CustomizableWLQuery();
        CustomizableWLQuery.customize(
            Collections.singleton(root1),
                "CORBA",                //NOI18N
                "CORBA-Disabled",       //NOI18N
                "No CORBA allowed",     //NOI18N
                "org.omg.CORBA"
            );
        lkp.setServices(q1);
        final WhiteListQueryImplementation.WhiteListImplementation wl = mergedWLQ.getWhiteList(root1);
        assertNotNull(wl);
        WhiteListQuery.Result res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, org.omg.CORBA.BAD_OPERATION.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertTrue(res.isAllowed());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.rmi.Remote.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
        final AtomicInteger cc = new AtomicInteger();
        wl.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                cc.incrementAndGet();
            }
        });
        lkp.setServices(q1, q2);
        assertEquals(1, cc.get());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, org.omg.CORBA.BAD_OPERATION.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
        res = wl.check(ElementHandle.createTypeElementHandle(ElementKind.CLASS, java.rmi.Remote.class.getName()), Operation.USAGE);
        assertNotNull(res);
        assertFalse(res.isAllowed());
    }

    private static void assertViolations(final String[] expected, final List<? extends RuleDescription> result) {
        final Set<String> ws = new HashSet<String>();
        for (RuleDescription rd : result) {
            ws.add(rd.getRuleName());
        }
        assertEquals(expected.length, ws.size());
        for (String s : expected) {
            assertTrue(ws.remove(s));
        }
    }


    private static class WhiteListImpl implements WhiteListQueryImplementation.WhiteListImplementation {

        private final ChangeSupport cs = new ChangeSupport(this);
        private Set<String> forbiddenPkgs;
        private String wlName;
        private String rName;
        private String rDesc;

        private WhiteListImpl (
                final String wlName,
                final String rName,
                final String rDesc,
                final String... forbiddenPkgs) {
            customize(wlName, rName, rDesc, forbiddenPkgs);
        }

        private synchronized void customize (
                final String wlName,
                final String rName,
                final String rDesc,
                final String... forbiddenPkgs) {
            this.wlName = wlName;
            this.rName = rName;
            this.rDesc = rDesc;
            final Set<String> fp = new HashSet<String>();
            for (String forPkg : forbiddenPkgs) {
                fp.add(forPkg+'.');
            }
            this.forbiddenPkgs = Collections.unmodifiableSet(fp);
            cs.fireChange();
        }

        @Override
        public synchronized Result check(ElementHandle<?> element, Operation operation) {
            final String[] vmSig = SourceUtils.getJVMSignature(element);
            for (String fp : forbiddenPkgs) {
                if (vmSig[0].startsWith(fp)) {
                    return new Result(Collections.singletonList(new RuleDescription(rName, rDesc, wlName)));
                }
            }
            return new Result();
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }
    }

    public static class CustomizableWLQuery implements WhiteListQueryImplementation {

        private static Set<FileObject> supportedFor;
        private static final AtomicReference<WhiteListImpl> impl = new AtomicReference<WhiteListImpl>();

        static synchronized void customize (
                final Set<FileObject> files,
                final String wln,
                final String rn,
                final String rd,
                final String... pkgs) {
            supportedFor = files;
            final WhiteListImpl wl = getWhiteListImpl();
            wl.customize(wln, rn, rd, pkgs);
        }

        @Override
        public WhiteListImplementation getWhiteList(FileObject file) {
            synchronized (CustomizableWLQuery.class) {
                if (supportedFor == null || !supportedFor.contains(file)) {
                    return null;
                }
                return getWhiteListImpl();
            }
        }

        private static WhiteListImpl getWhiteListImpl() {
            WhiteListImpl res = impl.get();
            if (res == null) {
                res =  new WhiteListImpl(
                    null,
                    null,
                    null);
                if (!impl.compareAndSet(null, res)) {
                    res = impl.get();
                }
            }
            return res;
        }
    }

    public static class RMIBLQuery implements WhiteListQueryImplementation {

        @Override
        public WhiteListImplementation getWhiteList(FileObject file) {
            return new WhiteListImpl("RMI", "RMI-Disabled", "No RMI allowed", "java.rmi");  //NOI18N
        }
    }
}
