/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.platforms.ios;

import java.awt.Image;
import org.netbeans.modules.cordova.platforms.api.ClientProjectUtilities;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperProvider;
import org.netbeans.modules.web.browser.spi.EnhancedBrowserFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Jan Becicka
 */
@NbBundle.Messages({
    "LBL_DeviceDefault=iOS Device",
    "LBL_SimulatorDefault=iOS Simulator"
})
public abstract class IOSBrowserFactory implements EnhancedBrowserFactory, HtmlBrowser.Factory, BrowserURLMapperProvider {
    
    private BrowserURLMapperImplementation urlMapper;

    public IOSBrowserFactory() {
        urlMapper = ClientProjectUtilities.createMobileBrowserURLMapper();
    }

    @Override
    public BrowserFamilyId getBrowserFamilyId() {
        return BrowserFamilyId.IOS;
    }

    @Override
    public boolean canCreateHtmlBrowserImpl() {
        return Utilities.isMac();
    }

    @Override
    public BrowserURLMapperImplementation getBrowserURLMapper() {
        return urlMapper;
    }

    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class EmulatorDefault extends IOSBrowserFactory {

        @Override
        public String getDisplayName() {
            return Bundle.LBL_SimulatorDefault();
        }

        @Override
        public HtmlBrowser.Impl createHtmlBrowserImpl() {
            return new IOSBrowser(IOSBrowser.Kind.IOS_SIMULATOR_DEFAULT);
        }

        @Override
        public String getId() {
            return IOSBrowser.Kind.IOS_SIMULATOR_DEFAULT.toString(); // NOI18N
        }

        @Override
        public boolean hasNetBeansIntegration() {
            return true;
        }
        
        @Override
        public Image getIconImage(boolean small) {
            return ImageUtilities.loadImage("org/netbeans/modules/cordova/platforms/ios/iossimulator" + (small?"16.png":".png"), false);
        }
    }

    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class DeviceDefault extends IOSBrowserFactory {

        @Override
        public String getDisplayName() {
            return Bundle.LBL_DeviceDefault();
        }

        @Override
        public HtmlBrowser.Impl createHtmlBrowserImpl() {
            return new IOSBrowser(IOSBrowser.Kind.IOS_DEVICE_DEFAULT);
        }

        @Override
        public String getId() {
            return IOSBrowser.Kind.IOS_DEVICE_DEFAULT.toString(); // NOI18N
        }

        @Override
        public boolean hasNetBeansIntegration() {
            return true;
        }

        @Override
        public Image getIconImage(boolean small) {
            return ImageUtilities.loadImage("org/netbeans/modules/cordova/platforms/ios/iosdevice" + (small?"16.png":".png"), false);
        }
        
    }
}
