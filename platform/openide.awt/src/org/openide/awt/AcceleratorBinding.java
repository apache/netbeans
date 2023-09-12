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

package org.openide.awt;

import java.util.Collection;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Permits accelerators to be set on actions based on global registrations.
 * This class is only intended for use from NetBeans infrastructure code:
 * that which either reads shortcut definitions (i.e. the global {@link Keymap});
 * or which loads actions into presenters (e.g. <a href="@org-openide-loaders@/org/openide/awt/Toolbar.html">Toolbar</a>).
 * @since org.openide.loaders 7.13 but moved down to org.openide.awt 7.42
 */
public abstract class AcceleratorBinding {
    private static final Iter ALL = new Iter();

    /**
     * Subclass constructor. Only certain implementations are permitted.
     */
    protected AcceleratorBinding() {
        assert getClass().getName().equals("org.netbeans.core.NbKeymap$AcceleratorBindingImpl") : this;
    }

    /**
     * Finds a keystroke for an action.
     * @param action an action
     * @param definingFile an instance file which defines the action
     * @return a keystroke or null
     */
    protected abstract KeyStroke keyStrokeForAction(Action action, FileObject definingFile);

    /**
     * Associates an {@link Action#ACCELERATOR_KEY} with an action based on a declared shortcut.
     * If an instance of {@link AcceleratorBinding} can be found in default lookup,
     * it will be used to determine the binding. Otherwise nothing is done.
     * @param action an action defined in layers
     * @param definingFile instance file defining the action
     */
    public static void setAccelerator(Action action, FileObject definingFile) {
        for (AcceleratorBinding bnd : ALL.all()) {
            KeyStroke key = bnd.keyStrokeForAction(action, definingFile);
            if (key != null) {
                action.putValue(Action.ACCELERATOR_KEY, key);
                break;
            }
        }
    }

    private static final class Iter implements LookupListener {
        private final Lookup.Result<AcceleratorBinding> result;
        private Collection<? extends AcceleratorBinding> all;

        Iter() {
            result = Lookup.getDefault().lookupResult(AcceleratorBinding.class);
            resultChanged(null);
            result.addLookupListener(this);
        }

        final Collection<? extends AcceleratorBinding> all() {
            return all;
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            all = result.allInstances();
        }
    }

}
