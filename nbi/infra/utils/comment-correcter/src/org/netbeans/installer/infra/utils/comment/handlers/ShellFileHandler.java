/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.utils.comment.handlers;

import java.io.File;
import java.util.regex.Pattern;

/**
 * A {@link FileHandler} implementation capable of handling shell scripts.
 *
 * @author Kirill Sorokin
 */
public class ShellFileHandler extends LineFileHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Creates a new instance of {@link ShellFileHandler}. The constuctor 
     * simply falls back to the
     * {@link LineFileHandler#LineFileHandler(Pattern, Pattern, String)} passing in
     * the parameters relevant to shell scripts.
     */
    public ShellFileHandler() {
        super(COMMENT_PATTERN, IGNORE_PATTERN, COMMENT_PREFIX);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean accept(final File file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "The 'file' parameter cannot be null."); // NOI18N
        }
        
        if (!file.isFile()) {
            return false;
        }
        
        return file.getName().endsWith(".sh"); // NOI18N
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * The regular expression pattern which matches the line which is a comment.
     */
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "^\\s*#.*"); // NOI18N
    
    /**
     * The regular expression pattern which matches the line which should be
     * ignored.
     */
    private static final Pattern IGNORE_PATTERN = Pattern.compile(
            "^\\S*#!.*"); // NOI18N
    
    /**
     * The prefix which should be used for each line in the comment.
     */
    private static final String COMMENT_PREFIX = 
            "# "; // NOI18N
}
