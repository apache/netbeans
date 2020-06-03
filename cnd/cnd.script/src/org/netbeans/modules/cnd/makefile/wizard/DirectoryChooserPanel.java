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

package  org.netbeans.modules.cnd.makefile.wizard;

import java.io.File;
import javax.swing.JFileChooser;
import org.netbeans.modules.cnd.makefile.utils.IpeFileSystemView;

/**
 * The DirectoryChooserPanel extends the FileChooserPanel but makes the
 * JFileChooser a directory chooser rather than a file chooser.
 */

public abstract class DirectoryChooserPanel extends FileChooserPanel {

    /** Serial version number */
    static final long serialVersionUID = -8477214279063965753L;

    public DirectoryChooserPanel(MakefileWizard wd) {
	super(wd);
	init();
	fc = new JFileChooser();
	fc.setApproveButtonText(getString("BTN_Approve"));		// NOI18N
	fc.setDialogTitle(getString("TITLE_DirChooser"));		// NOI18N
	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	fc.setFileSystemView(new IpeFileSystemView(fc.getFileSystemView()));
	fc.setCurrentDirectory(
			new File(System.getProperty("user.dir")));	// NOI18N
    }
}
