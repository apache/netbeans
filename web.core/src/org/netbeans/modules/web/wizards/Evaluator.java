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
package org.netbeans.modules.web.wizards;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;

/**
 * Generic methods for evaluating the input into the wizards.
 *
 * @author Ana von Klopp
 */
abstract class Evaluator {

    private static final Logger LOG = Logger.getLogger(Evaluator.class.getName());
    private FileType fileType = null;

    Evaluator(FileType fileType) {
        this.fileType = fileType;
    }

    abstract boolean isValid();

    abstract String getTargetPath();

    abstract String getErrorMessage();

    abstract Iterator getPathItems();

    abstract void setInitialFolder(DataFolder df, Project project);

    FileType getFileType() {
        return fileType;
    }

    /**
     * Returns the absolute path to the target class. Used by the
     * wizards to display the result of the selections. 
     */
    String getTargetPath(Iterator pathItems) {
        StringBuffer buffer = new StringBuffer();
        while (pathItems.hasNext()) {
            buffer.append((String) (pathItems.next()));
            if (pathItems.hasNext()) {
                buffer.append(File.separator);
            }
        }
        buffer.append("."); //NOI18N
        buffer.append(fileType.getSuffix());
        return buffer.toString();
    }

    void checkFile(Iterator pathItems, FileObject root) throws IOException {
        LOG.finer("checkFile() " + root); //NOI18N

        String pathItem;
        FileObject fo = root;

        while (pathItems.hasNext()) {
            pathItem = (String) (pathItems.next());
            LOG.finer("path item is " + pathItem); //NOI18N

            // Path item is a directory, check that we can get it
            if (pathItems.hasNext()) {
                LOG.finer("Not the last one"); //NOI18N
                try {
                    fo = fo.getFileObject(pathItem, null);
                } catch (IllegalArgumentException iaex) {
                    throw new IOException(NbBundle.getMessage(Evaluator.class, "MSG_clash_path", pathItem));
                }
                if (fo == null) {
                    return;
                }

                if (!fo.isFolder()) {
                    LOG.finer("Not a folder"); //NOI18N
                    throw new IOException(NbBundle.getMessage(Evaluator.class, "MSG_clash_path", pathItem));
                }
            } else {
                LOG.finer("This is the last one"); //NOI18N
                try {
                    fo = fo.getFileObject(pathItem, fileType.getSuffix());
                } catch (IllegalArgumentException iaex) {
                    throw new IOException(NbBundle.getMessage(Evaluator.class, "MSG_clash_path", pathItem));
                }
                if (fo == null) {
                    return;
                }
                if (fo.isData()) {
                    throw new IOException(NbBundle.getMessage(Evaluator.class, "MSG_file_exists", pathItem));
                }
            }
        }
        LOG.finer("checkFile() passed"); //NOI18N
    }
}

