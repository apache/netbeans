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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser;

import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@org.netbeans.api.annotations.common.SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class GSFPHPError implements org.netbeans.modules.csl.api.Error.Badging {
    private static final boolean SILENT_ERROR_BADGE =  Boolean.getBoolean("nb.php.silent.error.badge"); //NOI18N
    private final String displayName;
    private final FileObject file;
    private final int startPosition;
    private final int endPosition;
    private final Severity severity;
    private final Object[] parameters;

    public GSFPHPError(String displayName, FileObject file, int startPosition, int endPosition, Severity severity, Object[] parameters) {
        this.displayName = displayName;
        this.file = file;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.severity = severity;
        this.parameters = parameters;
    }


    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getKey() {
        return "[" + startPosition + "," + endPosition + "]-" + displayName;
    }

    @Override
    public FileObject getFile() {
        return this.file;
    }

    @Override
    public int getStartPosition() {
        return this.startPosition;
    }

    @Override
    public int getEndPosition() {
        return this.endPosition;
    }

    @Override
    public Severity getSeverity() {
        return this.severity;
    }

    @Override
    public Object[] getParameters() {
        return this.parameters;
    }

    @Override
    public boolean isLineError() {
        return true;
    }

    @Override
    public boolean showExplorerBadge() {
        return !SILENT_ERROR_BADGE;
    }
}
