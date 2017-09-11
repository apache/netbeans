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
package org.netbeans.modules.csl.spi;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;


/**
 * Simple implementation of the Error interface, which can be used for convenience
 * when generating errors during (for example) program parsing.
 *
 * @author Tor Norbye
 */
public class DefaultError implements Error {
    private String displayName;
    private String description;

    //private List<Fix> fixes;
    private FileObject file;
    private int start;
    private int end;
    private boolean lineError;
    private String key;
    private Severity severity;
    private Object[] parameters;

    public static Error createDefaultError(@NullAllowed String key,
            @NonNull String displayName,
            @NullAllowed String description,
            @NonNull FileObject file,
            @NonNull int start,
            @NonNull int end,
            boolean lineError,
            @NonNull Severity severity) {
        return new DefaultError(key, displayName, description, file, start, end, lineError, severity);
    }

    /** Creates a new instance of DefaultError */
    public DefaultError(
            @NullAllowed String key,
            @NonNull String displayName,
            @NullAllowed String description,
            @NonNull FileObject file,
            @NonNull int start,
            @NonNull int end,
            @NonNull Severity severity) {
        this(key, displayName, description, file, start, end, true, severity);
    }
    
    /** Creates a new instance of DefaultError */
    public DefaultError(
            @NullAllowed String key,
            @NonNull String displayName, 
            @NullAllowed String description,
            @NonNull FileObject file, 
            @NonNull int start, 
            @NonNull int end,
            boolean lineError,
            @NonNull Severity severity) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.file = file;
        this.start = start;
        this.end = end;
        this.lineError = lineError;
        this.severity = severity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    // TODO rename to getStartOffset
    public int getStartPosition() {
        return start;
    }

    // TODO rename to getEndOffset
    public int getEndPosition() {
        return end;
    }

    public boolean isLineError() {
        return lineError;
    }

    @Override
    public String toString() {
        return "DefaultError[" + displayName + ", " + description + ", " + severity + "]";
    }

    public String getKey() {
        return key;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(final Object[] parameters) {
        this.parameters = parameters;
    }

    public Severity getSeverity() {
        return severity;
    }

    public FileObject getFile() {
        return file;
    }

    public void setOffsets(int start, int end) {
        this.start = start;
        this.end = end;
    }
}
