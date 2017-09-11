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

package org.netbeans.modules.java.source.transform;

import org.netbeans.modules.java.source.query.CommentHandler;
import org.openide.util.NbBundle;
import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import java.util.List;
import java.util.logging.*;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.TreeFactory;

/**
 * A Transformer is an Query that modifies the model.  Model transformation
 * is done by a supplied ImmutableTreeTranslator implementation.  A new context
 * is set upon successful completion of this Transformer.
 */
public abstract class Transformer<R, P> extends TreeScanner<R,P> {

    CommentHandler commentHandler;
    public TreeMaker make;
    protected WorkingCopy copy;
    protected String refactoringDescription;
    protected Types types; // used by tests
    private String failureMessage;
    protected ASTService model;

    static final Logger logger = Logger.getLogger("org.netbeans.modules.java.source");

    public void init() {
    }

    /**
     * Initialize and associate this Query instance with the
     * specified QueryEnvironment.
     */
    public void attach(Context context, WorkingCopy copy) {
        make = copy.getTreeMaker();
        types = JavacTypes.instance(context);
        commentHandler = CommentHandlerService.instance(context);
        model = ASTService.instance(context);
        this.copy = copy;
    }

    /**
     * Release any instance data created during attach() invocation.  This
     * is necessary because the Java reflection support may cache created
     * instances, preventing the session data from being garbage-collected.
     */
    public void release() {
        //changes.release();  // enable when async results are supported
        //result.release()
        make = null;
        types = null;
        this.copy = null;
    }

    public void destroy() {}
    
    public String getRefactoringDescription() {
        return refactoringDescription != null ? refactoringDescription : "Unnamed Refactoring";
    }

    public void setRefactoringDescription(String description) {
        refactoringDescription = description;
    }

    public void apply(Tree t) {
        t.accept(this, null);
    }

    String getString(String key) {
        return NbBundle.getBundle(Transformer.class).getString(key); //NOI18N
    }

    /**
     * True if no translation failures occurred.
     */
    protected boolean translationSuccessful() {
        return failureMessage == null;
    }

    public final void copyCommentTo(Tree from, Tree to) {
        if (from != null && to != null) {
            commentHandler.copyComments(from, to);
        }
    }
}
