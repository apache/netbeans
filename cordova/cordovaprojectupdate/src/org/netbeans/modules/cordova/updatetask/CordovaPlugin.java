/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cordova.updatetask;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jan Becicka
 */
public class CordovaPlugin {
    private String id;
    private String url;

    private static final Map<String, String> wellKnowNames = new HashMap<String, String>();

    static {
        wellKnowNames.put("cordova-plugin-device", "Device API");
        wellKnowNames.put("cordova-plugin-network-information", "Network Connection");
        wellKnowNames.put("cordova-plugin-battery-status", "Battery Events");
        wellKnowNames.put("cordova-plugin-device-motion", "Acceleromatter");
        wellKnowNames.put("cordova-plugin-device-orientation", "Compass");
        wellKnowNames.put("cordova-plugin-geolocation", "Geolocation");
        wellKnowNames.put("cordova-plugin-camera", "Camera");
        wellKnowNames.put("cordova-plugin-media-capture", "Media Capture");
        wellKnowNames.put("cordova-plugin-media", "Media");
        wellKnowNames.put("cordova-plugin-file", "File API");
        wellKnowNames.put("cordova-plugin-file-transfer", "File Transfer");
        wellKnowNames.put("cordova-plugin-dialogs", "Dialogs (Notifications)");
        wellKnowNames.put("cordova-plugin-vibration", "Vibration");
        wellKnowNames.put("cordova-plugin-contacts", "Contacts");
        wellKnowNames.put("cordova-plugin-globalization", "Globalization");
        wellKnowNames.put("cordova-plugin-splashscreen", "Splashscreen");
        wellKnowNames.put("cordova-plugin-console", "Debugger Console");
   }

    public CordovaPlugin(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        String name = wellKnowNames.get(id);
        return name!=null?name:getId();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CordovaPlugin other = (CordovaPlugin) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
}
