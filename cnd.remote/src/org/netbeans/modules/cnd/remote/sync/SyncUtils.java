/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
//import javax.swing.DefaultComboBoxModel;
//import javax.swing.DefaultListCellRenderer;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JList;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
//import org.openide.DialogDescriptor;
//import org.openide.DialogDisplayer;
import org.openide.filesystems.FileSystem;

/**
 *
 */
final class SyncUtils {
    
    
    /*package*/ static boolean isDoubleRemote(ExecutionEnvironment buildEnvironment, FileSystem sourceFileSystem) {
        ExecutionEnvironment sourceExecutionEnvironment = FileSystemProvider.getExecutionEnvironment(sourceFileSystem);
        if (sourceExecutionEnvironment.isRemote()) {
            return ! buildEnvironment.equals(sourceExecutionEnvironment);
        }
        return false;
    }

  

    public static List<File> toFiles(List<FSPath> paths) {
        List<File> l = new ArrayList<>(paths.size());
        for (FSPath path : paths) {
            if (FileSystemProvider.getExecutionEnvironment(path.getFileSystem()).isLocal()) {
                l.add(new File(path.getPath()));
            }
        }
        return l;
    }
    
    public static File[] toFiles(FSPath[] paths) {
        List<File> l = new ArrayList<>(paths.length);
        for (FSPath path : paths) {
            if (FileSystemProvider.getExecutionEnvironment(path.getFileSystem()).isLocal()) {
                l.add(new File(path.getPath()));
            }
        }
        return l.toArray(new File[l.size()]);
    }

    public static FileSystem getSingleFileSystem(List<FSPath> paths) {
        FileSystem fs = null;
        for (FSPath fsp : paths) {
            if (fs == null) {
                fs = fsp.getFileSystem();
            } else {
                CndUtils.assertTrue(fs == fsp.getFileSystem(),
                        "Different file systems are unsupported: " + fs + ", " + fsp.getFileSystem() ); //NOI18N
                break;
            }
        }
        if (fs == null) {
            CndUtils.getLogger().warning("Defaulting file systems to a local one"); //NOI18N
            fs = FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal());
        }
        return fs;
    }

}
