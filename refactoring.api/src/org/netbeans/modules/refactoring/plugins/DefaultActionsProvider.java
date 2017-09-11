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

package org.netbeans.modules.refactoring.plugins;

import java.util.Collection;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.impl.SafeDeleteUI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class)
public class DefaultActionsProvider extends ActionsImplementationProvider {
    
    /** Creates a new instance of DefaultActionsProvider */
    public DefaultActionsProvider() {
    }
    @Override
    public boolean canDelete(Lookup lookup) {
        return false;
//        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
//        boolean result = false;
//        for (Node node:nodes) {
//            DataObject dob = (DataObject) node.getCookie(DataObject.class);
//            if (dob==null) {
//                return false;
//            } else {
//                if (dob.getPrimaryFile().isFolder()) {
//                    return false;
//                }
//            }
//            result=true;
//        }
//        return result;
    }

    @Override
    public void doDelete(final Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        FileObject[] fobs = new FileObject[nodes.size()];
        int i = 0;
        for (Node node:nodes) {
            DataObject dob = (DataObject) node.getCookie(DataObject.class);
            if (dob!=null) {
                fobs[i++] = dob.getPrimaryFile();
            }
        }
        UI.openRefactoringUI(new SafeDeleteUI(fobs));
    }
}
