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
