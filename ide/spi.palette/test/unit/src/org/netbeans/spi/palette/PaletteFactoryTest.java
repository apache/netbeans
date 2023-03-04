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

import java.io.FileNotFoundException;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Stanislav Aubrecht
 */
public class PaletteFactoryTest extends TestCase {

    private FileObject paletteRootFolder;
    private static final String PALETTE_ROOT_FOLDER_NAME = "test_palette_folder";

    public PaletteFactoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        paletteRootFolder = FileUtil.getConfigFile( PALETTE_ROOT_FOLDER_NAME );
        if( null != paletteRootFolder )
            paletteRootFolder.delete();
        paletteRootFolder = FileUtil.getConfigRoot().createFolder( PALETTE_ROOT_FOLDER_NAME );
    }

    protected void tearDown() throws Exception {
        paletteRootFolder = FileUtil.getConfigFile( PALETTE_ROOT_FOLDER_NAME );
        if( null != paletteRootFolder )
            paletteRootFolder.delete();
    }

    /**
     * Test of createPalette method, of class org.netbeans.modules.palette.api.PaletteFactory.
     */
    public void testCreatePaletteNulls() throws Exception {
        try {
            PaletteFactory.createPalette( (String)null, new DummyActions() );
            fail( "Palette factory accepts null as folder name." );
        } catch( IllegalArgumentException e ) {
            //that's what we want
        } catch( Throwable t ) {
            fail( "Invalid exception thrown." );
        }
        
        try {
            PaletteFactory.createPalette( (Node)null, new DummyActions() );
            fail( "Palette factory accepts null as root Node." );
        } catch( IllegalArgumentException e ) {
            //that's what we want
        } catch( Throwable t ) {
            fail( "Invalid exception thrown." );
        }
        
        try {
            PaletteFactory.createPalette( DummyPalette.createPaletteRoot(), null );
            fail( "Palette factory accepts null for palette actions." );
        } catch( IllegalArgumentException e ) {
            //that's what we want
        } catch( Throwable t ) {
            fail( "Invalid exception thrown." );
        }
    }
    
    public void testCreatePaletteFolder() throws Exception {
        try {
            PaletteFactory.createPalette( "non_existent_folder", new DummyActions() );
            fail( "Palette factory accepts non-existent folders." );
        } catch( FileNotFoundException fnfE ) {
            //that's what we want
        } catch( Throwable e ) {
            fail( "Invalid exception thrown." );
        }
        
        PaletteActions actions = new DummyActions();
        PaletteController controller = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, actions );
        Node rootNode = (Node)controller.getRoot().lookup( Node.class );
        assertNotNull( rootNode );
        assertEquals( actions, controller.getRoot().lookup( PaletteActions.class ) );
    }
    
    public void testCreatePaletteNodes() throws Exception {
        PaletteActions actions = new DummyActions();
        Node rootNode = DummyPalette.createPaletteRoot();
        PaletteController controller = PaletteFactory.createPalette( rootNode, actions );
        assertEquals( rootNode.getName(), controller.getRoot().lookup( Node.class ).getName() );
        assertEquals( actions, controller.getRoot().lookup( PaletteActions.class ) );
    }
    
}
