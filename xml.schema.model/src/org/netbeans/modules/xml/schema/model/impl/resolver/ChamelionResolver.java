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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.schema.model.impl.resolver;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 * The chamelion resolver is intended to be used only when the initial
 * schema doesn't have target namespace and the sought namespace isn't empty.
 *
 * @author Nikita Krjukov
 */
public class ChamelionResolver {

    public static <T extends NamedReferenceable> T resolve(
            SchemaModelImpl sModel, String namespace, String localName,
            Class<T> type) {
        //
        assert namespace != null;
        Schema schema = sModel.getSchema();
        if (schema == null) {
            return null;
        }
        String myTargetNamespace = schema.getTargetNamespace();
        assert myTargetNamespace == null;
        //
        ResolveSession session = new ResolveSession(sModel, namespace);
        T found = null;
        //
        // Find in included recursively at first
        found = IncludeResolver.resolveRecursiveDown(
                sModel, localName, type, session);
        if (found != null) {
            return found;
        }
        //
        // Find in direct imports
        found = ImportResolver.findInImports(
                sModel, namespace, localName, type, session);
        if (found != null) {
            return found;
        }
        //
        ImportResolver.populateRecursivelyIncludedImports(sModel, namespace, session);
        //
        // Find in included imports Down
        found = ImportResolver.findInIncludedImportsDown(
                namespace, localName, type, session, false);
        if (found != null) {
            return found;
        }
        //
        // Try using mega-include approach
        found = IncludeResolver.resolveUpwards(
                sModel, namespace, localName, type, session);
        if (found != null) {
            return found;
        }
        //
        ImportResolver.populateMegaIncludedImports(sModel, namespace, session);
        //
        // Find in included imports Down again but now by mega imports
        found = ImportResolver.findInIncludedImportsDown(
                namespace, localName, type, session, true);
        if (found != null) {
            return found;
        }
        //
        // Find in included imports Up
        found = ImportResolver.findInIncludedImportsUpwards(
                namespace, localName, type, session, false);
        if (found != null) {
            return found;
        }
        //
        // Find in included mega-imports Up
        found = ImportResolver.findInIncludedImportsUpwards(
                namespace, localName, type, session, true);
        if (found != null) {
            return found;
        }
        //
        //
        return null;
    }


}
