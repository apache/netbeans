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
package org.netbeans.modules.cordova.platforms.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.netbeans.modules.cordova.platforms.spi.PropertyProvider;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class AVD implements Device {

    private String name;
    private HashMap<String, String> props;

    private AVD() {
        this.props = new HashMap();
    }
    
    public static Collection<Device> parse(String output) throws IOException {
        BufferedReader r = new BufferedReader(new StringReader(output));
        
        Pattern pattern = Pattern.compile(" *([\\w]*): (.*)"); //NOI18N
        
        ArrayList<Device> result = new ArrayList<Device>();
        //ignore first line
        String line = r.readLine();
        
        line = r.readLine();
        
        AVD current = new AVD();
        String lastProp = null;
        while (line != null) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                if ("Name".equals(m.group(1))) { //NOI18N
                    current.name = m.group(2);
                } else {
                    current.props.put(m.group(1), m.group(2));
                    lastProp = m.group(1);
                }
            } else {
                if (line.contains("---------")) { //NOI18N
                    result.add(current);
                    current = new AVD();
                } else {
                    current.props.put(lastProp, current.props.get(lastProp) + line);
                }
            }
            line = r.readLine();
            if (line == null && current.name != null) {
                result.add(current);
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AVD{" + "name=" + name + ", props=" + props + '}'; //NOI18N
    }

    @Override
    public boolean isEmulator() {
        return true;
    }

    @Override
    public MobilePlatform getPlatform() {
        return AndroidPlatform.getDefault();
    }

    @Override
    public void addProperties(Properties props) {
        final MobilePlatform android = getPlatform();
        props.put("android.build.target", android.getPrefferedTarget().getName());//NOI18N
        props.put("android.sdk.home", android.getSdkLocation());//NOI18N
        props.put("android.target.device.arg", isEmulator() ? "emulate" : "run");//NOI18N
    }

    @Override
    public ActionProvider getActionProvider(Project p) {
        return new AndroidActionProvider(p);
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer(Project project, PropertyProvider aThis) {
        return new AndroidConfigurationPanel.AndroidConfigurationCustomizer(project, aThis);
    }
    
    @Override
    public void openUrl(String url) {
        try {
            String s = ProcessUtilities.callProcess(
                    ((AndroidPlatform) getPlatform()).getAdbCommand(), 
                    false, 
                    AndroidPlatform.DEFAULT_TIMEOUT, 
                    isEmulator()?"-d":"-e", // NOI18N
                    "wait-for-device", // NOI18N
                    "shell", "am", "start", "-a", "android.intent.action.VIEW", // NOI18N
                    "-n", "com.android.browser/.BrowserActivity", url); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public MobileDebugTransport getDebugTransport() {
        return new AndroidDebugTransport();
    }

    @Override
    public boolean isWebViewDebugSupported() {
        return AndroidPlatform.getDefault().isWebViewDebugSupported(isEmulator());
    }

}
