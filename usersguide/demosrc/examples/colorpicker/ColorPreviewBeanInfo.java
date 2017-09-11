/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package examples.colorpicker;

import java.beans.*;

/** BeanInfo for ColorPreview bean.
 */
public class ColorPreviewBeanInfo extends SimpleBeanInfo {

    // Property identifiers //GEN-FIRST:Properties
    private static final int PROPERTY_blue = 0;
    private static final int PROPERTY_green = 1;
    private static final int PROPERTY_red = 2;

    // Property array
    private static PropertyDescriptor[] properties = new PropertyDescriptor[3];

    static {
        try {

            properties[PROPERTY_blue] = new PropertyDescriptor( "blue", ColorPreview.class, "getBlue", "setBlue" );
            properties[PROPERTY_green] = new PropertyDescriptor( "green", ColorPreview.class, "getGreen", "setGreen" );
            properties[PROPERTY_red] = new PropertyDescriptor( "red", ColorPreview.class, "getRed", "setRed" );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties

        // Here you can add code for customizing the properties array.

    }//GEN-LAST:Properties

    // EventSet identifiers //GEN-FIRST:Events

    private static final int EVENT_propertyChangeListener = 0;
    // EventSet array

    private static EventSetDescriptor[] eventSets = new EventSetDescriptor[1];

    static {
        try {

            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor( ColorPreview.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] { "propertyChange" }, "addPropertyChangeListener", "removePropertyChangeListener" );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

    }//GEN-LAST:Events


    private static String ICON_COLOR_16x16 = null; //GEN-BEGIN:Icons
    private static String ICON_COLOR_32x32 = null;
    private static String ICON_MONO_16x16 = null;
    private static String ICON_MONO_32x32 = null; //GEN-END:Icons


    /** This methods returns an array of property descriptors.
     * @return Array of PropertyDescriptor instances associated with this BeanInfo.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return properties;
    }

    /** This methods returns an array of event set descriptors.
     * @return Array of EventSetDescriptor instances associated with this BeanInfo.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return eventSets;
    }


    java.awt.Image icon = loadImage("/tutorial/colorpicker/ColorPreview.gif");

    /** This method returns this bean info icon, depending on given argument.
     * @param iconKind Type of icon.
     * @return Icon associated with this BeanInfo.
     */
    public java.awt.Image getIcon(int iconKind) {

        switch (iconKind) {
            case java.beans.BeanInfo.ICON_COLOR_16x16: return icon;
        }
        return null;

    }
}
