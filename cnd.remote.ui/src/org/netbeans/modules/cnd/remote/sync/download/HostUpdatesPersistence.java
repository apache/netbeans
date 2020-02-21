/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.cnd.remote.sync.download;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

class HostUpdatesPersistence {

    private final Properties data;
    private final FileObject dataFile;

    // In version 2.0 value has format
    // (0|1)(p|v)  
    // where 1 means "on" and 0 means "off"
    // and p means "permanent" (i.e. user checked "remember my choice") 
    // and v means, in contrary, "volatile" 
    private static final String VERSION = "2.0"; // NOI18N

    private static final String VERSION_KEY = "____VERSION"; // NOI18N

    public HostUpdatesPersistence(FileObject privProjectStorageDir, ExecutionEnvironment executionEnvironment) throws IOException {
        super();
        data = new Properties();
        String dataFileName = "downloads-" + // NOI18N
                RemoteUtil.hostNameToLocalFileName(executionEnvironment.getHost()) + 
                '-' + RemoteUtil.hostNameToLocalFileName(executionEnvironment.getUser()) + 
                '-' + executionEnvironment.getSSHPort(); // NOI18N
        //NOI18N
        dataFile = FileUtil.createData(privProjectStorageDir, dataFileName);
        try {
            load();
            if (!VERSION.equals(data.get(VERSION_KEY))) {
                data.clear();
            }
        } catch (IOException ex) {
            data.clear();
            Exceptions.printStackTrace(ex);
        }
    }

    private void load() throws IOException {
        if (dataFile.isValid()) {
            InputStream is = dataFile.getInputStream();
            BufferedInputStream bs = new BufferedInputStream(is);
            try {
                data.load(bs);
            } finally {
                bs.close();
            }
        }
    }

    @SuppressWarnings(value = "RV")
    public void store() {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(dataFile.getOutputStream());
            data.setProperty(VERSION_KEY, VERSION);
            data.store(os, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            try {
                dataFile.delete();
            } catch (IOException ex2) {
                System.err.printf("Error deleting file %s%n", dataFile.getPath());
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public boolean getFileSelected(File file, boolean defaultValue) {
        final String key = file.getAbsolutePath();
        Object value = data.get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return (value instanceof String && ((String) value).startsWith("1")); // NOI18N
        }
    }

    public void setFileSelected(File file, boolean selected, boolean markAsPermanent) {
        final String key = file.getAbsolutePath();
        data.put(key, (selected ? "1" : "0") + (markAsPermanent ? 'p' : 'v')); // NOI18N
    }
    
    public boolean isAnswerPersistent(File file) {
        final String key = file.getAbsolutePath();
        Object value = data.get(key);
        return (value instanceof String && ((String) value).endsWith("p")); // NOI18N
    }
}
