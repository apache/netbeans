/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.refactoring;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=TreeElementFactoryImplementation.class, position=189)
public class MavenTreeElementFactory implements TreeElementFactoryImplementation {

    private final Map<Object,TreeElement> created = new WeakHashMap<Object,TreeElement>();

    @Override public TreeElement getTreeElement(Object o) {
        TreeElement old = created.get(o);
        if (old == null) {
            created.put(o, old = doGetTreeElement(o));
        }
        return old;
    }

    @Override public void cleanUp() {
        created.clear();
    }

    private TreeElement doGetTreeElement(Object o) {
        if (o instanceof NBVersionInfo) {
            return new ArtifactTreeElement((NBVersionInfo) o);
        } else if (o instanceof RepositoryInfo) {
            return new RepositoryTreeElement((RepositoryInfo) o);
        } else if (o instanceof RefactoringElement) {
            RefactoringElement el = (RefactoringElement) o;
            ReferringClass ref = el.getLookup().lookup(ReferringClass.class);
            if (ref != null) {
                return new ReferringClassTreeElement(ref, el);
            }
        }
        return null;
    }

}
