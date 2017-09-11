/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.palette.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 *
 * @author S. Aubrecht
 */
public class TextImporter implements Runnable {

    private String text;
    private Lookup category;
    private int dropIndex;
    private FileObject categoryFolder;
    
    public TextImporter( String text, Lookup category, int dropIndex ) {
        this.text = text;
        this.category = category;
        this.dropIndex = dropIndex;
    }
    
    public void run( ) {
        //find category folder
        categoryFolder = findFolder( category );
        if( null == categoryFolder ) {
            NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(TextImporter.class, "Err_NoTextDnDSupport"), //NOI18N
                    null, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE, null, null );
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        
        //ask user to provide name and tooltip for the new item
        JButton btnOk = new JButton( NbBundle.getMessage(TextImporter.class, "Btn_AddToPalette") );//NOI18N
        btnOk.getAccessibleContext().setAccessibleName(btnOk.getText());
        btnOk.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TextImporter.class, "ACD_Btn_AddToPalette") );//NOI18N
        JButton btnCancel = new JButton( NbBundle.getMessage(TextImporter.class, "Btn_Cancel") );//NOI18N
        btnCancel.getAccessibleContext().setAccessibleName(btnCancel.getText());
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TextImporter.class, "ACD_Btn_Cancel") );//NOI18N
        final TextImporterUI panel = new TextImporterUI( text, btnOk );
        DialogDescriptor dd = new DialogDescriptor(panel, 
                NbBundle.getMessage(TextImporter.class, "Btn_AddToPalette"), true, //NOI18N
                new Object[] { btnOk, btnCancel}, 
                new HelpCtx( TextImporter.class ), 
                DialogDescriptor.DEFAULT_ALIGN, null, null );
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        btnCancel.setDefaultCapable(false);
        btnCancel.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
            }
        });
        btnOk.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dlg.dispose();
                doAddToPalette(panel);
            }
        });
        dlg.setVisible(true);
    }
    
    private void doAddToPalette( final TextImporterUI panel ) {
        //store new item
        final String fileName = FileUtil.findFreeFileName( categoryFolder, "ccc", "xml" ); //NOI18N //NOI18N
        try {
            categoryFolder.getFileSystem().runAtomicAction( new AtomicAction() {
                public void run() throws IOException {
                    FileObject itemFile = categoryFolder.createData( fileName, "xml" ); //NOI18N
                    PrintWriter w = new PrintWriter( itemFile.getOutputStream() );
                    storeItem(w, panel );
                    w.close();
                }
            });
        } catch( IOException ioE ) {
            Logger.getLogger( TextImporter.class.getName() ).log( Level.SEVERE, 
                    NbBundle.getMessage(TextImporter.class, "Err_StoreItemToDisk"), ioE );// NOI18N
            return;
        }
        
        //reorder
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                try {
                    reorder(fileName, categoryFolder, dropIndex);
                } catch( IOException ioE ) {
                    Logger.getLogger( TextImporter.class.getName() ).log( Level.INFO, null, ioE );
                }
            }
        });
    }
    
    private FileObject findFolder( Lookup category ) {
        Node n = category.lookup( Node.class );
        if( null != n ) {
            DataFolder df = n.getCookie( DataFolder.class );
            if( null != df ) {
                return df.getPrimaryFile();
            }
        }
        return null;
    }
    
    private void storeItem( PrintWriter w, TextImporterUI panel ) throws IOException {
        String name = panel.getItemName();
        String tooltip = panel.getItemTooltip();
        if( null == tooltip || tooltip.trim().length() == 0 )
            tooltip = name;
        String content = panel.getItemContent();
        String smallIconPath = panel.getItemSmallIconPath();
        ClassLoader cl = Lookup.getDefault().lookup( ClassLoader.class );
        if( null == smallIconPath )
            smallIconPath = "org/netbeans/modules/palette/resources/unknown16.gif"; //NOI18N
        String largeIconPath = panel.getItemLargeIconPath();
        if( null == largeIconPath )
            largeIconPath = "org/netbeans/modules/palette/resources/unknown32.gif"; //NOI18N
        
        w.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ); //NOI18N
        w.println( "<!DOCTYPE editor_palette_item PUBLIC \"-//NetBeans//Editor Palette Item 1.1//EN\" \"http://www.netbeans.org/dtds/editor-palette-item-1_1.dtd\">" ); //NOI18N
        w.println();
        w.println( "<editor_palette_item version=\"1.0\">" ); //NOI18N
        w.println( "    <body>" ); //NOI18N
        w.println( "        <![CDATA[" ); //NOI18N
        w.println( toUTF8(content) );
        w.println( "        ]]>" ); //NOI18N
        w.println( "    </body>" ); //NOI18N

        w.print(   "    <icon16 urlvalue=\"" ); w.print(smallIconPath); w.println( "\" />" );
        w.print(   "    <icon32 urlvalue=\"" ); w.print(largeIconPath); w.println( "\" />" );
        w.println( "    <inline-description>" );
        w.print(   "        <display-name>" ); w.print( XMLUtil.toElementContent( toUTF8(name) ) ); w.println( "</display-name>" );
        w.print(   "        <tooltip>" ); w.print( XMLUtil.toElementContent( toUTF8(tooltip) ) ); w.println( "</tooltip>" );
        w.println( "    </inline-description>" );
        w.println( "</editor_palette_item>" );
    }
    
    private String toUTF8( String s ) {
        try {
            return new String( s.getBytes("UTF-8") ); //NOI18N
        } catch( UnsupportedEncodingException e ) {
            Logger.getLogger(TextImporter.class.getName()).log(Level.WARNING, null, e);
            return s;
        }
    }
    
    private void reorder( String fileName, FileObject categoryFolder, int dropIndex ) throws IOException {
        if( dropIndex < 0 )
            return;
        FileObject itemFile = categoryFolder.getFileObject(fileName, "xml" ); //NOI18N
        if( null == itemFile )
            return;
        DataFolder catDob = DataFolder.findFolder(categoryFolder);
        DataObject[] children = catDob.getChildren();
        
        DataObject dob = DataObject.find(itemFile);
        if( null == dob )
            return;
        
        int curIndex = -1;
        for( int i=0; i<children.length; i++ ) {
            if( children[i].equals(dob) ) {
                curIndex = i;
                break;
            } 
        }
        if( curIndex < 0 )
            return;
        
        DataObject[] sortedChildren = new DataObject[children.length];
        if( dropIndex >= sortedChildren.length )
            dropIndex = sortedChildren.length-1;
        sortedChildren[dropIndex] = dob;
        int index = 0;
        for( int i=0; i<sortedChildren.length; i++ ) {
            if( sortedChildren[i] != null ) {
                continue;
            }
            DataObject tmp = children[index++];
            if( dob.equals(tmp) ) {
                i--;
                continue;
            }
            sortedChildren[i] = tmp;
        }
        
        catDob.setOrder(sortedChildren);
    }
}
