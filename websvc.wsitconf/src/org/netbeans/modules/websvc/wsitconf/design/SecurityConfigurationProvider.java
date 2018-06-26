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

package org.netbeans.modules.websvc.wsitconf.design;


import java.util.Hashtable;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author rico
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider.class)
public class SecurityConfigurationProvider implements WSConfigurationProvider {
    
    private Hashtable<String, WSConfiguration> configProviders = new Hashtable<String, WSConfiguration>();
    
    /** Creates a new instance of SecurityConfigurationProvider */
    public SecurityConfigurationProvider() {
    }

    public synchronized WSConfiguration getWSConfiguration(Service service, FileObject implementationFile) {
        String key = "";
        if ((implementationFile == null) || (service == null)) {
            return null;
        }
        key += service.getLocalWsdlFile();
        key += implementationFile.getPath();
        if (!configProviders.containsKey(key)) {
            configProviders.put(key, new SecurityConfiguration(service, implementationFile));
        }
        implementationFile.addFileChangeListener(new FCListener());
        return configProviders.get(key);
    }
    
    private class FCListener implements FileChangeListener {

        public void fileDeleted(FileEvent fe) {
            if (fe == null) return;
            FileObject f = fe.getFile();
            if (f == null) return;
            
            for (String s : configProviders.keySet()) {
                if (s.contains(f.getPath())) {
                    configProviders.remove(s);
                    break;
                }
            }
        }

        public void fileRenamed(FileRenameEvent fe) {}
        public void fileFolderCreated(FileEvent fe) {}
        public void fileDataCreated(FileEvent fe) {}
        public void fileChanged(FileEvent fe) {}
        public void fileAttributeChanged(FileAttributeEvent fe) {}
    }
    
}
