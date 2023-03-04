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
