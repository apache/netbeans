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
package org.netbeans.modules.html.palette;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(id = "org.netbeans.modules.html.palette.HtmlPaletteCustomizerAction", category = "Tools")
@ActionRegistration(iconInMenu = false, displayName = "#ACT_OpenHTMLCustomizer")
@ActionReference(path = "Menu/Tools/PaletteManager", position = 200)
@Messages("ACT_OpenHTMLCustomizer=&HTML/JSP Code Clips")
public final class HtmlPaletteCustomizerAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        try {
            HtmlPaletteFactory.getHtmlPalette().showCustomizer();
        }
        catch (IOException ioe) {
            Logger.getLogger("global").log(Level.WARNING, null, ioe);
        }
    }
}
