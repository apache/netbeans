/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
