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
package org.netbeans.modules.cordova.platforms.android;

import java.awt.Image;
import org.netbeans.modules.cordova.platforms.api.ClientProjectUtilities;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperProvider;
import org.netbeans.modules.web.browser.spi.EnhancedBrowserFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Jan Becicka
 */
@NbBundle.Messages({
    "LBL_DeviceDefault=Android Device (Default Browser)",
    "LBL_DeviceChrome=Android Device (Chrome)",
    "LBL_EmulatorDefault=Android Emulator (Default Browser)"
})
public abstract class AndroidBrowserFactory implements EnhancedBrowserFactory, HtmlBrowser.Factory, BrowserURLMapperProvider {

    private BrowserURLMapperImplementation urlMapper;

    public AndroidBrowserFactory() {
        urlMapper = ClientProjectUtilities.createMobileBrowserURLMapper();
    }
    
    
    @Override
    public BrowserFamilyId getBrowserFamilyId() {
        return BrowserFamilyId.ANDROID;
    }

    @Override
    public boolean canCreateHtmlBrowserImpl() {
        return true;
    }

    @Override
    public BrowserURLMapperImplementation getBrowserURLMapper() {
        return urlMapper;
    }


    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class DeviceDefault extends AndroidBrowserFactory {

        @Override
        public String getDisplayName() {
            return Bundle.LBL_DeviceDefault();
        }

        @Override
        public HtmlBrowser.Impl createHtmlBrowserImpl() {
            return new AndroidBrowser(AndroidBrowser.Kind.ANDROID_DEVICE_DEFAULT);
        }

        @Override
        public String getId() {
            return AndroidBrowser.Kind.ANDROID_DEVICE_DEFAULT.toString(); // NOI18N
        }

        @Override
        public boolean hasNetBeansIntegration() {
            return false;
        }
        
        @Override
        public Image getIconImage(boolean small) {
            return ImageUtilities.loadImage("org/netbeans/modules/cordova/platforms/android/androiddevice" + (small?"16.png":".png"), false);
        }
        
    }

    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class DeviceChrome extends AndroidBrowserFactory {

        @Override
        public String getDisplayName() {
            return Bundle.LBL_DeviceChrome();
        }

        @Override
        public HtmlBrowser.Impl createHtmlBrowserImpl() {
            return new AndroidBrowser(AndroidBrowser.Kind.ANDROID_DEVICE_CHROME);
        }

        @Override
        public String getId() {
            return AndroidBrowser.Kind.ANDROID_DEVICE_CHROME.toString(); // NOI18N
        }
        
        @Override
        public boolean hasNetBeansIntegration() {
            return true;
        }

        @Override
        public Image getIconImage(boolean small) {
            return ImageUtilities.loadImage("org/netbeans/modules/cordova/platforms/android/androiddevice" + (small?"16.png":".png"), false);
        }
        
        
    }
    
    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class EmulatorDefault extends AndroidBrowserFactory {

        @Override
        public String getDisplayName() {
            return Bundle.LBL_EmulatorDefault();
        }

        @Override
        public HtmlBrowser.Impl createHtmlBrowserImpl() {
            return new AndroidBrowser(AndroidBrowser.Kind.ANDROID_EMULATOR_DEFAULT);
        }

        @Override
        public String getId() {
            return AndroidBrowser.Kind.ANDROID_EMULATOR_DEFAULT.toString(); // NOI18N
        }
        
        @Override
        public boolean hasNetBeansIntegration() {
            return false;
        }
        
        @Override
        public Image getIconImage(boolean small) {
            return ImageUtilities.loadImage("org/netbeans/modules/cordova/platforms/android/androidemulator" + (small?"16.png":".png"), false);
        }

    }
    

}
