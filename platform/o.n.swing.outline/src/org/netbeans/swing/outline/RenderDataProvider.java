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
package org.netbeans.swing.outline;

import java.awt.Color;
import javax.swing.Icon;

/** A class which can provide rendering data for the tree portion an Outline,
 * such as converting values to text, providing tooltip text and icons.
 * Makes it possible to provide most of the interesting data that affects
 * display without needing to provide a custom cell renderer.
 *
 * @author  Tim Boudreau
 */
public interface RenderDataProvider {
    /** Convert an object in the tree to the string that should be used to
     * display its node */
    public String getDisplayName (Object o);
    /** Returns true of the display name for this object should use HTML 
     * rendering (future support for integration of the lightweight HTML
     * renderer into NetBeans).  */
    public boolean isHtmlDisplayName (Object o);
    /** Get the background color to be used for rendering this node.  Return
     * null if the standard table background or selected color should be used.
     */
    public Color getBackground (Object o);
    /** Get the foreground color to be used for rendering this node.  Return
     * null if the standard table foreground or selected foreground should be
     * used. */
    public Color getForeground (Object o);
    /** Get a description for this object suitable for use in a tooltip.  Return
     * null if no tooltip is desired.  */
    public String getTooltipText (Object o);
    /** Get an icon to be used for this object.  Return null if the look and 
     * feel's default tree folder/leaf icons should be used as appropriate. */
    public Icon getIcon (Object o);
}
