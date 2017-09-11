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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.spi.actions;

import java.awt.Image;
import java.util.Collection;

/**
 * Subclass of ContextAction which does not support multi-selection -
 * like ContextAction, it is sensitive to a particular type.  However,
 * it only is enabled if there is exactly one object of type <code>type</code>
 * in the selection.
 * @param <T> The type this action is sensitive to
 * @author Tim Boudreau
 */
public abstract class Single<T> extends ContextAction<T> {
    protected Single(Class<T> type) {
        super(type);
    }

    protected Single(Class<T> type, String displayName, Image icon) {
        super(type, displayName, icon);
    }

    /**
     * Delegates to actionePerformed(T)</code> with the first and
     * only element of the collection.
     * @param targets The objects this action may operate on
     */
    @Override
    protected final void actionPerformed(Collection<? extends T> targets) {
        actionPerformed(targets.iterator().next());
    }

    /**
     * Actually perform the action.
     * @param target The only instance of <code>T</code> in the action
     * context.
     */
    protected abstract void actionPerformed(T target);

    @Override
    protected final boolean checkQuantity(int count) {
        return count == 1;
    }

    /**
     * Determine if this action should be enabled.  This method will only be
     * called if the size of the collection == 1.  The default implementation
     * returns <code>true</code>.  If you need to do some further
     * test on the collection of objects to determine if the action should
     * really be enabled or not, override this method do that here.
     *
     * @param targets A collection of objects of type <code>type</code>
     * @return Whether or not the action should be enabled.
     */
    @Override
    protected final boolean isEnabled(Collection<? extends T> targets) {
        //Overridden only in order to have different javadoc
        assert !targets.isEmpty();
        return isEnabled (targets.iterator().next());
    }

    /**
     * Determine if the action should be enabled for this object.
     * @param target The target object.
     * @return true if the action should be enabled
     */
    protected boolean isEnabled (T target) {
        return true;
    }
}
