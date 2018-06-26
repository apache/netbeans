/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
