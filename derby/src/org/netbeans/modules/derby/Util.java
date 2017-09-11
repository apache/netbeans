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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.derby;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.modules.derby.ui.DerbyPropertiesPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class Util {
    private static final String DERBY_CLIENT = "derbyclient.jar"; // NOI18N

    private Util() {
    }

    public static boolean hasInstallLocation() {
        return getCheckedLocation() != null;
    }
    
    public static boolean checkInstallLocation() {
        if (!hasInstallLocation()) {
            showInformation(NbBundle.getMessage(Util.class, "MSG_DerbyLocationIncorrect"));
            return false;
        }
        return true;
    }

    private static File getCheckedLocation() {
        File location = new File(DerbyOptions.getDefault().getLocation());
        if (location.isAbsolute() && location.isDirectory() && location.exists()) {
            return location;
        }
        return null;
    }

    public static File getDerbyFile(String relPath) {
        File location = getCheckedLocation();
        if (location != null) {
            return new File(location, relPath);
        }
        return null;
    }
    
    public static boolean ensureSystemHome() {
        if (DerbyOptions.getDefault().getSystemHome().length() <= 0) {
            return Mutex.EVENT.writeAccess(new Mutex.Action<Boolean>() {
                public Boolean run() {
                    return DerbyPropertiesPanel.showDerbyProperties();
                }
            });
        }
        return true;
    }

    public static void showInformation(final String msg){
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
    }

    public static boolean isDerbyInstallLocation(File location) {
        if (location == null || ! location.exists()) {
            return false;
        }
        File libDir = new File(location, "lib"); // NOI18N
        if (!libDir.exists()) {
            return false;
        }
        File[] libs = libDir.listFiles();
        if (libs == null || libs.length <= 0) {
            return false;
        }
        for (File lib : libs) {
            if (lib.getName().equals(DERBY_CLIENT)) {
                return true;
            }
        }
        return false;
    }

    public static void extractZip(File source, FileObject target) throws IOException {
        FileInputStream is = new FileInputStream(source);
        try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();

                // if directory, create
                if (ze.isDirectory()) {
                    FileUtil.createFolder(target, name);
                    continue;
                }

                // if file, copy
                FileObject fd = FileUtil.createData(target, name);
                FileLock lock = fd.lock();
                try {
                    OutputStream os = fd.getOutputStream(lock);
                    try {
                        FileUtil.copy(zis, os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } finally {
            is.close();
        }
    }

    /**
     * Check if candiate FileObject is a derby database.
     * 
     * @param candidate the value of candidate
     * @return true if candidate is a derby database
     */
    public static boolean isDerbyDatabase(FileObject candidate) {
        if (candidate.isFolder()) {
            FileObject sp = candidate.getFileObject("service.properties");
            return sp != null && FileUtil.toFile(sp) != null;
        } else {
            return false;
        }
    }
}
