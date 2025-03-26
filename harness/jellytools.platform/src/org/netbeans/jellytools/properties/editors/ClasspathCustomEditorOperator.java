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

package org.netbeans.jellytools.properties.editors;

import java.io.File;
/*
 * ClasspathCustomEditorOperator.java
 *
 * Created on 6/13/02 4:40 PM
 */

import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.ListModel;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling Classpath Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class ClasspathCustomEditorOperator extends NbDialogOperator {

    /** Creates new ClasspathCustomEditorOperator
     * @throws org.netbeans.jemmy.TimeoutExpiredException when NbDialog not found
     * @param title String title of custom editor */
    public ClasspathCustomEditorOperator(String title) {
        super(title);
    }

    /** Creates new ClasspathCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public ClasspathCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    private JButtonOperator _btAddDirectory;
    private JButtonOperator _btMoveDown;
    private JListOperator _lstClasspath;
    private JButtonOperator _btAddJARZIP;
    private JButtonOperator _btRemove;
    private JButtonOperator _btMoveUp;


    /** Tries to find Add Directory... JButton in this dialog.
     * @throws org.netbeans.jemmy.TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btAddDirectory() {
        if (_btAddDirectory==null) {
            _btAddDirectory = new JButtonOperator(this, Bundle.getStringTrimmed(
                                            "org.netbeans.core.execution.beaninfo.editors.Bundle",
                                            "CTL_AddDirectory"));
        }
        return _btAddDirectory;
    }

    /** Tries to find "Move Down" JButton in this dialog.
     * @throws org.netbeans.jemmy.TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btMoveDown() {
        if (_btMoveDown==null) {
            _btMoveDown = new JButtonOperator(this, Bundle.getStringTrimmed(
                                            "org.netbeans.core.execution.beaninfo.editors.Bundle",
                                            "CTL_MoveDown"));
        }
        return _btMoveDown;
    }

    /** Tries to find null JList in this dialog.
     * @throws org.netbeans.jemmy.TimeoutExpiredException when component not found
     * @return JListOperator
     */
    public JListOperator lstClasspath() {
        if (_lstClasspath==null) {
            _lstClasspath = new JListOperator(this);
        }
        return _lstClasspath;
    }

    /** Tries to find Add JAR/ZIP... JButton in this dialog.
     * @throws org.netbeans.jemmy.TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btAddJARZIP() {
        if (_btAddJARZIP==null) {
            _btAddJARZIP = new JButtonOperator(this, Bundle.getStringTrimmed(
                                            "org.netbeans.core.execution.beaninfo.editors.Bundle", 
                                            "CTL_AddJAR"));
        }
        return _btAddJARZIP;
    }

    /** Tries to find "Remove" JButton in this dialog.
     * @throws org.netbeans.jemmy.TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, Bundle.getStringTrimmed(
                                            "org.netbeans.core.execution.beaninfo.editors.Bundle", 
                                            "CTL_Remove"));
        }
        return _btRemove;
    }

    /** Tries to find "Move Up" JButton in this dialog.
     * @throws org.netbeans.jemmy.TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btMoveUp() {
        if (_btMoveUp==null) {
            _btMoveUp = new JButtonOperator(this, Bundle.getStringTrimmed(
                                            "org.netbeans.core.execution.beaninfo.editors.Bundle", 
                                            "CTL_MoveUp"));
        }
        return _btMoveUp;
    }

    /** clicks on Add Directory... JButton
     * @throws org.netbeans.jemmy.TimeoutExpiredException when JButton not found
     * @return FileCustomEditorOperator of directory selector */
    public FileCustomEditorOperator addDirectory() {
        btAddDirectory().pushNoBlock();
        return new FileCustomEditorOperator(Bundle.getString(
                                            "org.netbeans.core.execution.beaninfo.editors.Bundle", 
                                            "CTL_FileSystemPanel.Local_Dialog_Title"));
    }

    /** clicks on "Move Down" JButton
     * @throws org.netbeans.jemmy.TimeoutExpiredException when JButton not found
     */
    public void moveDown() {
        btMoveDown().push();
    }

    /** clicks on Add JAR/ZIP... JButton
     * @throws org.netbeans.jemmy.TimeoutExpiredException when JButton not found
     * @return FileCustomEditorOperator of JAR or ZIP file selector */
    public FileCustomEditorOperator addJARZIP() {
        btAddJARZIP().pushNoBlock();
        return new FileCustomEditorOperator(Bundle.getString(
                                            "org.netbeans.core.execution.beaninfo.editors.Bundle", 
                                            "CTL_FileSystemPanel.Jar_Dialog_Title"));
    }

    /** adds directory into classpath list
     * @param directoryPath String directory path to be added */    
    public void addDirectory(String directoryPath) {
        FileCustomEditorOperator editor=addDirectory();
        editor.setSelectedFile(directoryPath);
        editor.ok();
    }

    /** adds JAR or ZIP file into classpath list
     * @param filePath String path of JAR or ZIP file to be added */    
    public void addJARZIP(String filePath) {
        FileCustomEditorOperator editor=addJARZIP();
        editor.setFileValue(filePath);
        editor.ok();
    }

    /** adds directory into classpath list
     * @param directory File directory to be added */    
    public void addDirectory(File directory) {
        FileCustomEditorOperator editor=addDirectory();
        editor.setSelectedFile(directory);
        editor.ok();
    }

    /** adds JAR or ZIP file into classpath list
     * @param jarZip File JAR or ZIP to be added */    
    public void addJARZIP(File jarZip) {
        FileCustomEditorOperator editor=addJARZIP();
        editor.setFileValue(jarZip);
        editor.ok();
    }
    
    /** sets complete classpath in custom editor
     * @param classPathElements File[] array of directories or JAR or ZIP files 
     * to be included in classapth */    
    public void setClasspathValue(File[] classPathElements) {
        removeAll();
        for (int i=0; i<classPathElements.length; i++) {
            if (classPathElements[i].isFile())
                addJARZIP(classPathElements[i]);
            else
                addDirectory(classPathElements[i]);
        }
    }
    
    /** sets complete classpath in custom editor
     * @param classPathElements String[] array of paths of directories or JAR 
     * or ZIP files to be included in classapth */    
    public void setClasspathValue(String[] classPathElements) {
        removeAll();
        for (int i=0; i<classPathElements.length; i++) {
            String lower=classPathElements[i].toLowerCase();
            if (lower.endsWith(".jar")||lower.endsWith(".zip"))
                addJARZIP(classPathElements[i]);
            else
                addDirectory(classPathElements[i]);
        }
    }

    /** clicks on "Remove" JButton
     * @throws org.netbeans.jemmy.TimeoutExpiredException when JButton not found
     */
    public void remove() {
        btRemove().push();
    }

    /** clicks on "Move Up" JButton
     * @throws org.netbeans.jemmy.TimeoutExpiredException when JButton not found
     */
    public void moveUp() {
        btMoveUp().push();
    }
    
    /** removes given item from classpath
     * @param value String item to be removed */    
    public void remove(String value) {
        lstClasspath().selectItem(value);
        remove();
    }
    
    /** removes complete classpath */    
    public void removeAll() {
        while (lstClasspath().getModel().getSize()>0) {
            lstClasspath().selectItem(0);
            remove();
        }
    }
    
    /** returns complete class path from editor
     * @return String[] class paths */    
    public String[] getClasspathValue() {
        ArrayList<String> data=new ArrayList<String>();
        ListModel model=lstClasspath().getModel();
        for (int i=0; i<model.getSize(); i++) {
            data.add(model.getElementAt(i).toString());
        }
        return data.toArray(new String[0]);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btAddDirectory();
        btAddJARZIP();
        btMoveDown();
        btMoveUp();
        btRemove();
        lstClasspath();
    }
}

