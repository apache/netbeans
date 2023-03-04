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
package org.netbeans.modules.nativeexecution.ui;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;

/**
 *
 * @author ak119685
 */
public final class SSHKeyFileChooser extends JFileChooser {

    public SSHKeyFileChooser(String startPoint) {
        super(getClosestDirectory(startPoint));
        setMultiSelectionEnabled(false);
        setFileFilter(new SSHKeyFilter());
        setFileHidingEnabled(false);
        setDialogTitle(NbBundle.getMessage(SSHKeyFileChooser.class, "SSHKeyFileChooser.DialogTitle.text")); // NOI18N
    }

    private static File getClosestDirectory(String startPoint) {
        File candidate = new File(startPoint.trim());

        while (true) {
            if (candidate == null) {
                return new File(System.getProperty("user.home")); // NOI18N
            }

            if (candidate.isDirectory()) {
                return candidate;
            }

            candidate = candidate.getParentFile();
        }
    }

    private static class SSHKeyFilter extends FileFilter {

        @Override
        public boolean accept(File file) {
            return SSHKeyFileFilter.getInstance().accept(file);
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SSHKeyFileChooser.class, "SSHKeyFileChooser.SSHKeyFileType.text"); // NOI18N
        }
    }
}
