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
import org.openide.util.Lookup;

/**
 * A context action which, once enabled, remains enabled.
 * <p/>
 * The canonical example of this sort of action in the NetBeans IDE is
 * NextErrorAction:  It becomes enabled when the output window gains
 * focus.  But it should remain enabled when focus goes back to the
 * editor, and still work against whatever context the output window
 * gave it to work on.  Such cases are rare but legitimate.
 * <p/>
 * Use judiciously - such actions are temporary memory
 * leaks - the action will retain the last usable collection of
 * objects it had to work on as long as there are any property
 * change listeners attached to it.
 *
 * @param <T> The type this object is sensitive to
 * @author Tim Boudreau
 */
public abstract class SurviveSelectionChange<T> extends ContextAction<T> {

    protected SurviveSelectionChange(Class<T> type) {
        super(type);
    }

    protected SurviveSelectionChange(Class<T> type, String displayName, Image icon) {
        super(type, displayName, icon);
    }

    @Override
    ActionStub<T> createStub(Lookup actionContext) {
        return new RetainingStub<T>(actionContext, this);
    }

    @Override
    boolean checkQuantity(Collection<? extends T> targets) {
        return super.checkQuantity(targets) || stub != null &&
                super.checkQuantity(((RetainingStub<T>) stub).retained);
    }
}
