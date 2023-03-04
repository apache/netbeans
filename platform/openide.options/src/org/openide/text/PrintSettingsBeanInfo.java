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
package org.openide.text;


import java.awt.Image;
import java.beans.*;
import org.openide.util.*;


/** BeanInfo for PrintSettings.
*
* @author Ales Novak
*/
public class PrintSettingsBeanInfo extends SimpleBeanInfo {
    /** Returns the PrintSettings' icon */
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/openide/text/printSettings.gif"); // NOI18N
    }

    /** Descriptor of valid properties
    * @return array of properties
    */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] desc = new PropertyDescriptor[] {
                    new PropertyDescriptor(PrintSettings.PROP_WRAP, PrintSettings.class), // 0
                    new PropertyDescriptor(PrintSettings.PROP_HEADER_FORMAT, PrintSettings.class), // 1
                    new PropertyDescriptor(PrintSettings.PROP_FOOTER_FORMAT, PrintSettings.class), // 2
                    new PropertyDescriptor(PrintSettings.PROP_HEADER_FONT, PrintSettings.class), // 3
                    new PropertyDescriptor(PrintSettings.PROP_FOOTER_FONT, PrintSettings.class), // 4
                    new PropertyDescriptor(PrintSettings.PROP_HEADER_ALIGNMENT, PrintSettings.class), // 5
                    new PropertyDescriptor(PrintSettings.PROP_FOOTER_ALIGNMENT, PrintSettings.class), // 6

                    //        new PropertyDescriptor(PrintSettings.PROP_PAGE_FORMAT, PrintSettings.class), // 7
                    new PropertyDescriptor(PrintSettings.PROP_LINE_ASCENT_CORRECTION, PrintSettings.class) // 8
                };
            desc[0].setDisplayName(NbBundle.getMessage(PrintSettings.class, "PROP_WRAP"));
            desc[0].setShortDescription(NbBundle.getMessage(PrintSettings.class, "HINT_WRAP"));
            desc[1].setDisplayName(NbBundle.getMessage(PrintSettings.class, "PROP_HEADER_FORMAT"));
            desc[1].setShortDescription(NbBundle.getMessage(PrintSettings.class, "HINT_HEADER_FORMAT"));
            desc[2].setDisplayName(NbBundle.getMessage(PrintSettings.class, "PROP_FOOTER_FORMAT"));
            desc[2].setShortDescription(NbBundle.getMessage(PrintSettings.class, "HINT_FOOTER_FORMAT"));
            desc[3].setDisplayName(NbBundle.getMessage(PrintSettings.class, "PROP_HEADER_FONT"));
            desc[3].setShortDescription(NbBundle.getMessage(PrintSettings.class, "HINT_HEADER_FONT"));
            desc[4].setDisplayName(NbBundle.getMessage(PrintSettings.class, "PROP_FOOTER_FONT"));
            desc[4].setShortDescription(NbBundle.getMessage(PrintSettings.class, "HINT_FOOTER_FONT"));
            desc[5].setDisplayName(NbBundle.getMessage(PrintSettings.class, "PROP_HEADER_ALIGNMENT"));
            desc[5].setShortDescription(NbBundle.getMessage(PrintSettings.class, "HINT_HEADER_ALIGNMENT"));
            desc[5].setPropertyEditorClass(PrintSettings.AlignmentEditor.class);
            desc[6].setDisplayName(NbBundle.getMessage(PrintSettings.class, "PROP_FOOTER_ALIGNMENT"));
            desc[6].setShortDescription(NbBundle.getMessage(PrintSettings.class, "HINT_FOOTER_ALIGNMENT"));
            desc[6].setPropertyEditorClass(PrintSettings.AlignmentEditor.class);

            /*
            desc[7].setDisplayName(PrintSettings.getString("PROP_PAGE_FORMAT"));
            desc[7].setShortDescription(PrintSettings.getString("HINT_PAGE_FORMAT"));
            desc[7].setPropertyEditorClass(PrintSettings.PageFormatEditor.class);
            */
            desc[7].setDisplayName(NbBundle.getMessage(PrintSettings.class, "PROP_LINE_ASCENT_CORRECTION"));
            desc[7].setShortDescription(NbBundle.getMessage(PrintSettings.class, "HINT_LINE_ASCENT_CORRECTION"));

            return desc;
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);

            return null;
        }
    }
}
