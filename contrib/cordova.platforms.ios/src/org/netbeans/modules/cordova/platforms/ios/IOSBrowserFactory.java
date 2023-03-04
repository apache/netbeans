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
