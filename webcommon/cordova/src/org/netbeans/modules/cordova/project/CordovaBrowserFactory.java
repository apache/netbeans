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
package org.netbeans.modules.cordova.project;

import java.awt.Image;
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
    "LBL_CordovaIOSDevice=Cordova (iOS Device)",
    "LBL_CordovaIOSSimulator=Cordova (iOS Simulator)",
    "LBL_CordovaAndroidDevice=Cordova (Android Device)",
    "LBL_CordovaAndroidEmulator=Cordova (Android Emulator)"
})
public abstract class CordovaBrowserFactory implements EnhancedBrowserFactory, HtmlBrowser.Factory, BrowserURLMapperProvider {

    private CordovaURLMapper mapper;
    
    @Override
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        return new CordovaBrowser();
    }

    @Override
    public BrowserFamilyId getBrowserFamilyId() {
        return BrowserFamilyId.PHONEGAP;
    }
    
    @Override
    public BrowserURLMapperImplementation getBrowserURLMapper() {
        if (mapper==null) {
            mapper = new CordovaURLMapper();
        }
        return mapper;
    }
    
    public BrowserURLMapperImplementation.BrowserURLMapper getMapper() {
        return ((CordovaURLMapper) getBrowserURLMapper()).getBrowserURLMapper();
    }


    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class IOSDevice extends CordovaBrowserFactory {

        @Override
        public Image getIconImage(boolean small) {
            return ImageUtilities.loadImage("org/netbeans/modules/cordova/resources/iosdevice"+ (small?"16.png":".png"), false);
        }

        @Override
        public String getDisplayName() {
            return Bundle.LBL_CordovaIOSDevice();
        }

        @Override
        public String getId() {
            return "ios_1"; // NOI18N
        }

        @Override
        public boolean hasNetBeansIntegration() {
            return true;
        }

        @Override
        public boolean canCreateHtmlBrowserImpl() {
            return Utilities.isMac();
        }
    }

    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class IOSSimulator extends CordovaBrowserFactory {

        @Override
        public Image getIconImage(boolean small) {
            return ImageUtilities.loadImage("org/netbeans/modules/cordova/resources/iossimulator" + (small?"16.png":".png"), false);
        }

        @Override
        public String getDisplayName() {
            return Bundle.LBL_CordovaIOSSimulator();
        }

        @Override
        public String getId() {
            return "ios"; // NOI18N
        }

        @Override
        public boolean hasNetBeansIntegration() {
            return true;
        }

        @Override
        public boolean canCreateHtmlBrowserImpl() {
            return Utilities.isMac();
        }
    }

    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class AndroidEmulator extends CordovaBrowserFactory {

        @Override
        public Image getIconImage(boolean small) {
            return ImageUtilities.loadImage("org/netbeans/modules/cordova/resources/androidemulator" + (small?"16.png":".png"), false);
        }

        @Override
        public String getDisplayName() {
            return Bundle.LBL_CordovaAndroidEmulator();
        }

        @Override
        public String getId() {
            return "android"; // NOI18N
        }

        @Override
        public boolean hasNetBeansIntegration() {
            return false;
        }

        @Override
        public boolean canCreateHtmlBrowserImpl() {
            return true;
        }

        @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
        public static class AndroidDevice extends CordovaBrowserFactory {

            @Override
            public Image getIconImage(boolean small) {
                return ImageUtilities.loadImage("org/netbeans/modules/cordova/resources/androiddevice" + (small?"16.png":".png"), false);
            }

            @Override
            public String getDisplayName() {
                return Bundle.LBL_CordovaAndroidDevice();
            }

            @Override
            public String getId() {
                return "android_1"; // NOI18N
            }

            @Override
            public boolean hasNetBeansIntegration() {
                return false;
            }

            @Override
            public boolean canCreateHtmlBrowserImpl() {
                return true;
            }
        }
    }
}