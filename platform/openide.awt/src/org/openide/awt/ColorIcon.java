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
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * Icon to paint a colored rectangle in color picker combo box.
 *
 * @author S. Aubrecht
 * @see ColorComboBox
 */
final class ColorIcon implements Icon {

    private final Color color;
    private final int size;

    public ColorIcon( Color color, int size ) {
        this.color = color;
        this.size = size;
    }

    @Override
    public void paintIcon( Component c, Graphics g, int x, int y ) {
        g.setColor( Color.black );
        g.drawRect( x, y, size-1, size-1 );
        if( null == color ) {
            g.drawLine( x, y+size-1, x+size-1, y );
        } else {
            g.setColor( color );
            g.fillRect( x+1, y+1, size-2, size-2 );
        }
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

}
