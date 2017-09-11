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
package org.netbeans.modules.css.editor;

import java.io.IOException;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
@NbBundle.Messages("CssResolver=CSS Files")
@MIMEResolver.ExtensionRegistration(
    mimeType="text/css",
    position=169,
    displayName="#CssResolver",
    extension={ "css" }
)
public class CssProjectSupport {

    private static final WeakHashMap<Project, CssProjectSupport> INSTANCIES = new WeakHashMap<>();

    public static CssProjectSupport findFor(Source source) {
	FileObject fo = source.getFileObject();
	if (fo == null) {
	    return null;
	} else {
	    return findFor(fo);
	}
    }

    public static CssProjectSupport findFor(Document doc) {
	return findFor(DataLoadersBridge.getDefault().getFileObject(doc));
    }

    public static CssProjectSupport findFor(FileObject fo) {
	try {
	    Project p = FileOwnerQuery.getOwner(fo);
	    if (p == null) {
		return null;
	    }
            synchronized (INSTANCIES) {
		CssProjectSupport instance = INSTANCIES.get(p);
		if (instance == null) {
		    instance = new CssProjectSupport(p);
		    INSTANCIES.put(p, instance);
		}
                return instance;
	    }
	} catch (IOException ex) {
	    Exceptions.printStackTrace(ex);
	}

	return null;
    }
    private Project project;
    private CssIndex index;

    public CssProjectSupport(Project project) throws IOException {
	this.project = project;
	this.index = CssIndex.create(project);
    }

    public CssIndex getIndex() {
	return index;
    }

    public Project getProject() {
	return project;
    }
}
