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

import static org.netbeans.conffile.ui.Localization.DEFAULT;
import static org.netbeans.conffile.ui.Localization.GASP;
import static org.netbeans.conffile.ui.Localization.HBGR;
import static org.netbeans.conffile.ui.Localization.HRGB;
import static org.netbeans.conffile.ui.Localization.LCD;
import static org.netbeans.conffile.ui.Localization.OFF;
import static org.netbeans.conffile.ui.Localization.ON;
import static org.netbeans.conffile.ui.Localization.VBGR;
import static org.netbeans.conffile.ui.Localization.VRGB;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Models one possible value of <code>-Dawt.useSystemAAFontSettings</code>.
 *
 * @author Tim Boudreau
 */
final class HintEntry {

    private static final HintEntry HINT_NONE = new HintEntry("off", OFF, RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, true);
    private static final HintEntry HINT_HBGR = new HintEntry("hbgr", HBGR, RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR, false);
    private static final HintEntry HINT_HRGB = new HintEntry("hrgb", HRGB, RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR, false);
    private static final HintEntry HINT_VBGR = new HintEntry("vbgr", VBGR, RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR, false);
    private static final HintEntry HINT_VRGB = new HintEntry("vrgb", VRGB, RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB, false);
    private static final HintEntry HINT_GASP = new HintEntry("gasp", GASP, RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_GASP, true);
    static final HintEntry HINT_DEFAULT = new HintEntry("default", DEFAULT, RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, true);
    static final HintEntry HINT_ON = new HintEntry("on", ON, RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON, true);
    static final HintEntry HINT_GENERIC_LCD = new HintEntry("lcd", LCD, RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, false);

    /**
     * The set of hints available.
     */
    public static final HintEntry[] ALL_HINTS = new HintEntry[]{
        HINT_NONE, HINT_HBGR, HINT_HRGB, HINT_VBGR, HINT_VRGB, HINT_GENERIC_LCD, HINT_GASP, HINT_ON
    };

    final String name;
    final String displayName;
    private final RenderingHints.Key key;
    private final Object value;
    private final boolean crtAppropriate;
    private String description;

    public HintEntry(String name, Localization displayName, RenderingHints.Key key, Object value, boolean crtAppropriate) {
        this(name, displayName.toString(), key, value, crtAppropriate);
        description = displayName.tip();
    }

    public HintEntry(String name, String displayName, RenderingHints.Key key, Object value, boolean crtAppropriate) {
        this.displayName = displayName;
        this.name = name;
        this.key = key;
        this.value = value;
        this.crtAppropriate = crtAppropriate;
    }

    public String description() {
        return description;
    }

    public boolean isCrtAppropriate() {
        return crtAppropriate;
    }

    public String displayName() {
        return displayName;
    }

    public static HintEntry parse(String propertyValue) {
        Set<HintEntry> all = new HashSet<>(Arrays.asList(ALL_HINTS));
        all.add(HINT_DEFAULT);
        all.add(HINT_GENERIC_LCD);
        for (HintEntry e : all) {
            if (e.name.equals(propertyValue)) {
                return e;
            } else if (("lcd_" + e.name).equals(propertyValue)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public Object value() {
        return value;
    }

    void apply(Graphics g, Runnable r) {
        Graphics2D gg = (Graphics2D) g;
        Object old = gg.getRenderingHint(key);
        if (value != null) {
            gg.setRenderingHint(key, value);
        } else {
            //                gg.setRenderingHint(key, null);
        }
        try {
            r.run();
        } finally {
            gg.setRenderingHint(key, old);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.name);
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
        final HintEntry other = (HintEntry) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}
