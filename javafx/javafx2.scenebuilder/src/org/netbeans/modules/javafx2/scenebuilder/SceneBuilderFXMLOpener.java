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
package org.netbeans.modules.javafx2.scenebuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javafx2.editor.spi.FXMLOpener;
import org.openide.loaders.DataObject;

import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Opens an FXML file in SceneBuilder instance if available.
 */
@ServiceProvider(service=FXMLOpener.class)
public final class SceneBuilderFXMLOpener extends FXMLOpener {
    private static final Logger LOG = Logger.getLogger(SceneBuilderFXMLOpener.class.getName());
    private Settings settings = Settings.getInstance();
 
    @Override
    public boolean isEnabled(Lookup context) {
        return settings.getSelectedHome() != null;
    }

    @Override
    @Messages("LBL_SceneBuilder_Out=JavaFX Scene Builder")
    public boolean open(Lookup context) {
        String execPath = getExecutablePath();
        if (execPath == null) {
            return false;
        }
        
        List<String> cmdList = new ArrayList<String>();
        cmdList.add(getExecutablePath());
        
        boolean allSaved = true;
        Collection<? extends DataObject> dobjs = context.lookupAll(DataObject.class);
        for (DataObject dataObject : dobjs) {
            try {
                SaveCookie sc = dataObject.getLookup().lookup(SaveCookie.class);
                if (sc != null) sc.save();
                
            } catch (IOException e) {
                allSaved = false;
                LOG.log(Level.SEVERE, null, e);
                return false;
            }
        }

        if (allSaved) {
            String firstPath = null;
            for (DataObject dataObject : dobjs) {
                if (firstPath == null) {
                    firstPath = dataObject.getPrimaryFile().getPath();
                }
                cmdList.add(dataObject.getPrimaryFile().getPath());
            }
            if (firstPath != null) {
                try {
                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.start();
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @NbBundle.Messages({
        "LOG_NO_HOME=SceneBuilder home is not set",
        "# {0} - SceneBuilder home path",
        "LOG_HOME_INVALID=SceneBuilder home \"{0}\" is not valid. Please, repair the SceneBuilder installation or choose another one"
    })
    private String getExecutablePath() {
        Home home = settings.getSelectedHome();
        if (home != null && home.isValid()) {
            return home.getLauncherPath();
        } else {
            if (home == null) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest(Bundle.LOG_NO_HOME());
                }
            } else {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, Bundle.LOG_HOME_INVALID(home));
                }
            }
        }
        return null;
    }
}
