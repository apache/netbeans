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

package org.netbeans.modules.profiler.snaptracer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.snaptracer.impl.icons.TracerIcons;
import org.openide.util.ImageUtilities;

/**
 * TracerProbeDescriptor defines how a TracerProbe appears in the Tracer UI.
 *
 * @author Jiri Sedlacek
 */
public final class TracerProbeDescriptor implements Positionable {

    private final String name;
    private final String description;
    private final Icon icon;
    private final int preferredPosition;
    private final boolean available;


    /**
     * Creates new instance of TracerProbeDescriptor.
     *
     * @param name probe name
     * @param description probe description
     * @param icon probe icon
     * @param preferredPosition preferred position of the probe in UI
     * @param available availability of the probe in actual context
     */
    public TracerProbeDescriptor(String name, String description, Icon icon,
                                 int preferredPosition, boolean available) {
        this.name = name;
        this.description = description;
        this.icon = icon != null ? icon : Icons.getIcon(TracerIcons.PROBE);
        this.preferredPosition = preferredPosition;
        this.available = available;
    }


    /**
     * Returns probe name.
     *
     * @return probe name
     */
    public String getProbeName() { return name; }

    /**
     * Returns probe description.
     *
     * @return probe description
     */
    public String getProbeDescription() { return description; }

    /**
     * Returns probe icon.
     *
     * @return probe icon
     */
    public Icon getProbeIcon() { return icon; }

    /**
     * Returns preferred position of the probe in UI.
     *
     * @return preferred position of the probe in UI
     */
    public int getPreferredPosition() { return preferredPosition; }

    /**
     * Returns true if the probe is available in current context, false otherwise.
     *
     * @return true if the probe is available in current context, false otherwise
     */
    public boolean isProbeAvailable() { return available; }

}
