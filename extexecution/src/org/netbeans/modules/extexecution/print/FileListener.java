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
package org.netbeans.modules.extexecution.print;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.spi.extexecution.open.FileOpenHandler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * An OutputProcessor takes filename and line information
 * and produces hyperlinks. Actually resolving filenames
 * into real FileObjects is done lazily via user-supplied
 * FileLocators when the links are actually clicked.
 *
 * @author Tor Norbye, Petr Hejl
 */
public class FileListener implements OutputListener {

    private static final Logger LOGGER = Logger.getLogger(FileListener.class.getName());

    private final String file;

    private final int lineno;

    private final FileLocator fileLocator;

    private final FileOpenHandler handler;

    public FileListener(String file, int line, FileLocator fileLocator,
            FileOpenHandler handler) {

        if (line < 0) {
            line = 0;
        }

        // TODO : columns?
        this.file = file;
        this.lineno = line;
        this.fileLocator = fileLocator;
        this.handler = handler;
    }

    public void outputLineSelected(OutputEvent ev) {

    }

    public void outputLineAction(OutputEvent ev) {
        // Find file such and such and warp to it
        FileObject fo = findFile(file);

        if (fo != null) {
            handler.open(fo, lineno);
        }
    }

    public void outputLineCleared(OutputEvent ev) {
    }

    private FileObject findFile(final String path) {
        if (fileLocator != null) {
            FileObject fo = fileLocator.find(path);
            if (fo != null) {
                return fo;
            }
        }

        // Perhaps it's an absolute path of some sort... try to resolve those
        // Absolute path? Happens for stack traces in libraries and such
        File realFile  = new File(path);
        if (realFile.isFile()) {
            return FileUtil.toFileObject(FileUtil.normalizeFile(realFile));
        } else {
            LOGGER.warning("Cannot resolve file for \"" + path + "\" path.");
            return null;
        }
    }
}
