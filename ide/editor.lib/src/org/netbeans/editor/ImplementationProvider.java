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

package org.netbeans.editor;

import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.lib2.EditorImplementation;
import org.netbeans.modules.editor.lib2.EditorImplementationProvider;

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
 * @deprecated See org.netbeans.spi.editor.lib2.EditorImplementationProvider
 */
@Deprecated
public abstract class ImplementationProvider {

    private static final ImplementationProvider PROVIDER = new ProviderBridge();

    /** Returns currently registered provider */
    public static ImplementationProvider getDefault() {
        return PROVIDER;
    }

    /** Register your own provider through this method */
    public static void registerDefault(ImplementationProvider prov) {
        EditorImplementation.getDefault().setExternalProvider(new Wrapper(prov));
    }

    /** Returns ResourceBundle for the given class.*/
    public abstract ResourceBundle getResourceBundle(String localizer);

    /** This is temporary method which allows core editor to access
     * glyph gutter action. These actions are then used when user clicks
     * on glyph gutter. In next version this should be removed and redesigned
     * as suggested in issue #16762 */
    public abstract Action[] getGlyphGutterActions(JTextComponent target);

    /** Activates the given component or one of its ancestors.
     * @return whether the component or one of its ancestors was succesfuly activated
     * */
    public boolean activateComponent(JTextComponent c) {
        return false;
    }

    private static final class ProviderBridge extends ImplementationProvider {
        
        public ResourceBundle getResourceBundle(String localizer) {
            return EditorImplementation.getDefault().getResourceBundle(localizer);
        }
        
        public Action[] getGlyphGutterActions(JTextComponent target) {
            return EditorImplementation.getDefault().getGlyphGutterActions(target);
        }
        
        public boolean activateComponent(JTextComponent c) {
            return EditorImplementation.getDefault().activateComponent(c);
        }
    } // End of ProviderBridge class
    
    private static final class Wrapper implements EditorImplementationProvider {
        
        private ImplementationProvider origProvider;
        
        public Wrapper(ImplementationProvider origProvider) {
            this.origProvider = origProvider;
        }
        
        public ResourceBundle getResourceBundle(String localizer) {
            return origProvider.getResourceBundle(localizer);
        }

        public Action[] getGlyphGutterActions(JTextComponent target) {
            return origProvider.getGlyphGutterActions(target);
        }

        public boolean activateComponent(JTextComponent c) {
            return origProvider.activateComponent(c);
        }
        
    } // End of Wrapper class
}
