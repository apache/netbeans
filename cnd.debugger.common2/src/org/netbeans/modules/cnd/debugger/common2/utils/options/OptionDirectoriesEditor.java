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

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

class OptionDirectoriesEditor extends PropertyEditorSupport
			    implements ExPropertyEditor {

    private OptionPropertySupport ops;
    private final String baseDir;
    private final FileSystem fileSystem;
    private PropertyEnv env;

    public OptionDirectoriesEditor(OptionPropertySupport ops,
				 String baseDir) {
        this(ops, baseDir, FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal()));        
    }

    public OptionDirectoriesEditor(OptionPropertySupport ops,
				 String baseDir, FileSystem fileSystem) {
	this.ops = ops;
	this.baseDir = baseDir;
	this.fileSystem = fileSystem;
    }

    // interface PropertyEditor
    @Override
    public void setAsText(String text) {
	ops.setValue(text); // from PropertyEditorSupport
    }

    // interface PropertyEditor
    @Override
    public String getAsText() {
        return (String) getValue();
    }

    // interface PropertyEditor
    @Override
    public void setValue(Object v) {
        if (v instanceof List) {
            boolean addSep = false;
            StringBuilder ret = new StringBuilder();
            for (Object row : ((List) v)) {
                if (addSep) {
                    ret.append(File.pathSeparator);
                }
                ret.append(row.toString());
                addSep = true;
            }
            ops.setValue(ret.toString());
        }
    }

    // interface PropertyEditor
    @Override
    public Object getValue() {
	return ops.getValue();
    }

    // interface PropertyEditor
    @Override
    public boolean supportsCustomEditor() {
	return true;
    }

    // interface PropertyEditor
    @Override
    public java.awt.Component getCustomEditor() {
        return new DirectoriesChooser(this, env, baseDir, (String) ops.getValue(), fileSystem);
    }

    // interface ExPropertyEditor
    @Override
    public void attachEnv(PropertyEnv env) {
	this.env = env;
    }
    
    static class DirectoriesChooser extends ListEditorPanel<String>
                                    implements PropertyChangeListener {

        private final PropertyEditorSupport editor;
        private final String baseDir;
        private final String path;
        private final FileSystem fileSystem;

        private DirectoriesChooser(PropertyEditorSupport editor, PropertyEnv env, 
                String baseDir, String path, FileSystem fileSystem){
            super(getPathsFromString(path));
            
            getDefaultButton().setVisible(false);
            
            this.editor = editor;
            this.baseDir = baseDir;
            this.path = path;
            this.fileSystem = fileSystem;
            
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
	    env.addPropertyChangeListener(this);
        }
        
        private static String fullPath(String baseDir, String path) {
	    String seed = path;
	    if (seed.length() == 0) {
		seed = ".";	// NOI18N
            }
	    if (!CndPathUtilities.isPathAbsolute(seed)) {
		seed = baseDir + File.separatorChar + seed;
            }
	    return seed;
	}
        
        private static List getPathsFromString(String inpStr){
            List<String> newList = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(inpStr, File.pathSeparator); // NOI18N
            while (st.hasMoreTokens()) {
                newList.add(st.nextToken());
            }
            return newList;
        }

        @Override
        public String addAction() {
            JFileChooser fileChooser;
            if (RemoteFileUtil.isRemote(fileSystem)) {
                fileChooser = RemoteFileChooserUtil.createFileChooser(fileSystem,
                        getString("ADD_DIRECTORY_DIALOG_TITLE"), // NOI18N
                        getString("ADD_DIRECTORY_BUTTON_TXT"), // NOI18N
                        FileChooser.DIRECTORIES_ONLY,
                        null,
                        fullPath(baseDir, path),
                        true);
            } else {
                // TODO: remove if and leave RemoteFileChooserUtil.createFileChooser
                // I'm just not sure there won't be side effects, 
                // so I leave FileChooser for local  
                fileChooser = new FileChooser(
                        getString("ADD_DIRECTORY_DIALOG_TITLE"),  // NOI18N
                        getString("ADD_DIRECTORY_BUTTON_TXT"),  // NOI18N
                        FileChooser.DIRECTORIES_ONLY,
                        null,
                        fullPath(baseDir, path),
                        true);                
            }

            int ret = fileChooser.showOpenDialog(this);
            if (ret == FileChooser.CANCEL_OPTION) {
                return null;
            }
            String itemPath = fileChooser.getSelectedFile().getPath();
            itemPath = CndPathUtilities.naturalizeSlashes(itemPath);
            String bd = baseDir;
            if (bd != null) {
                bd = CndPathUtilities.naturalizeSlashes(bd);
            }
            itemPath = CndPathUtilities.toRelativePath(bd, itemPath);
            itemPath = CndPathUtilities.normalizeSlashes(itemPath);
            return itemPath;
        }
        
        @Override
        public String copyAction(String o) {
            return o;
        }

        @Override
        public void editAction(String o, int i) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = 
                    new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"),  // NOI18N
                    getString("EDIT_DIALOG_TITLE_TXT"));    // NOI18N
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText().trim();
            replaceElement(o, newS, i);
        }
        
        @Override
        public String getListLabelText() {
            return getString("DIRECTORIES_LABEL_TXT");  // NOI18N
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("DIRECTORIES_LABEL_MN").charAt(0); // NOI18N
        }

        @Override
        public String getAddButtonText() {
            return getString("ADD_BUTTON_TXT"); // NOI18N
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("ADD_BUTTON_MN").charAt(0);    // NOI18N
        }

        @Override
        public String getRenameButtonText() {
            return getString("EDIT_BUTTON_TXT");    // NOI18N
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("EDIT_BUTTON_MN").charAt(0);   // NOI18N
        }

        @Override
        public String getDownButtonText() {
            return getString("DOWN_BUTTON_TXT");    // NOI18N
        }

        @Override
        public char getDownButtonMnemonics() {
            return getString("DOWN_BUTTON_MN").charAt(0);   // NOI18N
        }

        @Override
        protected String getUpButtonText() {
            return getString("UP_BUTTON_TXT");  // NOI18N
        }
        
        @Override
        public char getUpButtonMnemonics() {
            return getString("UP_BUTTON_MN").charAt(0); // NOI18N
        }
        
        //PropertyChangeListener interface
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
                editor.setValue(getListData());
            }
        }
        
        private static String getString(String key) {
            return NbBundle.getMessage(OptionDirectoriesEditor.class, key);
        }
    }
}
