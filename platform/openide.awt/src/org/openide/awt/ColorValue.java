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

package org.openide.awt;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * Represents one color with some text description.
 *
 * @author Administrator
 * @author S. Aubrecht
 */
class ColorValue {

    static final ColorValue  CUSTOM_COLOR = new ColorValue (loc ("Custom"), null, false); //NOI18N

    private static final Map<Color, String> colorMap = new HashMap<Color, String>();
    static {
        colorMap.put (Color.BLACK,      loc ("Black"));         //NOI18N
        colorMap.put (Color.BLUE,       loc ("Blue"));          //NOI18N
        colorMap.put (Color.CYAN,       loc ("Cyan"));          //NOI18N
        colorMap.put (Color.DARK_GRAY,  loc ("Dark_Gray"));     //NOI18N
        colorMap.put (Color.GRAY,       loc ("Gray"));          //NOI18N
        colorMap.put (Color.GREEN,      loc ("Green"));         //NOI18N
        colorMap.put (Color.LIGHT_GRAY, loc ("Light_Gray"));    //NOI18N
        colorMap.put (Color.MAGENTA,    loc ("Magenta"));       //NOI18N
        colorMap.put (Color.ORANGE,     loc ("Orange"));        //NOI18N
        colorMap.put (Color.PINK,       loc ("Pink"));          //NOI18N
        colorMap.put (Color.RED,        loc ("Red"));           //NOI18N
        colorMap.put (Color.WHITE,      loc ("White"));         //NOI18N
        colorMap.put (Color.YELLOW,     loc ("Yellow"));        //NOI18N
    }
    
    final String text;
    final Color color;
    final boolean isCustom;

    static String toText( Color color ) {
        String text = colorMap.get( color );
        if( null == text && null != color ) {
            StringBuffer sb = new StringBuffer ();
            sb.append ('[').append (color.getRed ()).
                append (',').append (color.getGreen ()).
                append (',').append (color.getBlue ()).
                append (']');
            text = sb.toString ();
        }
        return text;
    }

    ColorValue (Color color, boolean custom) {
        this(toText( color ), color, custom);
    }

    ColorValue (String text, Color color, boolean custom) {
        this.text = text;
        this.color = color;
        this.isCustom = custom;
    }

    @Override
    public String toString() {
        return text;
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ColorValue.class, key);
    }
}
