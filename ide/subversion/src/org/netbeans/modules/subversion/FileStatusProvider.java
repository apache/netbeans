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

package org.netbeans.modules.subversion;

import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.Image;
import java.util.logging.Level;

/**
 * Contract specific for Filesystem <-> UI interaction, to be replaced later with something more
 * sophisticated (hopefuly).
 * 
 * @author Maros Sandor
 */
class FileStatusProvider extends VCSAnnotator {
    private static final int INCLUDE_STATUS = FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    
    private boolean shutdown; 

    public String annotateName(String name, VCSContext context) {
        if (shutdown) return null;
        try {
            return Subversion.getInstance().getAnnotator().annotateNameHtml(name, context, INCLUDE_STATUS);
        } catch (Exception e) {
            Subversion.LOG.log(Level.SEVERE, e.getMessage(), e);            
            return name;
        }
    }

    public Image annotateIcon(Image icon, VCSContext context) {
        if (shutdown) return null;
        try {
            return Subversion.getInstance().getAnnotator().annotateIcon(icon, context, INCLUDE_STATUS);
        } catch (Exception e) {
            Subversion.LOG.log(Level.SEVERE, e.getMessage(), e);
            return icon;
        }        
    }

    public Action[] getActions(VCSContext context, VCSAnnotator.ActionDestination destination) {
        return Annotator.getActions(context, destination);
    }

    void shutdown() {
        shutdown = true;
    }
}
