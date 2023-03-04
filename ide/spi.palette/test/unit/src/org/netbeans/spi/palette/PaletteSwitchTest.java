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

package org.netbeans.spi.palette;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.SplitConstraint;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
/**
 *
 * @author S. Aubrecht
 */
public class PaletteSwitchTest extends AbstractPaletteTestHid {
    
    private String lookupPaletteRootName;
    private FileObject lookupPaletteRootFolder;
    
    static String mimePaletteRootName;
    private static FileObject mimePaletteRootFolder;
    private static final String MIME_TYPE_NAME = "text/x-paletteswitchtest";
    
    private FileObject dummyDocumentFile;
    private static final String DUMMY_DOCUMENT_FILE_EXTENSION = "junitPaletteSwitchTest";
    
    static {
        String[] layers = new String[] {"org/netbeans/spi/palette/mf-layer.xml"};//NOI18N
        Object[] instances = new Object[] { new MyMimeResolver() };
        IDEInitializer.setup(layers,instances);
    }
    
    public PaletteSwitchTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        lookupPaletteRootName = "lookupPalette" + System.currentTimeMillis();
        lookupPaletteRootFolder = FileUtil.getConfigRoot().createFolder( lookupPaletteRootName );
        createDefaultPaletteContentInFolder( lookupPaletteRootFolder );
        
        if( null == mimePaletteRootName ) {
            mimePaletteRootName = "mimePalette" + System.currentTimeMillis();
            mimePaletteRootFolder = FileUtil.getConfigRoot().createFolder( mimePaletteRootName );
            createDefaultPaletteContentInFolder( mimePaletteRootFolder );
        }
        
        dummyDocumentFile = FileUtil.getConfigRoot().createData( "dummyDocumentFile" + System.currentTimeMillis(), DUMMY_DOCUMENT_FILE_EXTENSION );
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testNoLookupPalette() throws IOException {
        TopComponent tc = createTopComponentWithPalette( null );
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromTopComponent( tc, false, tc.isOpened() );
        
        assertNull( foundPalette );
    }
    
    public void testNoMimePalette() throws IOException {
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromMimeType( "text/_unknown" );
        
        assertNull( foundPalette );
    }
    
    public void testLookupPalette() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( lookupPaletteRootName, new DummyActions() );
        
        TopComponent tc = createTopComponentWithPalette( pc );
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromTopComponent( tc, false, tc.isOpened() );
        
