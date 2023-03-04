/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source.transform;

import org.netbeans.modules.java.source.query.CommentHandler;
import org.openide.util.NbBundle;
import com.sun.source.tree.*;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
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
public abstract class Transformer<R, P> extends ErrorAwareTreeScanner<R,P> {

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
