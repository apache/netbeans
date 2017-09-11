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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * Information about the current context a rule is being asked to evaluate.
 * 
 * @author Tor Norbye
 */
public class RuleContext {

    public RuleContext() {
    }

    /** The HintsManager associated with this rule context */
    @NonNull
    public HintsManager manager;
    
    /** The current caret offset (for caret-based rules), or -1 */
    public int caretOffset = -1;
    
    /** The start of the current selection (if any) or -1 */
    public int selectionStart = -1;
    
    /** The end of the current selection (if any) or -1 */
    public int selectionEnd;
    
    /** The CompilationInfo corresponding to this rule context */
    @NonNull
    public ParserResult parserResult;
    
    /** The document */
    @NonNull
    public BaseDocument doc;
    
// XXX: parsingapi
//    /** All the embedded parser results for this compilation info */
//    @NonNull
//    public Collection<? extends ParserResult> parserResults;
//
//    /** The FIRST parser result (if parserResults.size() > 0) or null */
//    @CheckForNull
//    public ParserResult parserResult;

    // Fields useful for subclasses
    // TODO - push into subclasses?
    public int lexOffset;
    public int astOffset;
}
