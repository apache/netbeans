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
     * @throws TimeoutExpiredException when NbDialog not found
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
     * @throws TimeoutExpiredException when component not found
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
     * @throws TimeoutExpiredException when component not found
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
     * @throws TimeoutExpiredException when component not found
     * @return JListOperator
     */
    public JListOperator lstClasspath() {
        if (_lstClasspath==null) {
            _lstClasspath = new JListOperator(this);
        }
        return _lstClasspath;
    }

    /** Tries to find Add JAR/ZIP... JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
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
     * @throws TimeoutExpiredException when component not found
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
     * @throws TimeoutExpiredException when component not found
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
     * @throws TimeoutExpiredException when JButton not found
     * @return FileCustomEditorOperator of directory selector */
    public FileCustomEditorOperator addDirectory() {
        btAddDirectory().pushNoBlock();
        return new FileCustomEditorOperator(Bundle.getString(
                                            "org.netbeans.core.execution.beaninfo.editors.Bundle", 
                                            "CTL_FileSystemPanel.Local_Dialog_Title"));
    }

    /** clicks on "Move Down" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void moveDown() {
        btMoveDown().push();
    }

    /** clicks on Add JAR/ZIP... JButton
     * @throws TimeoutExpiredException when JButton not found
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
     * @throws TimeoutExpiredException when JButton not found
     */
    public void remove() {
        btRemove().push();
    }

    /** clicks on "Move Up" JButton
     * @throws TimeoutExpiredException when JButton not found
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
        return (String[])data.toArray(new String[data.size()]);
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

