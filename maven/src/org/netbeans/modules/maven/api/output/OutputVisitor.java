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

package org.netbeans.modules.maven.api.output;

import java.awt.Color;
import javax.swing.Action;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;

/**
 * Is collecting line parsing information from all the registered Outputprocessors.
 * @author  Milos Kleint
 */
public final class OutputVisitor {
    
    private OutputListener outputListener;
    private Action successAction;
    private boolean important;
    private String line;
    private boolean skipLine = false;
    private IOColors.OutputType outputType;
    private Color color;
    private Context context;
    
    /**
     * property for success Action. Holds question text.
     */
    public static final String ACTION_QUESTION = "Question"; //NOI18N
    /**
     * property for success Action. Priority of the action.
     * From all collected actions one is used (the one with highest
     * priority).
     */
    public static final String ACTION_PRIORITY = "Priority"; //NOI18N
    
    /** Creates a new instance of OutputVisitor */
    public OutputVisitor() {
    }
    
    /** Creates a new instance of OutputVisitor */
    public OutputVisitor(Context context) {
        this.context = context;
    }

    /**
     * not to be called by the OutputProcessors.
     */
    public void resetVisitor() {
        outputListener = null;
        successAction = null;
        important = false;
        line = null;
        skipLine = false;
        color = null;
        outputType = null;
    }
    
    public OutputListener getOutputListener() {
        return outputListener;
    }

    /**
     * add output line highlight and hyperlink via 
     * <code>org.openide.windows.OutputListener</code> instance.
     */
    public void setOutputListener(OutputListener listener) {
        outputListener = listener;
    }
    /**
     * add output line highlight and hyperlink via 
     * <code>org.openide.windows.OutputListener</code> instance.
     * @param isImportant mark the line as important (useful in Nb 4.1 only)
     */
    public void setOutputListener(OutputListener listener, boolean isImportant) {
        setOutputListener(listener);
        important = isImportant;
    }
    
    /**
     * at least one of the <code>OutputProcessor</code>s added a <code>OutputListener</code> and
     * marked it as important.
     */
    public boolean isImportant() {
        return important;
    }

    public Action getSuccessAction() {
        return successAction;
    }

    /**
     * add an action that should be performed when the build finishes.
     * Only one action will be performed, if more than one success actions are 
     * collected during processing, the one with highest value of property
     * ACTION_PRIORITY is performed. 
     * Another property used is ACTION_QUESTION which 
     * holds text for Yes/No question. If user confirms, it's performed.
     */
    public void setSuccessAction(Action sAction) {
        successAction = sAction;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public void skipLine() {
        skipLine = true;
    }
    
    public boolean isLineSkipped() {
        return skipLine;
    }

    /**
     * Get the color. If the output type was set using
     * {@link #setOutputType(org.openide.windows.IOColors.OutputType)}, try to
     * resolve the actual color. If the output type was not set, or the actual
     * color cannot be resolved, return value that was set using
     * {@link #setColor(java.awt.Color)}; supported.
     *
     * @since maven/2.78
     */
    public Color getColor(InputOutput io) {
        Color c = this.outputType == null
                ? null
                : IOColors.getColor(io, this.outputType);
        return c == null
                ? getColor()
                : c;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Set output type that will be used in method
     * {@link #getColor(org.openide.windows.InputOutput)}.
     *
     * @since maven/2.78
     */
    public void setOutputType(IOColors.OutputType outputType) {
        this.outputType = outputType;
    }

    public @CheckForNull Context getContext() {
        return context;
    }

    public static interface Context {

        @CheckForNull Project getCurrentProject();
        
    }
        
}
