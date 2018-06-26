/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import java.util.Collection;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsParserError implements FilterableError, Error.Badging {

    private final JsErrorManager.SimpleError error;
    private final FileObject file;
    private final Severity severity;
    private final Object[] parameters;
    private final boolean showExplorerBadge;
    private final boolean showInEditor;
    
    private final Collection<SetFilterAction> enableFilter;
    private final SetFilterAction disableFilter;

    public JsParserError(JsErrorManager.SimpleError error, FileObject file,
            Severity severity, Object[] parameters,
            boolean showExplorerBadge, boolean showInEditor,
            Collection<FilterableError.SetFilterAction> enableFilter, FilterableError.SetFilterAction disableFilter) {

        this.error = error;
        this.file = file;
        this.severity = severity;
        this.parameters = parameters != null ? parameters.clone() : new Object[] {};
        this.showExplorerBadge = showExplorerBadge;
        this.showInEditor = showInEditor;
        this.disableFilter = disableFilter;
        this.enableFilter = enableFilter;
    }

    @Override
    public String getDisplayName() {
        return error.getMessage();
    }

    @Override
    public String getDescription() {
        return error.getMessage();
    }

    @Override
    public String getKey() {
        int position = error.getStartPosition();
        return "[" + position + "," + position + "]-" + error.getMessage();
    }

    @Override
    public FileObject getFile() {
        return file;
    }

    @Override
    public int getStartPosition() {
        return error.getStartPosition();
    }

    @Override
    public int getEndPosition() {
        return error.getEndPosition();
    }

    @Override
    public boolean isLineError() {
        return error.isLineError();
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public boolean showExplorerBadge() {
        return showExplorerBadge;
    }

    public boolean showInEditor() {
        return showInEditor;
    }

    @Override
    public boolean isFiltered() {
        return disableFilter != null;
    }

    @Override
    public Collection<SetFilterAction> getEnableFilterActions() {
        return enableFilter;
    }

    @Override
    public SetFilterAction getDisableFilterAction() {
        return disableFilter;
    }
}
