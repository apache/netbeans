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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
    private final static String DUMMY_DOCUMENT_FILE_EXTENSION = "junitPaletteSwitchTest";
    
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