        assertNotNull( foundPalette );
        assertEquals( pc.getModel().getName(), foundPalette.getModel().getName() );
    }
    
    public void testMimePalette() throws IOException {
        TopComponent tc = createTopComponentWithPalette( null );
        tc.setActivatedNodes( new Node[] { DataObject.find( dummyDocumentFile ).getNodeDelegate() } );
        tc.open();
        
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromMimeType( MIME_TYPE_NAME );
        assertNotNull( foundPalette );
        assertEquals( mimePaletteRootName, foundPalette.getModel().getName() );
        
        foundPalette = paletteSwitch.getPaletteFromTopComponent( tc, false, tc.isOpened() );
        assertNotNull( foundPalette );
        assertEquals( mimePaletteRootName, foundPalette.getModel().getName() );
    }
    
    
    public void testLookupPaletteTakePrecendsOverMimePalette() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( lookupPaletteRootName, new DummyActions() );
        
        TopComponent tc = createTopComponentWithPalette( pc );
        tc.setActivatedNodes( new Node[] { DataObject.find( dummyDocumentFile ).getNodeDelegate() } );
        
        PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        
        PaletteController foundPalette = paletteSwitch.getPaletteFromMimeType( MIME_TYPE_NAME );
        assertNotNull( foundPalette );
        assertEquals( mimePaletteRootName, foundPalette.getModel().getName() );
        
        foundPalette = paletteSwitch.getPaletteFromTopComponent( tc, false, tc.isOpened() );
        assertNotNull( foundPalette );
        assertEquals( pc.getModel().getName(), foundPalette.getModel().getName() );
    }
    
    public void testPaletteInNonActivatedEditor() throws IOException {
        final TopComponent palette = PaletteTopComponent.getDefault();
        PaletteController pc = PaletteFactory.createPalette( lookupPaletteRootName, new DummyActions() );
        
        final MyTopComponentWithSwitchablePalette tc = new MyTopComponentWithSwitchablePalette(pc);
        tc.setPaletteAvailable(true);
        initEditorTopComponent(tc);
        
        final TopComponent noPaletteTc = new TopComponent();
        initViewTopComponent(noPaletteTc);
        
        final PaletteSwitch paletteSwitch = PaletteSwitch.getDefault();
        paletteSwitch.startListening();
        
        tc.requestActive();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                assertEquals( TopComponent.getRegistry().getActivated(), tc );
                assertNotNull( "TC with PaletteController is active", 
                        paletteSwitch.getCurrentPalette() );
                assertTrue( "Palette window opens by default when a document with PaletteController is active", 
                        palette.isOpened() );
                
                noPaletteTc.requestActive();
                
                //switch to window with no palette while the palette editor is still showing
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        assertEquals( TopComponent.getRegistry().getActivated(), noPaletteTc );
                        assertNotNull( "TC without PaletteController is active", 
                                paletteSwitch.getCurrentPalette() );
                        assertTrue( "Palette window opens by default when a document with PaletteController is showing", 
                                palette.isOpened() );

                        tc.setPaletteAvailable(false);

                
                        //remove palette from the showing non-active editor
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                assertEquals( TopComponent.getRegistry().getActivated(), noPaletteTc );
                                assertNull( "Showing editor(s) has no palette", 
                                        paletteSwitch.getCurrentPalette() );
                                assertFalse( "Palette window is closed when no palette is available", 
                                        palette.isOpened() );

                                tc.setPaletteAvailable(true);


                                //add palette to the showing non-active editor
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        assertEquals( TopComponent.getRegistry().getActivated(), noPaletteTc );
                                        assertNotNull( "Showing editor(s) has a palette, PaletteSwitch must listen to changes in Lookup of showing editors", 
                                                paletteSwitch.getCurrentPalette() );
                                        assertTrue( palette.isOpened() );
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
    
    private MyTopComponent createTopComponentWithPalette( PaletteController pc ) throws IOException {
        MyTopComponent tc = new MyTopComponent( pc );
        initEditorTopComponent( tc );
        return tc;
    }
    
    private void initEditorTopComponent( TopComponent tc ) throws IOException {
        Mode editorMode = WindowManagerImpl.getInstance().findMode( "unitTestEditorMode" );
        if( null == editorMode ) {
            editorMode = WindowManagerImpl.getInstance().createMode( "unitTestEditorMode", Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        }
        editorMode.dockInto(tc);
    }
    
    private void initViewTopComponent( TopComponent tc ) throws IOException {
        Mode viewMode = WindowManagerImpl.getInstance().findMode( "unitTestViewMode" );
        if( null == viewMode ) {
            viewMode = WindowManagerImpl.getInstance().createMode( "unitTestViewMode", Constants.MODE_KIND_VIEW, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        }
        viewMode.dockInto(tc);
    }
    
    private static class MyTopComponent extends TopComponent {
        private boolean hackIsShowing = false;
        
        public MyTopComponent( PaletteController palette ) throws DataObjectNotFoundException {
            this( new InstanceContent(), palette );
        }
        
        MyTopComponent( InstanceContent ic, PaletteController palette ) throws DataObjectNotFoundException {
            super( new AbstractLookup( ic ) );
            if( null != palette )
                ic.add( palette );
        }
        
        @Override
        public boolean isShowing() {
            return hackIsShowing;
        }

        @Override
        protected void componentActivated() {
            super.componentActivated();
            hackIsShowing = true;
        }

        @Override
        protected void componentDeactivated() {
            super.componentDeactivated();
            hackIsShowing = false;
        }
    }
    
    private static class MyTopComponentWithSwitchablePalette extends MyTopComponent {
        private InstanceContent ic;
        private PaletteController pc;
        
        public MyTopComponentWithSwitchablePalette( PaletteController palette ) throws DataObjectNotFoundException {
            this( new InstanceContent(), palette );
        }
        
        private MyTopComponentWithSwitchablePalette( InstanceContent ic, PaletteController palette ) throws DataObjectNotFoundException {
            super( ic, palette );
            this.ic = ic;
            this.pc = palette;
            this.ic.remove(pc);
        }
        
        public void setPaletteAvailable( boolean paletteAvailable ) {
            if( paletteAvailable )
                ic.add( pc );
            else
                ic.remove( pc );
        }
    }
    
    public static class MyMimeResolver extends MIMEResolver {
        
        public String findMIMEType(FileObject fo) {
            if( DUMMY_DOCUMENT_FILE_EXTENSION.equals( fo.getExt() ) )
                return MIME_TYPE_NAME;
            return null;
        }
    }
    
    
}
