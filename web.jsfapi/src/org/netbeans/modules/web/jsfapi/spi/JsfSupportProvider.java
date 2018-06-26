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
package org.netbeans.modules.web.jsfapi.spi;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Someone should provide an implementation of this class in the global lookup.
 *
 * @author marekfukala
 */
public abstract class JsfSupportProvider {

    private static final Logger LOGGER = Logger.getLogger(JsfSupportProvider.class.getName());
    //note: JsfSupport DOES hold a strong reference to the Project (which is not really 
    //necessary, may be got from FEQ all the time) so I cannot hold the JsfSupport strongly
    //in the WeakHashMap - it would never GC due to the strong ref in this GC root.
    static final Map<Project, Reference<JsfSupport>> CACHE = new WeakHashMap<Project, Reference<JsfSupport>>();

    public static JsfSupport get(Source source) {
        FileObject fo = source.getFileObject();
        if (fo == null) {
            return null;
        } else {
            return get(fo);
        }
    }

    public static JsfSupport get(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }
        return get(project);

    }

    public static synchronized JsfSupport get(Project project) {
        Reference<JsfSupport> support_ref = CACHE.get(project);
        if (support_ref != null) {
            JsfSupport support = support_ref.get();
            if (support != null) {
                //use the cached one...
                return support;
            }
        }

        //...or create new one and put to the cache
        JsfSupportProvider provider = Lookup.getDefault().lookup(JsfSupportProvider.class);
        if (provider == null) {
            LOGGER.warning("There's no instance of JsfSupportProvider registered in the global lookup!"); //NOI18N
            return null;
        }
        JsfSupport support = provider.getSupport(project);
        if (support == null) {
            LOGGER.fine(
                    String.format("The implementation %s of JsfSupportProvider returned no JsfSupport instance for project %s", //NOI18N
                    provider.getClass().getName(),
                    getProjectDisplayName(project)));
            return null;
        }
        CACHE.put(project, new WeakReference<JsfSupport>(support));
        
        return support;

    }

    private static String getProjectDisplayName(Project project) {
        return FileUtil.getFileDisplayName(project.getProjectDirectory());
    }

    public abstract JsfSupport getSupport(Project project);
}
