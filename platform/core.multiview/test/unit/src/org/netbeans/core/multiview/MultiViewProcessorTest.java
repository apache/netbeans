/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.core.multiview;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElementTest;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MultiViewProcessorTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewProcessorTest.class);
    }

    public MultiViewProcessorTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        MVE.closeState = null;
        CloseH.globalElements = null;
        CloseH.retValue = null;
        DD.d = null;
        DD.ret = -1;
        MockServices.setServices(DD.class);
    }

    public void testMultiViewsCreate() {
        TopComponent mvc = MultiViews.createMultiView("text/figaro", new LP(Lookup.EMPTY));
        assertNotNull("MultiViewComponent created", mvc);
        mvc.open();
        mvc.requestActive();
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvc);
        assertNotNull("Handler found", handler);
        MultiViewPerspective[] arr = handler.getPerspectives();
        assertEquals("Two perspetives found", 2, arr.length);
        assertEquals("Figaro", arr[0].getDisplayName());
        assertEquals("Figaro", arr[1].getDisplayName());
	MultiViewDescription description = Accessor.DEFAULT.extractDescription(arr[0]);
	assertTrue(description instanceof ContextAwareDescription);
        assertFalse("First one is not for split", ((ContextAwareDescription)description).isSplitDescription());
	description = Accessor.DEFAULT.extractDescription(arr[1]);
	assertTrue(description instanceof ContextAwareDescription);
        assertTrue("Second one is for split", ((ContextAwareDescription)description).isSplitDescription());

        CloseH.retValue = true;
        MVE.closeState = MultiViewFactory.createUnsafeCloseState("warn", new AbstractAction() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                MVE.closeState = null;
            }
        }, null);
        assertTrue("Closed OK", mvc.close());
        assertNotNull(CloseH.globalElements);
        assertEquals("One handle", 1, CloseH.globalElements.length);
        assertEquals("states are the same", MVE.closeState, CloseH.globalElements[0]);
    }

    public void testCloneableMultiViewsCreate() {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        
        CloneableTopComponent cmv = MultiViews.createCloneableMultiView("text/context", new LP(lookup));
        assertNotNull("MultiViewComponent created", cmv);
        cmv.open();
        TopComponent mvc = cmv.cloneTopComponent();
        doCheck(mvc, ic);
        
        assertTrue("First component can be closed without any questions", cmv.close());
        
        CntAction accept = new CntAction() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                super.actionPerformed( e );
                MVE.closeState = null;
            }

        };
        CntAction discard = new CntAction();
        CloseH.retValue = false;
        MVE.closeState = MultiViewFactory.createUnsafeCloseState("warn", accept, discard);
        DD.ret = 2;
        mvc.open();
        assertFalse("Closed cancelled", mvc.close());
        assertEquals("No accept", 0, accept.cnt);
        assertEquals("No discard", 0, discard.cnt);
        MVE.closeState = MultiViewFactory.createUnsafeCloseState("warn", accept, discard);
        DD.ret = 1;
        DD.d = null;
        mvc.open();
        assertTrue("Changes discarded, close accepted", mvc.close());
        assertEquals("Still no accept", 0, accept.cnt);
        assertEquals("One discard", 1, discard.cnt);
        MVE.closeState = MultiViewFactory.createUnsafeCloseState("warn", accept, discard);
        DD.ret = 0;
        DD.d = null;
        mvc.open();
        assertTrue("Closed accepted OK", mvc.close());
        assertEquals("Three buttons", 3, DD.d.getOptions().length);
        assertNull("Not called, we use default handler", CloseH.globalElements);
        MVE.closeState = null;
    }

    public void testCloneableMultiViewsSerialize() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        
        CloneableTopComponent cmv = MultiViews.createCloneableMultiView("text/context", new LP(lookup));
        assertPersistence("Always", TopComponent.PERSISTENCE_ALWAYS, cmv);
        assertNotNull("MultiViewComponent created", cmv);
        NbMarshalledObject mar = new NbMarshalledObject(cmv);
        TopComponent mvc = (TopComponent) mar.get();
        doCheck(mvc, ic);
    }

    private void assertPersistence(String msg, int pt, TopComponent cmv) {
        CharSequence log = Log.enable("org.netbeans.core.multiview", Level.WARNING);
        int res = cmv.getPersistenceType();
        if (log.length() > 0) {
            fail("There should be no warnings to compute getPersistenceType():\n" + log);
        }
        assertEquals(msg, pt, res);
    }
    
    private void doCheck(TopComponent mvc, InstanceContent ic) {
        assertNotNull("MultiViewComponent cloned", mvc);
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvc);
        assertNotNull("Handler found", handler);
        MultiViewPerspective[] arr = handler.getPerspectives();
        assertEquals("Two perspetives found", 2, arr.length);
        assertEquals("Contextual", arr[0].getDisplayName());
	assertEquals("Contextual", arr[1].getDisplayName());
	MultiViewDescription description = Accessor.DEFAULT.extractDescription(arr[0]);
	assertTrue(description instanceof ContextAwareDescription);
        assertFalse("First one is not for split", ((ContextAwareDescription)description).isSplitDescription());
	description = Accessor.DEFAULT.extractDescription(arr[1]);
	assertTrue(description instanceof ContextAwareDescription);
        assertTrue("Second one is for split", ((ContextAwareDescription)description).isSplitDescription());

        assertPersistence("Always", TopComponent.PERSISTENCE_ALWAYS, mvc);
        
        mvc.open();
        mvc.requestActive();
        mvc.requestVisible();
        
        handler.requestActive(arr[0]);
        assertNull("No integer now", mvc.getLookup().lookup(Integer.class));
        ic.add(1);
        assertEquals("1 now", Integer.valueOf(1), mvc.getLookup().lookup(Integer.class));

	((MultiViewCloneableTopComponent)mvc).splitComponent(JSplitPane.HORIZONTAL_SPLIT, -1);
	handler.requestActive(arr[0]);
	ic.remove(1);
        assertNull("No integer now", mvc.getLookup().lookup(Integer.class));
        ic.add(2);
        assertEquals("2 now", Integer.valueOf(2), mvc.getLookup().lookup(Integer.class));
    }
    
    
    public void testLookupInitializedForCloneable() {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        ic.add(10);

        CloneableTopComponent cmv = MultiViews.createCloneableMultiView("text/context", new LP(lookup));
        assertEquals("10 now", Integer.valueOf(10), cmv.getLookup().lookup(Integer.class));
        
        assertNotNull("MultiViewComponent created", cmv);
        TopComponent mvc = cmv.cloneTopComponent();
        
        assertNotNull("MultiViewComponent cloned", mvc);
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvc);
        assertNotNull("Handler found", handler);
        
        assertEquals("10 now", Integer.valueOf(10), mvc.getLookup().lookup(Integer.class));
        ic.remove(10);
        ic.add(1);
        assertEquals("1 now", Integer.valueOf(1), mvc.getLookup().lookup(Integer.class));
    }
    
    public void testLookupInitialized() {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        ic.add(10);

        TopComponent mvc = MultiViews.createMultiView("text/context", new LP(lookup));
        assertEquals("10 now", Integer.valueOf(10), mvc.getLookup().lookup(Integer.class));
        ic.remove(10);
        ic.add(1);
        assertEquals("1 now", Integer.valueOf(1), mvc.getLookup().lookup(Integer.class));
    }
    
    
    public void testNotSourceView() {
        int cnt = 0;
        for (MultiViewDescription d : MimeLookup.getLookup("text/context").lookupAll(MultiViewDescription.class)) {
            cnt++;
            assertFalse(
                "No view in text/context has source element",
                MultiViewCloneableTopComponent.isSourceView(d)
            );
        }
        if (cnt == 0) {
            fail("There shall be at least one description");
        }
    }
    
    public void testCompileInApt() throws Exception {
        clearWorkDir();
        String src = "\n"
                + "import org.netbeans.core.spi.multiview.MultiViewElement;\n"
                + "public class Test extends org.netbeans.core.multiview.MultiViewProcessorTest.MVE {\n"
        + "@MultiViewElement.Registration(displayName = \"Testing\","
        + "mimeType = \"text/ble\","
        + "persistenceType = 0,"
        + "preferredID = \"bleple\")"
                + "  public static MultiViewElement create() {\n"
                + "    return new Test();\n"
                + "  }\n"
                + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "pkg.Test", src);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation should succeed:\n" + os.toString(), res);
    }
    
    public void testCompileInAptFullPath() throws Exception {
        clearWorkDir();
        String src = "\n"
                + "import org.netbeans.core.spi.multiview.MultiViewElement;\n"
                + "public class Test extends org.netbeans.core.multiview.MultiViewProcessorTest.MVE {\n"
        + "@MultiViewElement.Registration(displayName = \"Testing\","
        + "iconBase = \"pkg/one.png\","
        + "mimeType = \"text/ble\","
        + "persistenceType = 0,"
        + "preferredID = \"bleple\")"
                + "  public static MultiViewElement create() {\n"
                + "    return new Test();\n"
                + "  }\n"
                + "}\n";
        generateIcon("one.png");
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "pkg.Test", src);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation should succeed:\n" + os.toString(), res);
    }

    private void generateIcon(String icon) throws IOException {
        File pkg = new File(getWorkDir(), "pkg");
        pkg.mkdirs();
        File f = new File(pkg, icon);
        f.createNewFile();
    }

    public void testFailsWithoutAnIcon() throws Exception {
        clearWorkDir();
        String src = "\n"
                + "import org.netbeans.core.spi.multiview.MultiViewElement;\n"
                + "public class Test extends org.netbeans.core.multiview.MultiViewProcessorTest.MVE {\n"
        + "@MultiViewElement.Registration(displayName = \"Testing\","
        + "iconBase = \"pkg/none-existing.png\","
        + "mimeType = \"text/ble\","
        + "persistenceType = 0,"
        + "preferredID = \"bleple\")"
                + "  public static MultiViewElement create() {\n"
                + "    return new Test();\n"
                + "  }\n"
                + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "pkg.Test", src);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation should fail:\n" + os.toString(), res);
        assertTrue("because of missing icon:\n" + os.toString(), os.toString().contains("iconBase"));
        assertTrue("because of missing icon:\n" + os.toString(), os.toString().contains("Cannot find resource pkg/none-existing.png"));
    }
    
    public void testIsSourceView() {
        int cnt = 0;
        for (MultiViewDescription d : MimeLookup.getLookup("text/plaintest").lookupAll(MultiViewDescription.class)) {
            cnt++;
            assertTrue(
                "All views in text/plaintest have source element: " + d,
                MultiViewCloneableTopComponent.isSourceView(d)
            );
        }
        if (cnt == 0) {
            fail("There shall be at least one description");
        }
    }
    
    public void testIconIsAlwaysTakenFromSourceView() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup lkp = new AbstractLookup(ic);
        ic.add(MultiViewEditorElementTest.createSupport(lkp));
        
        final CloneableTopComponent tc = MultiViews.createCloneableMultiView("text/plaintest", new LP(lkp));
        final CloneableEditorSupport.Pane p = (CloneableEditorSupport.Pane) tc;
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                p.updateName();
            }
        });

        assertNull("No icon yet", tc.getIcon());
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        final MultiViewPerspective[] two = handler.getPerspectives();
        assertEquals("Two elements only" + Arrays.asList(two), 2, handler.getPerspectives().length);
        assertEquals("First one is source", "source", two[0].preferredID());
	MultiViewDescription description = Accessor.DEFAULT.extractDescription(two[0]);
	assertTrue(description instanceof ContextAwareDescription);
        assertFalse("First one is not for split", ((ContextAwareDescription)description).isSplitDescription());
        assertEquals("Second one is source", "source", two[1].preferredID());
	description = Accessor.DEFAULT.extractDescription(two[1]);
	assertTrue(description instanceof ContextAwareDescription);
        assertTrue("Second one is for split", ((ContextAwareDescription)description).isSplitDescription());
        handler.requestVisible(two[0]);
        
        
        class P implements PropertyChangeListener {
            int cnt;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                cnt++;
            }
        }
        P listener = new P();
        tc.addPropertyChangeListener("icon", listener);
        
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_GRAY);
        ic.add(img);
        assertEquals("One change in listener", 1, listener.cnt);
        assertEquals("Image changed", img, tc.getIcon());
        
        ic.remove(img);
        assertEquals("Second change in listener", 2, listener.cnt);
        assertNull("No icon again", tc.getIcon());

	((MultiViewCloneableTopComponent)tc).splitComponent(JSplitPane.HORIZONTAL_SPLIT, -1);
	handler.requestVisible(two[1]);
        ic.add(img);
        assertEquals("Third change in listener", 3, listener.cnt);
        assertEquals("Image changed", img, tc.getIcon());

	ic.remove(img);
        assertEquals("Forth change in listener", 4, listener.cnt);
        assertNull("No icon again", tc.getIcon());
    }

    public void testMultiViewsContextCreate() {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        
        TopComponent mvc = MultiViews.createMultiView("text/context", new LP(lookup));
        assertNotNull("MultiViewComponent created", mvc);
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvc);
        assertNotNull("Handler found", handler);
        MultiViewPerspective[] arr = handler.getPerspectives();
        assertEquals("Two perspetives found", 2, arr.length);
        assertEquals("Contextual", arr[0].getDisplayName());
	assertEquals("Contextual", arr[1].getDisplayName());
	MultiViewDescription description = Accessor.DEFAULT.extractDescription(arr[0]);
	assertTrue(description instanceof ContextAwareDescription);
        assertFalse("First one is not for split", ((ContextAwareDescription)description).isSplitDescription());
	description = Accessor.DEFAULT.extractDescription(arr[1]);
	assertTrue(description instanceof ContextAwareDescription);
        assertTrue("Second one is for split", ((ContextAwareDescription)description).isSplitDescription());
        
        mvc.open();
        mvc.requestActive();
        mvc.requestVisible();
        
        handler.requestActive(arr[0]);
        assertNull("No integer now", mvc.getLookup().lookup(Integer.class));
        ic.add(1);
        assertEquals("1 now", Integer.valueOf(1), mvc.getLookup().lookup(Integer.class));
	
	((MultiViewTopComponent)mvc).splitComponent(JSplitPane.HORIZONTAL_SPLIT, -1);
	ic.remove(1);
	handler.requestActive(arr[1]);
        assertNull("No integer now", mvc.getLookup().lookup(Integer.class));
        ic.add(2);
        assertEquals("2 now", Integer.valueOf(2), mvc.getLookup().lookup(Integer.class));
    }

    @MimeRegistration(mimeType="text/figaro", service=CloseOperationHandler.class)
    public static class CloseH implements CloseOperationHandler {
        static CloseOperationState[] globalElements;
        static Boolean retValue;
        @Override
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            assertNull("globalElement not specified yet", globalElements);
            assertNotNull("We know what to return", retValue);
            boolean r = retValue;
            retValue = null;
            globalElements = elements;
            return r;
        }
    }

    @MultiViewElement.Registration(
        displayName="org.netbeans.core.multiview.TestBundle#FIGARO",
        mimeType="text/figaro",
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="figaro"
    )
    public static class MVE extends JPanel implements MultiViewElement {
        static CloseOperationState closeState;
        
        private JPanel toolbar = new JPanel();
        
        public MVE() {
        }
        
        @Override
        public JComponent getVisualRepresentation() {
            return this;
        }

        @Override
        public JComponent getToolbarRepresentation() {
            return toolbar;
        }

        @Override
        public Action[] getActions() {
            return new Action[0];
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public void componentOpened() {
        }

        @Override
        public void componentClosed() {
        }

        @Override
        public void componentShowing() {
        }

        @Override
        public void componentHidden() {
        }

        @Override
        public void componentActivated() {
        }

        @Override
        public void componentDeactivated() {
        }

        @Override
        public UndoRedo getUndoRedo() {
            return UndoRedo.NONE;
        }

        @Override
        public void setMultiViewCallback(MultiViewElementCallback callback) {
        }

        @Override
        public CloseOperationState canCloseElement() {
            if (closeState != null) {
                return closeState;
            }
            return CloseOperationState.STATE_OK;
        }
    } // end of MVE
    
    @MultiViewElement.Registration(
        displayName="Contextual",
        mimeType="text/context",
        persistenceType=TopComponent.PERSISTENCE_ALWAYS,
        preferredID="context"
    )
    public static CntxMVE create(Lookup lkp) {
        return new CntxMVE(lkp);
    }
    static class CntxMVE extends MVE {
        private Lookup context;
        public CntxMVE(Lookup context) {
            this.context = context;
        }
        public CntxMVE() {
        }

        @Override
        public Lookup getLookup() {
            return context;
        }
    } // end of CntxMVE

    @MultiViewElement.Registration(
        displayName="Source",
        mimeType="text/plaintest",
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="source"
    )
    public static class SourceMVC extends MultiViewEditorElement 
    implements LookupListener {
        private final Lookup.Result<Image> res;
        
        public SourceMVC(Lookup lookup) {
            super(lookup);
            res = lookup.lookupResult(Image.class);
            res.addLookupListener(this);
            resultChanged(null);
        }

        @Override
        public final void resultChanged(LookupEvent ev) {
            Collection<? extends Image> all = res.allInstances();
            if (all.isEmpty()) {
                getComponent().setIcon(null);
            } else {
                getComponent().setIcon(all.iterator().next());
            }
        }
        
    }
    
    public static class LP implements Lookup.Provider, Serializable {
        private static final Map<Integer,Lookup> map = new HashMap<Integer, Lookup>();
        
        private final int cnt;
        public LP(Lookup lkp) {
            synchronized (map) {
                cnt = map.size() + 1;
                map.put(cnt, lkp);
            }
        }
        
        @Override
        public Lookup getLookup() {
            return map.get(cnt);
        }
    }
    
    public static final class DD extends DialogDisplayer {
        static int ret;
        static NotifyDescriptor d;
        
        @Override
        public Object notify(NotifyDescriptor descriptor) {
            assertNull("No descriptor yet", d);
            if (ret == -1) {
                fail("We should know what to return");
            }
            d = descriptor;
            if (d.getOptions().length <= ret) {
                fail("not enough options. Need index " + ret + " but is just " + Arrays.toString(d.getOptions()));
            }
            Object obj = d.getOptions()[ret];
            ret = -1;
            return obj;
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    private static class CntAction extends AbstractAction {
        int cnt;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            cnt++;
        }
    }
}
