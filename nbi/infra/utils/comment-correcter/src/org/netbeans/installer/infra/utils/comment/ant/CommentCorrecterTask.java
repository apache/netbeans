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

package org.netbeans.installer.infra.utils.comment.ant;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.netbeans.installer.infra.utils.comment.CommentCorrecter;
import org.netbeans.installer.infra.utils.comment.handlers.FileHandler;
import org.netbeans.installer.infra.utils.comment.utils.Utils;

/**
 * This is the apache ant interface of the initial comment correcting utility.
 *
 * <p>
 * The ant task defined in this class supports two arguments: <code>text</code> -
 * its value should be the path to the file which contains the desired intiial
 * comment text; 'lineLength' - a positive integer defining the requried line length
 * for the comment (optional).
 *
 * <p>
 * The task supports the nested <code>&lt;fileset&gt;</code> elements which should be
 * used to define the set of files for which the correction should take place.
 *
 * @author Kirill Sorokin
 */
public final class CommentCorrecterTask extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The file with the desired initial comment text.
     */
    private File text;
    
    /**
     * The desired line length for the comment.
     */
    private int lineLength;
    
    /**
     * List of <code>FileSet</code> which define the set of files for which the
     * comment correction should take place.
     */
    private List<FileSet> filesets;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * The default and only constructor for the ant task. It only sets the defaults
     * values for the class fields.
     */
    public CommentCorrecterTask() {
        lineLength = CommentCorrecter.DEFAULT_LINE_LENGTH;
        filesets = new LinkedList<FileSet>();
    }
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the <code>text</code> property.
     *
     * @param path New value for the <code>text</code> property.
     */
    public void setText(String path) {
        text = new File(path).getAbsoluteFile();
    }
    
    /**
     * Setter for the <code>lineLength</code> property.
     *
     * @param string New value for the <code>text</code> property as a string - it
     *      will be converted to an integer.
     */
    public void setLineLength(String string) {
        try {
            lineLength = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            log(e.getMessage());
        }
    }
    
    /**
     * Adds a new <code>FileSet</code> to the list. The list of file sets will be
     * used to perform the comment correction.
     *
     * @param fileset The file set to add to the list.
     */
    public void addFileSet(FileSet fileset) {
        filesets.add(fileset);
    }
    
    /**
     * Executes the ant task, performing initial comment correction on the set of
     * files defined in <code>filesets</code>. Overrides the <code>execute()</code>
     * method in the <code>Task</code> class.
     *
     * @throws org.apache.tools.ant.BuildException if parameters validation fails or
     *      an I/O error occurs
     */
    @Override
    public void execute() throws BuildException {
        if (text == null) {
            throw new BuildException(
                    "The 'text' attribute is required."); // NOI18N
        }
        
        try {
            final String comment = Utils.readFile(text);
            
            for (FileSet fileset: filesets) {
                final DirectoryScanner scanner =
                        fileset.getDirectoryScanner(getProject());
                
                for (String filename: scanner.getIncludedFiles()) {
                    final File file =
                            new File(fileset.getDir(getProject()), filename);
                    
                    log(file.getAbsolutePath());
                    
                    final FileHandler handler =
                            CommentCorrecter.getInstance().getHandler(file);
                    if (handler != null) {
                        handler.load(file);
                        
                        final String current = handler.getCurrentComment();
                        if (current == null) {
                            log("    ...inserting " + // NOI18N
                                    "the initial comment"); // NOI18N
                            handler.insertComment(comment, lineLength);
                        } else {
                            final String correct =
                                    handler.getCorrectComment(comment, lineLength);
                            if (current.equals(correct)) {
                                log("    ...skipping -- " + // NOI18N
                                        "the comment is up to date"); // NOI18N
                            } else {
                                log("    ...updating " + // NOI18N
                                        "the initial comment"); // NOI18N
                                handler.updateComment(comment, lineLength);
                            }
                        }
                        
                        handler.save(file);
                    } else {
                        log("   ...skipping -- " + // NOI18N
                                "not recognized by any handler"); // NOI18N
                    }
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
