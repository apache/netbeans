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
