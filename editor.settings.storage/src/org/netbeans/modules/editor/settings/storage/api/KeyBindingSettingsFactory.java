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

package org.netbeans.modules.editor.settings.storage.api;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.editor.settings.MultiKeyBinding;


/**
 * Getters and setters for keymap editor profiles. Instances of this
 * class should be registerred in <code>MimeLookup</code> for particular mime types.
 *
 * @author Jan Jancura
 */
public abstract class KeyBindingSettingsFactory {

    /**
     * Gets the keybindings list, where items are instances of
     * {@link org.netbeans.api.editor.settings.MultiKeyBinding}.
     *
     * @return List of <code>MultiKeyBinding</code>s.
     */
    public abstract List<MultiKeyBinding> getKeyBindings ();
    
    /**
     * Gets the keybindings list for given keymap name, where items 
     * are instances of {@link org.netbeans.api.editor.settings.MultiKeyBinding}.
     *
     * @param profile a name of keymap
     * 
     * @return List of <code>MultiKeyBinding</code>s.
     */
    public abstract List<MultiKeyBinding> getKeyBindings (String profile);

    
    /**
     * Returns default keybindings list for given keymap name, where items 
     * are instances of {@link org.netbeans.api.editor.settings.MultiKeyBinding}.
     *
     * @return List of <code>MultiKeyBinding</code>s.
     */
    public abstract List<MultiKeyBinding> getKeyBindingDefaults (String profile);
    
    /**
     * Gets the keybindings list, where items are instances of 
     * {@link org.netbeans.api.editor.settings.MultiKeyBinding}.
     *
     * @param profile
     * @param keyBindings the list of <code>MultiKeyBindings</code>
     */
    public abstract void setKeyBindings (
        String profile, 
        List<MultiKeyBinding> keyBindings
    );
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be registerred
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be unregisterred
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
}
