/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.conffile;

import org.netbeans.conffile.OS.ChassisType;
import org.netbeans.conffile.ui.DisplayAutoConfigurer;
import java.awt.DisplayMode;

/**
 *
 * @author Tim Boudreau
 */
public class ScreenContributor implements LineSwitchContributor {

    @Override
    public void contribute(LineSwitchWriter writer) {
        int fontsize = DisplayAutoConfigurer.adjustFontSizeForScreenSize(12);
        writer.appendOrReplaceArguments("--fortsize", Integer.toString(fontsize));
        DisplayMode mode = DisplayAutoConfigurer.displayModeNonGui(true);
        if (OS.get().systemType() == ChassisType.NOTEBOOK || !DisplayAutoConfigurer.isCrtAspectRatio(mode)) {
            writer.appendOrReplaceArguments("-J-Dawt.useSystemAAFontSettings=lcd");
        } else {
            writer.appendOrReplaceArguments("-J-Dawt.useSystemAAFontSettings=gasp");
            writer.appendOrReplaceArguments("-J-Dnb.cellrenderer.antialiasing=true");
            writer.appendOrReplaceArguments("-J-Dswing.aatext=true");
        }
        if (mode.getWidth() > 1600) {
            writer.appendOrReplaceArguments("-J-Dhidpi=true")
                    .appendOrReplaceArguments("-J-Dsun.java2d.dpiaware=true");
        }
    }

}
