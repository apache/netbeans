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
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.eecommon.api.config;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Peter Williams
 */
public class FolderListener implements FileChangeListener {

    public static FileChangeListener createListener(File key, FileObject folder, J2eeModule.Type type) {
        return new FolderListener(key, folder, type);
    }

    private final File configKey;
    private final String [] targets;
    
    private FolderListener(File key, FileObject folder, J2eeModule.Type type) {
        configKey = key;
        if(type == J2eeModule.Type.WAR) {
            targets = new String [] { "web.xml", "webservices.xml" };
        } else if(type == J2eeModule.Type.EJB) {
            targets = new String [] { "ejb-jar.xml", "webservices.xml" };
        } else if(type == J2eeModule.Type.EAR) {
            targets = new String [] { "application.xml" };
        } else if(type == J2eeModule.Type.CAR) {
            targets = new String [] { "application-client.xml" };
        } else {
            Logger.getLogger("glassfish-eecommon").log(Level.WARNING, "Unsupported module type: " + type);
            targets = new String [0];
        }
        
        folder.addFileChangeListener(this);
    }
    
    public void fileFolderCreated(FileEvent fe) {
    }

    public void fileDataCreated(FileEvent fe) {
        FileObject fo = fe.getFile();
        for(String target: targets) {
            if(target.equals(fo.getNameExt())) {
                GlassfishConfiguration config = GlassfishConfiguration.getConfiguration(configKey);
                if(config != null) {
                    config.addDescriptorListener(fo);
                }
            }
        }
    }
    
    public void fileChanged(FileEvent fe) {
    }

    public void fileDeleted(FileEvent fe) {
    }

    public void fileRenamed(FileRenameEvent fe) {
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

}
