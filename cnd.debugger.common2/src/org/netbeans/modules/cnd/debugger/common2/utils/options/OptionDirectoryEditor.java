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

import java.awt.BorderLayout;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.filesystems.FileSystem;

class OptionDirectoryEditor extends PropertyEditorSupport
			    implements ExPropertyEditor {

    private OptionPropertySupport ops;
    private final String baseDir;
    private final FileSystem fileSystem;
    private PropertyEnv env;
    private int dirOrFile;

    public OptionDirectoryEditor(OptionPropertySupport ops,
				 String baseDir, int dirOrFile) {
        this(ops, baseDir, dirOrFile, 
                FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal()));
    }

    public OptionDirectoryEditor(OptionPropertySupport ops,
				 String baseDir, int dirOrFile, FileSystem fileSystem) {
	this.ops = ops;
	this.baseDir = baseDir;
	this.dirOrFile = dirOrFile;
	this.fileSystem = fileSystem;
    }

    // interface PropertyEditor
    @Override
    public void setAsText(String text) {
	setValue(text); // from PropertyEditorSupport
    }

    // interface PropertyEditor
    @Override
    public String getAsText() {
	return (String) getValue();
    }

    // interface PropertyEditor
    @Override
    public void setValue(Object v) {
	ops.setValue(v);
    }

    // interface PropertyEditor
    @Override
    public Object getValue() {
	Object o = ops.getValue();
	return o;
    }

    // interface PropertyEditor
    @Override
    public boolean supportsCustomEditor() {
	return true;
    }

    // interface PropertyEditor
    @Override
    public java.awt.Component getCustomEditor() {
	return new DirectoryChooser(this, env, dirOrFile, baseDir, 
                (String) ops.getValue(), fileSystem);
    }

    // interface ExPropertyEditor
    @Override
    public void attachEnv(PropertyEnv env) {
	this.env = env;
    }

    private static class DirectoryChooser extends JPanel
				  implements PropertyChangeListener {

	private final PropertyEditorSupport editor;
	private final String baseDir;
	private final JFileChooser fileChooser;


	private static String fullPath(String baseDir, String path) {
            if (baseDir == null) {
                return CndPathUtilities.isPathAbsolute(path) ? path : null;
            } else {
                String seed = path;
                if (seed.length() == 0) {
                    seed = ".";	// NOI18N
                }
                if (!CndPathUtilities.isPathAbsolute(seed)) {
                    seed = baseDir + File.separatorChar + seed;
                }
                return seed;
            }
	}

	public DirectoryChooser(PropertyEditorSupport editor,
				PropertyEnv env, int dirOrFile,
				String baseDir, String path,
                                FileSystem fileSystem) {
            if (RemoteFileUtil.isRemote(fileSystem)) {
                fileChooser = RemoteFileChooserUtil.createFileChooser(fileSystem,
                        "Experiment Directory", // NOI18N
                        "Select", // NOI18N
                        dirOrFile,
                        // FileChooser.DIRECTORIES_ONLY,
                        null,
                        fullPath(baseDir, path),
                        true);
            } else {
                // TODO: remove if and leave RemoteFileUtil.createFileChooser
                // I'm just not sure there won't be side effects, 
                // so I leave FileChooser for local  
                fileChooser = new FileChooser("Experiment Directory", // NOI18N
		  "Select", // NOI18N
		  dirOrFile, 
		  // FileChooser.DIRECTORIES_ONLY,
		  null,
		  fullPath(baseDir, path),
		  true);
            }
            
	    this.editor = editor;
	    this.baseDir = baseDir;

	    fileChooser.setControlButtonsAreShown(false);
            setLayout(new BorderLayout());
            add(fileChooser, BorderLayout.CENTER);
            
	    env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
	    env.addPropertyChangeListener(this);
	}

        @Override
	public void propertyChange(PropertyChangeEvent evt) {
	    if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) &&
		evt.getNewValue() == PropertyEnv.STATE_VALID) {

		File file = fileChooser.getSelectedFile();
		if (file != null) {
		    String path = file.getAbsolutePath();
		    editor.setValue(path);
		}
	    }
	}
    }
}
