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

package org.netbeans.modules.html.editor.lib.api;

import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;

/**
 * Usefull for providing an html source code version for templating languages typically
 * embedding pieces of html source code w/o html doctype declaration.
 *
 * An implementation of this class should be registered into the global lookup.
 * Register your instance with an appropriate layer position!!!
 *
 * Implementations wishing to do the resolution on per project basis can use
 * source.getSourceFileObject() and then FileObjectQuery to get the project.
 *
 * @author marekfukala
 */
public interface HtmlSourceVersionController {

    /**
     * Returns a version of the html source code.
     *
     * @param detectedVersion Detected version of the html code. If your controller doesn't
     * want to adjust the version of the given source just return null so other controllers
     * can be possibly queried. Any non-null value will stop the query and use the returned
     * value.
     *
     * The argument can be null if no version is detected.
     *
     * @param analyzerResult The html source code
     *
     */
    public HtmlVersion getSourceCodeVersion(SyntaxAnalyzerResult analyzerResult, HtmlVersion detectedVersion);

}
