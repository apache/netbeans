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

package org.netbeans.modules.cnd.remote.mapper;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.setup.MirrorPathProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.remote.spi.FileSystemCacheProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.util.Exceptions;

/**
 * MirrorPathProvider implementation
 */
@org.openide.util.lookup.ServiceProvider(service=MirrorPathProvider.class, position=100)
public class RemoteMirrorPathProvider implements MirrorPathProvider {
    private static final String POSTFIX = System.getProperty("cnd.remote.sync.root.postfix"); //NOI18N

    /** Service provider contract */
    public RemoteMirrorPathProvider() {
    }

    @Override
    public String getLocalMirror(ExecutionEnvironment executionEnvironment) {
        return FileSystemCacheProvider.getCacheRoot(executionEnvironment);
    }

    @Override
    public String getRemoteMirror(ExecutionEnvironment executionEnvironment) throws ConnectException, IOException, ConnectionManager.CancellationException {
        String root;
        root = System.getProperty("cnd.remote.sync.root." + RemoteUtil.hostNameToRemoteFileName(executionEnvironment.getHost())); //NOI18N
        if (root != null) {
            return root;
        }
        root = System.getProperty("cnd.remote.sync.root"); //NOI18N
        if (root != null) {
            return root;
        }
        
        if (!HostInfoUtils.isHostInfoAvailable(executionEnvironment)) {
            return null;
        }
        String home = HostInfoUtils.getHostInfo(executionEnvironment).getUserDir();
        
        final ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();
        MacroExpander expander = MacroExpanderFactory.getExpander(local);
        String localHostID = local.getHost();
        try {
            localHostID = expander.expandPredefinedMacros("${hostname}-${osname}-${platform}${_isa}"); // NOI18N
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        localHostID = RemoteUtil.hostNameToLocalFileName(localHostID);
        // each local host maps into own remote folder to prevent collisions on path mapping level
        // remote hosts should be separated as well since they can share home directory
        String result = home + "/.netbeans/remote/" + RemoteUtil.hostNameToRemoteFileName(executionEnvironment.getHost()) + "/" + localHostID; //NOI18N
        if (POSTFIX != null) {
            result += '-' + POSTFIX;
        }
        String canonical = FileSystemProvider.getCanonicalPath(executionEnvironment, result);
        return (canonical == null) ? result : canonical;
    }
}
