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
package org.netbeans.modules.html.angular;

import java.awt.Color;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author marekfukala
 */
public class Constants {
    
    public static final ImageIcon ANGULAR_ICON =
                ImageUtilities.loadImageIcon("org/netbeans/modules/html/angular/resources/AngularJS_icon_16.png", false); // NOI18N
    
    public static final Color ANGULAR_COLOR = Color.green.darker();
    
    public static final String JAVASCRIPT_MIMETYPE = "text/javascript"; //NOI18N
}
