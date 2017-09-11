/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
 * or which loads actions into presenters (e.g. {@link Toolbar}).
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
