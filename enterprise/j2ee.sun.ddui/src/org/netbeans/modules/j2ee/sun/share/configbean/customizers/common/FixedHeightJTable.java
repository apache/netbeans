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
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JTable;

/** JTables do not automatically scale their row height with font size, so when
 *  running under, for example --fontsize 16 (common for Chinese), the rows of our
 *  tables are too narrow and only marginally usable.  For --fontsize 24, the text
 *  overlap makes the rows illegible.
 * 
 *  This code is take from a posting Tim Boudreau made on javalobby and automatically
 *  scales the row height with the font size or if the font is changed.  It does not 
 *  take icon height into account  but we don't use icons anywhere.
 *
 * @author Peter Williams
 */
public class FixedHeightJTable extends JTable {

    private boolean firstPaint = true;
    
    public void setFont (Font f) {
        firstPaint = true;
        super.setFont(f);
    }

    private void calcFixedHeight (Graphics g) {
        g.setFont (getFont());
        setRowHeight(g.getFontMetrics().getHeight());
        firstPaint = false;
    }

    public void paint (Graphics g) {
        if (firstPaint) {
            calcFixedHeight(g);
            //Setting the fixed height will generate another paint request,
            //no need to complete this one
            return;
        }
        super.paint (g);
    }
}
