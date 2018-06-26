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