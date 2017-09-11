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

package org.netbeans.modules.options.keymap;

import java.util.Map;
import org.netbeans.core.options.keymap.api.ShortcutAction;

/**
 *
 * @author Jan Jancura, David Strupl
 */
public class CompoundAction implements ShortcutAction {
    private static final String DEFAULT_PROVIDER = "EditorBridge";
    private Map<String, ShortcutAction> actions;

    public CompoundAction(Map<String, ShortcutAction> actions) {
        this.actions = actions;
    }
    
    /**
     * Use with care, invalidates hashcode.
     * @param mgr
     * @param ac 
     */
    void addAction(String mgr, ShortcutAction ac) {
        this.actions.put(mgr, ac);
    }

    public String getDisplayName () {
        ShortcutAction s = actions.get(DEFAULT_PROVIDER);
        if (s != null) {
            return s.getDisplayName();
}
        for (ShortcutAction sa: actions.values()) {
            String dn = sa.getDisplayName();
            if (dn != null) {
                return dn;
            }
        }
        return "";
    }

    public String getId () {
        ShortcutAction s = actions.get(DEFAULT_PROVIDER);
        if (s != null) {
            return s.getId();
        }
        for (ShortcutAction sa: actions.values()) {
            String id = sa.getId();
            if (id != null) {
                return id;
            }
        }
        return "<error>"; // TODO:
    }

    public String getDelegatingActionId () {
        ShortcutAction s = actions.get(DEFAULT_PROVIDER);
        if (s != null) {
            return s.getDelegatingActionId();
        }
        for (ShortcutAction sa: actions.values()) {
            String id = sa.getDelegatingActionId();
            if (id != null) {
                return id;
            }
        }
        return null; // TODO:
    }
    
    public boolean equals (Object o) {
        if (! (o instanceof CompoundAction)) {
            return false;
        }
        if (actions.get(DEFAULT_PROVIDER) != null) {
            return (getKeymapManagerInstance(DEFAULT_PROVIDER).equals(
                ((CompoundAction)o).getKeymapManagerInstance(DEFAULT_PROVIDER)
            ));
        }
        if (actions.keySet().isEmpty()) {
            return false;
        }
        String k = actions.keySet().iterator().next();
        return (getKeymapManagerInstance(k).equals(
                ((CompoundAction)o).getKeymapManagerInstance(k)
            ));
    }
    
    public int hashCode () {
        if (actions.get(DEFAULT_PROVIDER) != null) {
            return getKeymapManagerInstance(DEFAULT_PROVIDER).hashCode() * 2;
        }
        if (actions.keySet().isEmpty()) {
            return 0;
        }
        String k = actions.keySet().iterator().next();
        return actions.get(k).hashCode() * 2;
    }

    public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
        return actions.get(keymapManagerName);
    }
    
    public String toString() {
        return "CompoundAction[" + actions + "]";
    }
}
