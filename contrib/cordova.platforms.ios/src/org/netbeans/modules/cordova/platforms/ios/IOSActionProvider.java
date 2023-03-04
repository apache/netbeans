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
package org.netbeans.modules.cordova.platforms.ios;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;
import org.netbeans.modules.cordova.platforms.spi.BuildPerformer;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Cordova build action
 * @author Jan Becicka
 * 
 */
public class IOSActionProvider implements ActionProvider {
    private final Project p;

    public IOSActionProvider(Project p) {
        this.p = p;
    }
    
    @Override
    public String[] getSupportedActions() {
        return new String[]{
                    COMMAND_BUILD,
                    COMMAND_CLEAN,
                    COMMAND_RUN,
                    COMMAND_RUN_SINGLE,
                    COMMAND_REBUILD
                };
    }

    @NbBundle.Messages({
        "ERR_NotMac=iOS Development is available only on Mac OS X",
        "ERR_Title=Error",
        "LBL_Opening=Connecting to iOS Simulator.\n Please start Mobile Safari if it is not already running.",
        "ERR_NO_Xcode=Supported version of Xcode and Command Line Tools for Xcode not found.\n"
            + "Make sure, that you have latest version of Xcode and iOS SDK installed from Mac App Store."
    })
    @Override
    public void invokeAction(String command, final Lookup context) throws IllegalArgumentException {
        if (!Utilities.isMac()) {
            NotifyDescriptor not = new NotifyDescriptor(
                    Bundle.LBL_NoMac(),
                    Bundle.ERR_Title(),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    null,
                    null);
            DialogDisplayer.getDefault().notify(not);
            return;
        }

        if (!PlatformManager.getPlatform(PlatformManager.IOS_TYPE).isReady()) {
            NotifyDescriptor not = new NotifyDescriptor(
                    Bundle.ERR_NO_Xcode(),
                    Bundle.ERR_Title(),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    null,
                    null);
            DialogDisplayer.getDefault().notify(not);
            return;
        }
        
        final BuildPerformer build = Lookup.getDefault().lookup(BuildPerformer.class);
        assert build != null;
        try {
            switch (command) {
                case COMMAND_BUILD:
                    build.perform(BuildPerformer.BUILD_IOS, p);
                    break;
                case COMMAND_CLEAN:
                    build.perform(BuildPerformer.CLEAN_IOS, p);
                    break;
                case COMMAND_RUN:
                case COMMAND_RUN_SINGLE:
                    WebKitDebuggingSupport.getDefault().stopDebugging(true);
                    build.perform(BuildPerformer.RUN_IOS, p);
                    break;
                case COMMAND_REBUILD:
                    build.perform(BuildPerformer.REBUILD_IOS, p);
            }
        } catch (UnsupportedOperationException | IllegalStateException ex) {
                NotifyDescriptor.Message not = new DialogDescriptor.Message(
                        ex.getMessage(),
                        DialogDescriptor.ERROR_MESSAGE);
                Object value = DialogDisplayer.getDefault().notify(not);
            return;
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return Utilities.isMac();
    }
    
}
