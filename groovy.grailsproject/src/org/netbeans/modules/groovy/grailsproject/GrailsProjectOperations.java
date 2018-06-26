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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author schmidtm
 */
public class GrailsProjectOperations implements DeleteOperationImplementation {

    private final GrailsProject project;

    public GrailsProjectOperations(GrailsProject project) {
        this.project = project;
    }

    public void notifyDeleting() throws IOException {
        return;
    }

    public void notifyDeleted() throws IOException {
        project.getProjectState().notifyDeleted();
    }
    
    public List<FileObject> getMetadataFiles() {
        // we don't write any metadata, if some will be added in future, add them here
        return Collections.emptyList();
    }

    public List<FileObject> getDataFiles() {
        return Arrays.asList(project.getProjectDirectory().getChildren());
    }

}
