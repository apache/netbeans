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
package org.netbeans.conffile.ui;

import org.netbeans.conffile.OS;
import static org.netbeans.conffile.ui.Localization.TWEAK_CONSOLE_LOGGER;
import static org.netbeans.conffile.ui.Localization.TWEAK_OPENGL;
import static org.netbeans.conffile.ui.Localization.TWEAK_STATUS_LINE_IN_MENU_BAR;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * Entry for a simple boolean or-or-off alteration to the configuration
 * file.
 *
 * @author Tim Boudreau
 */
final class TweakEntry {

    public static final TweakEntry CONSOLE_LOGGER = new TweakEntry("-J-Dnetbeans.logger.console=true", //NOI18N
            TWEAK_CONSOLE_LOGGER);
    public static final TweakEntry OPENGL_PIPELINE = new TweakEntry("-J-Dsun.java2d.opengl=true", //NOI18N
            TWEAK_OPENGL, OS.MAC_OS);
    public static final TweakEntry STATUS_LINE_IN_MENU_BAR = new TweakEntry("-J-Dnetbeans.winsys.statusLine.in.menuBar=true", //NOI18N
            TWEAK_STATUS_LINE_IN_MENU_BAR, OS.MAC_OS);

    private static final TweakEntry[] ALL_TWEAKS = new TweakEntry[]{STATUS_LINE_IN_MENU_BAR, OPENGL_PIPELINE, CONSOLE_LOGGER};

    public static List<TweakEntry> availableTweaks() {
        List<TweakEntry> result = new ArrayList<>(ALL_TWEAKS.length);
        for (TweakEntry t : ALL_TWEAKS) {
            if (t.isDisabled()) {
                continue;
            }
            result.add(t);
        }
        return result;
    }

    final String lineSwitch;
    private final Localization description;
    private final EnumSet<OS> disabledOn = EnumSet.noneOf(OS.class);

    @SuppressWarnings(value = "ManualArrayToCollectionCopy") //NOI18N
    public TweakEntry(String lineSwitch, Localization description, OS... disabledOn) {
        this.lineSwitch = lineSwitch;
        this.description = description;
        for (int i = 0; i < disabledOn.length; i++) {
            this.disabledOn.add(disabledOn[i]);
        }
    }

    public Localization info() {
        return description;
    }

    public boolean isDisabled() {
        return disabledOn.contains(OS.get());
    }

    public String lineSwitch() {
        return lineSwitch;
    }

    @Override
    public String toString() {
        return description.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.lineSwitch);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TweakEntry other = (TweakEntry) obj;
        return Objects.equals(this.lineSwitch, other.lineSwitch);
    }
}
