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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.java.source;

import java.net.URL;
import java.util.EventObject;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Event used to notify the {@link ClassIndexListener} about
 * a change of declared types in the {@link ClassIndex}
 * @author Tomas Zezula
 */
public final class TypesEvent extends EventObject {

    private final URL root;
    private final Iterable<? extends ElementHandle<TypeElement>> types;
    private final ElementHandle<ModuleElement> module;

    TypesEvent (
            final ClassIndex source,
            final URL root,
            final ElementHandle<ModuleElement> module,
            final Iterable<? extends ElementHandle<TypeElement>> types) {
        super (source);
        Parameters.notNull("root", root);   //NOI18N
        Parameters.notNull("types", types); //NOI18N
        this.root = root;
        this.module = module;
        this.types = types;
    }

    /**
     * Returns an {@link URL} of the affected root.
     * @return the affected root
     * @since 2.23
     */
    @NonNull
    public URL getRoot() {
        return root;
    }

    /**
     * Returns the affected declared types.
     * @return an {@link Iterable} of {@link TypeElement} handles
     */
    @NonNull
    public Iterable<? extends ElementHandle<TypeElement>> getTypes () {
        return this.types;
    }

    /**
     * Returns the affected module in case of module-info change.
     * @return a {@link ModuleElement} handles
     * @since 2.23
     */
    @CheckForNull
    public ElementHandle<ModuleElement> getModule() {
        return this.module;
    }

    @NonNull
    @Override
    public String toString () {
        return String.format(
                "TypesEvent for root: %s changed module: %s, changed types: %s",    //NOI18N
                root,
                module,
                types);
    }

}
