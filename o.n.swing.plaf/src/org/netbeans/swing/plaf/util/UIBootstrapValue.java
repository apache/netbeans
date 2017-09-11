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
/*
 * UIBootstrapValue.java
 *
 * Created on March 29, 2004, 4:17 PM
 */

package org.netbeans.swing.plaf.util;

import javax.swing.*;

/**
 * Value that can be placed into UIDefaults, which will put additional items into
 * UIDefaults when its value is requested.
 * <p>
 * The idea is to avoid putting a lot of things that may not be used into
 * UIDefaults on startup.  So, for example, the first time a tab control's UI
 * is requested, this value will return the string from which the UI can be
 * fetched - but first it will put the assorted keys and values that that UI
 * will need into UIDefaults.
 * <p>
 * Since multiple UIs may require the same things in UIDefaults, there is the
 * method createShared(), which will create another instance (really an inner
 * class instance) that shares the code and key/value pairs.  Whichever is
 * asked for first will initialize the keys and values required.  So the usage
 * pattern is something like:
 * <pre>
 * Object someKeysAndValues = new Object[] {"fooColor", Color.BLUE, "barColor", Color.RED};
 * UIBootstrapValue bv = new UIBootstrapValue ("com.foo.FnordUIClass", someKeysAndValues);
 * Object next = bv.createShared ("com.foo.ThiptUIClass");
 * UIManager.put ("FnordUI", bv);
 * UIManager.put ("ThiptUI", next);
 * </pre>
 *
 * @author  Tim Boudreau
 */
public class UIBootstrapValue implements UIDefaults.LazyValue {
    private boolean installed = false;
    private final String uiClassName;
    protected Object[] defaults;
    /** Creates a new instance of UIBootstrapValue */
    public UIBootstrapValue(String uiClassName, Object[] defaults) {
        this.defaults = defaults;
        this.uiClassName = uiClassName;
    }
    
    /** Create the value that UIDefaults will return.  If the keys and values
     * the UI class we're representing requires have not yet been installed,
     * this will install them.
     */
    public Object createValue(UIDefaults uidefaults) {
        if (!installed) {
            installKeysAndValues(uidefaults);
        }
        return uiClassName;
    }
    
    /** Install the defaults the UI we're representing will need to function */
    private void installKeysAndValues(UIDefaults ui) {
        ui.putDefaults (getKeysAndValues());
        installed = true;
    }

    public Object[] getKeysAndValues() {
        return defaults;
    }

    public void uninstall() {
        if (defaults == null) {
            return;
        }
        for (int i=0; i < defaults.length; i+=2) {
            UIManager.put (defaults[i], null);
        }
        //null defaults so a Meta instance won't cause us to do work twice
        defaults = null;
    }

    public String toString() {
        return getClass() + " for " + uiClassName; //NOI18N
    }

    /** Create another entry value to put in UIDefaults, which will also
     * trigger installing the keys and values, to ensure that they are only
     * added once, by whichever entry is asked for the value first. */
    public UIDefaults.LazyValue createShared (String uiClassName) {
        return new Meta (uiClassName);
    }
    
    private class Meta implements UIDefaults.LazyValue {
        private String name;
        public Meta (String uiClassName) {
            this.name = uiClassName;
        }
        
        public Object createValue(javax.swing.UIDefaults uidefaults) {
            if (!installed) {
                installKeysAndValues(uidefaults);
            }
            return name;
        }

        public String toString() {
            return "Meta-" + super.toString() + " for " + uiClassName; //NOI18N
        }
    }

    public abstract static class Lazy extends UIBootstrapValue implements UIDefaults.LazyValue {
        public Lazy (String uiClassName) {
            super (uiClassName, null);
        }

        public Object[] getKeysAndValues() {
            if (defaults == null) {
                defaults = createKeysAndValues();
            }
            return defaults;
        }

        public abstract Object[] createKeysAndValues();

    }
}
