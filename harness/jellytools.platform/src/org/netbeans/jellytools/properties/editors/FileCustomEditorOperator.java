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

package org.netbeans.jellytools.properties.editors;

/*
 * FileCustomEditorOperator.java
 *
 * Created on June 13, 2002, 4:01 PM
 */

import java.io.File;
import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling File Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class FileCustomEditorOperator extends NbDialogOperator {

    private JFileChooserOperator _fileChooser=null;
    
    /** Creates a new instance of FileCustomEditorOperator
     * @param title String title of custom editor */
    public FileCustomEditorOperator(String title) {
        super(title);
    }
    
    /** Creates a new instance of FileCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public FileCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    /** getter for JFileChooserOperator
     * @return JFileChooserOperator */    
    public JFileChooserOperator fileChooser() {
        if (_fileChooser==null) {
            _fileChooser=new JFileChooserOperator(this);
        }
        return _fileChooser;
    }
    
    /** returns edited file
     * @return File */    
    public File getFileValue() {
        return fileChooser().getSelectedFile();
    }
    
    /** sets edited file
     * @param file File */    
    public void setFileValue(File file) {
        // Need to go from parent to file because events are not fired when
        // only setSelectedFile(file) is used.
        // select parent directory
        fileChooser().setSelectedFile(file.getParentFile());
        // go into dir
        fileChooser().enterSubDir(file.getParentFile().getName());
        // wait file is displayed
        fileChooser().waitFileDisplayed(file.getName());
        // select file
        fileChooser().selectFile(file.getName());
    }
    
    /** sets edited file
     * @param fileName String file name */    
    public void setFileValue(String fileName) {
        setFileValue(new File(fileName));
    }
    
    /** Sets selected file and approve it.
     * @param file file to be selected.
     */
    public void setSelectedFile(File file) {
        fileChooser().setSelectedFile(file);
    }
    
    /** Sets selected file and approve it.
     * @param fileName file name to be selected.
     */
    public void setSelectedFile(String fileName) {
        setSelectedFile(new File(fileName));
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        fileChooser();
    }
}
