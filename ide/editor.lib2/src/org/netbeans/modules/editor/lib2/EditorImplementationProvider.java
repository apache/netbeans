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

package org.netbeans.modules.editor.lib2;

import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.text.JTextComponent;

/** This is provider of implementation. This package (org.netbeans.editor) 
 * represent editor core which can be used independently on the rest of NetBeans.
 * However this core needs access to higher level functionality like access
 * to localized bundles, access to settings storage, etc. which can be implemented
 * differently by the applications which uses this editor core. For this purpose
 * was created this abstract class and it can be extended with any other methods which
 * are more and more often required by core editor. Example implementation
 * of this provider can be found in org.netbeans.modules.editor package
 * 
 * @author David Konecny
 * @since 10/2001
 */
public interface EditorImplementationProvider {

    /** Returns ResourceBundle for the given class.*/
    public ResourceBundle getResourceBundle(String localizer);

    /** This is temporary method which allows core editor to access
     * glyph gutter action. These actions are then used when user clicks
     * on glyph gutter. In next version this should be removed and redesigned
     * as suggested in issue #16762 */
    public Action[] getGlyphGutterActions(JTextComponent target);

    /** Activates the given component or one of its ancestors.
     * @return whether the component or one of its ancestors was succesfuly activated
     * */
    public boolean activateComponent(JTextComponent c);
}
