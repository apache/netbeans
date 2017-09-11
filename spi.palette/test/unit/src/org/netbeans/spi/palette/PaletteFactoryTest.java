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
