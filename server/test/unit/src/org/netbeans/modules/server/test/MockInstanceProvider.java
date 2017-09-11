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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.server.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class MockInstanceProvider implements ServerInstanceProvider {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final List<ServerInstance> instances = new ArrayList<ServerInstance>();

    public MockInstanceProvider() {
        super();
    }

    public static void registerInstanceProvider(String instanceName, ServerInstanceProvider provider) throws IOException {
        if (provider == null) {
            return;
        }

        Lookup.getDefault().lookup(ModuleInfo.class);

        FileObject servers = FileUtil.getConfigFile(ServerRegistry.SERVERS_PATH);
        FileObject testProvider = FileUtil.createData(servers, instanceName);

        testProvider.setAttribute("instanceOf", ServerInstanceProvider.class.getName()); // NOI18N
        testProvider.setAttribute("instanceCreate", provider); // NOI18N
    }

    public void addInstance(ServerInstance instance) {
        if (instance == null) {
            return;
        }

        synchronized (this) {
            instances.add(instance);
        }
        changeSupport.fireChange();
    }

    public void removeInstance(ServerInstance instance) {
        if (instance == null) {
            return;
        }

        synchronized (this) {
            instances.remove(instance);
        }
        changeSupport.fireChange();
    }

    public void clear() {
        synchronized (this) {
            instances.clear();
        }
        changeSupport.fireChange();
    }

    public List<ServerInstance> getInstances() {
        synchronized (this) {
            return new ArrayList<ServerInstance>(instances);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

}
