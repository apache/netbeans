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
package org.netbeans.beaninfo.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;

import org.netbeans.junit.NbTestCase;


/**
 * Test for IZ#36742
 * @author ads
 *
 */
public class FileArrayEditorTest extends NbTestCase {
    
    public FileArrayEditorTest(String name) {
        super(name);
    }
    
    /*
     *  Set of tests for checking that normal functionality is not broken 
     */
    public void testSetFileSingleSelection(){
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled( false );
        setFile(chooser);
    }
    
    public void testSetFileMultiSelection(){
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled( true );
        setFile(chooser);
    }
    
    public void testSetFilesSingleSelection(){
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled( false );
        setFiles(chooser);
    }
    
    public void testSetFilesMultiSelection(){
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled( true );
        setFiles(chooser);
    }
    
    public void testSetCombinedSingleSelection(){
        JFileChooser chooser = new JFileChooser();
        FileArrayEditor editor = getArrayEditor();
        chooser.addPropertyChangeListener( editor );
        chooser.setMultiSelectionEnabled( false );
        chooser.setSelectedFile( new File("file"));
        chooser.setSelectedFiles( new File[]{ new File("other") , 
                new File("other1")});
        assert myListener.size() == 2 : "FileArrayEditor should fire" +
            " exactly two property change events, but fired "+
            myListener.size()+ " event(s)";
    }
    
    public void testSetCombinedMultiSelection(){
        JFileChooser chooser = new JFileChooser();
        FileArrayEditor editor = getArrayEditor();
        chooser.addPropertyChangeListener( editor );
        chooser.setMultiSelectionEnabled( true );
        chooser.setSelectedFile( new File("file"));
        chooser.setSelectedFiles( new File[]{ new File("other") , 
                new File("other1")});
        assert myListener.size() == 2 : "FileArrayEditor should fire" +
            " exactly two property change events, but fired "+
            myListener.size()+ " event(s)";
    }
    
    public void testSetCombinedOtherOrderSelection(){
        JFileChooser chooser = new JFileChooser();
        FileArrayEditor editor = getArrayEditor();
        chooser.addPropertyChangeListener( editor );
        chooser.setMultiSelectionEnabled( true );
        chooser.setSelectedFiles( new File[]{ new File("other") , 
                new File("other1")});
        chooser.setSelectedFile( new File("file"));
        assert myListener.size() == 2 : "FileArrayEditor should fire" +
            " exactly two property change events, but fired "+
            myListener.size()+ " event(s)";
    }
    
    /*
     * This test for fix IZ#36742
     */
    public void testDoubleSelection(){
        JFileChooser chooser = new JFileChooser();
        FileArrayEditor editor = getArrayEditor();
        chooser.addPropertyChangeListener( editor );
        chooser.setMultiSelectionEnabled( true );
        File file = new File("file");
        chooser.setSelectedFiles( new File[]{ file , 
                new File("other")});
        chooser.setSelectedFile( file );
        assert myListener.size() == 1 : "FileArrayEditor should fire" +
            " exactly one property change event, but fired "+
            myListener.size()+ " event(s)";
        
        chooser.setSelectedFile( new File("other1") );
        assert myListener.size() == 2 : "FileArrayEditor should fire" +
            " exactly two property change events, but fired "+
            myListener.size()+ " event(s)";
        
        chooser.setSelectedFiles( new File[]{ file , 
                new File("other")});
        assert myListener.size() == 3 : "FileArrayEditor should fire" +
            " exactly three property change events, but fired "+
            myListener.size()+ " event(s)";
    }

    private void setFile( JFileChooser chooser ){
        FileArrayEditor editor = getArrayEditor();
        chooser.addPropertyChangeListener( editor );
        chooser.setSelectedFile( new File("file"));
        assert myListener.size() == 1 : "FileArrayEditor should fire" +
        		" exactly one property change event, but fired "+
        		myListener.size()+ " event(s)";
        myListener.clear();
        chooser.setSelectedFile( new File("other"));
        assert myListener.size() == 1 : "FileArrayEditor should fire" +
                " exactly one property change event, but fired "+
                myListener.size()+ " event(s)";
    }
    
    private void setFiles( JFileChooser chooser ){
        FileArrayEditor editor = getArrayEditor();
        chooser.addPropertyChangeListener( editor );
        chooser.setSelectedFiles( new File[]{ new File("file") , new File("file1")});
        assert myListener.size() == 1 : "FileArrayEditor should fire" +
                " exactly one property change event, but fired "+
                myListener.size()+ " event(s)";
        myListener.clear();
        chooser.setSelectedFiles( new File[]{ new File("other") , new File("other1")});
        assert myListener.size() == 1 : "FileArrayEditor should fire" +
                " exactly one property change event, but fired "+
                myListener.size()+ " event(s)";
    }
    
    private FileArrayEditor getArrayEditor(){
        myListener = null;
        FileArrayEditor editor = new FileArrayEditor();
        myListener = new Listener();
        editor.addPropertyChangeListener( myListener );
        return editor;
    }
    
    class Listener implements PropertyChangeListener {

        /* (non-Javadoc)
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange( PropertyChangeEvent event ) {
            myList.add( event );
        }
        
        void clear(){
            myList.clear();
        }
        
        int size(){
            return myList.size();
        }
        
        private List<PropertyChangeEvent> myList 
            = new LinkedList<PropertyChangeEvent>();
    }
    
    private Listener myListener;
}
