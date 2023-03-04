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

package org.netbeans.modules.profiler.snaptracer.impl.options;

import javax.swing.Icon;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.snaptracer.impl.icons.TracerIcons;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;

/**
 *
 * @author Jiri Sedlacek
 */
final class TracerOptionsCategory extends OptionsCategory {

    private static TracerOptionsCategory INSTANCE;


    public static synchronized TracerOptionsCategory instance() {
        if (INSTANCE == null) INSTANCE = new TracerOptionsCategory();
        return INSTANCE;
    }

    public Icon getIcon() {
        return Icons.getIcon(TracerIcons.TRACER_32);
    }

    public String getCategoryName() {
        return "Tracer";
    }

    public String getTitle() {
        return "Tracer";
    }

    public OptionsPanelController create() {
        return new TracerOptionsPanelController();
    }

    private TracerOptionsCategory() {}

}
