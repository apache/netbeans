/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remotefs.versioning.spi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import javax.swing.JFileChooser;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public interface RemoteVcsSupportImplementation {

    /**
     * @param proxy defines FS and initial selection
     * @return file chooser
     */
    JFileChooser createFileChooser(VCSFileProxy proxy);

    VCSFileProxy getSelectedFile(JFileChooser chooser);

    FileSystem getFileSystem(VCSFileProxy proxy);

    FileSystem[] getFileSystems();

    FileSystem[] getConnectedFileSystems();

    FileSystem getDefaultFileSystem();

    boolean isSymlink(VCSFileProxy proxy);

    String readSymbolicLinkPath(VCSFileProxy file) throws IOException;
    
    boolean canRead(VCSFileProxy proxy);

    public boolean canRead(VCSFileProxy base, String subdir);

    public String getCanonicalPath(VCSFileProxy proxy) throws IOException;

    public VCSFileProxy getCanonicalFile(VCSFileProxy proxy) throws IOException;

    public VCSFileProxy getHome(VCSFileProxy proxy);

    public boolean isMac(VCSFileProxy proxy);

    public boolean isSolaris(VCSFileProxy proxy);

    public boolean isUnix(VCSFileProxy proxy);

    public long getSize(VCSFileProxy proxy);

    public String getFileSystemKey(FileSystem proxy);

    public boolean isConnectedFileSystem(FileSystem file);

    public void connectFileSystem(FileSystem file);

    public String toString(VCSFileProxy proxy);

    public VCSFileProxy fromString(String proxy);

    public OutputStream getOutputStream(VCSFileProxy proxy) throws IOException;

    public void delete(VCSFileProxy file);

    public void deleteOnExit(VCSFileProxy file);
    
    public void deleteExternally(VCSFileProxy file);

    public void setLastModified(VCSFileProxy file, VCSFileProxy referenceFile);

    public FileSystem readFileSystem(DataInputStream is) throws IOException ;

    public void writeFileSystem(DataOutputStream os, FileSystem fs) throws IOException ;

    public void refreshFor(FileSystem fs, String... paths) throws ConnectException, IOException;

    /** NB: never adds a trailing slash (for compatibility and performance reasons) */
    public URI toURI(VCSFileProxy file);

    /** NB: never adds a trailing slash (for compatibility and performance reasons) */
    public URL toURL(VCSFileProxy file);
}
