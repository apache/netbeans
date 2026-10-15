/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

        @Override
        public Object[] getKeysAndValues() {
            if (defaults == null) {
                defaults = createKeysAndValues();
            }
            return defaults;
        }

        public abstract Object[] createKeysAndValues();

    }
}
