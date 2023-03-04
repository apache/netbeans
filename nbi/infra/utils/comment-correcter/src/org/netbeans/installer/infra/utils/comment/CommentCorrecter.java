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

package org.netbeans.installer.infra.utils.comment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.infra.utils.comment.handlers.FileHandler;
import org.netbeans.installer.infra.utils.comment.handlers.HtmlFileHandler;
import org.netbeans.installer.infra.utils.comment.handlers.JspFileHandler;
import org.netbeans.installer.infra.utils.comment.handlers.MakefileFileHandler;
import org.netbeans.installer.infra.utils.comment.handlers.PropertiesFileHandler;
import org.netbeans.installer.infra.utils.comment.handlers.ShellFileHandler;
import org.netbeans.installer.infra.utils.comment.handlers.SourcesFileHandler;
import org.netbeans.installer.infra.utils.comment.handlers.XmlFileHandler;
import org.netbeans.installer.infra.utils.comment.utils.Utils;

/**
 * This is the main class for the initial comment correcting utility.
 *
 * <p>
 * It contains the <code>main</code> method which allows calling the utility from
 * the command line.
 *
 * <p>
 * This class is a singleton. Programmatic clients are expected to call its
 * {@link #getInstance()} method to obtain an instance of the class. After an
 * instance of the class has been obtained it should be initialized with the desired
 * initial comment text and line length.
 *
 * <p>
 * Optionally the clients may want to insert additional file type handlers via the
 * {@link #addHandler(FileHandler)} method.
 *
 * <p>
 * After the class has been intialized, the client should call the
 * {@link #updateFile(File)} method which would perform the comment update procedure
 * on the given file (or recurse into the directory structure if the supplied file
 * is a directory).
 *
 * @author Kirill Sorokin
 */
public final class CommentCorrecter {
    /////////////////////////////////////////////////////////////////////////////////
    // Main
    /**
     * Allows the initial comment correcting utility to be called from the command
     * line.
     *
     * <p>
     * The method expects two command-line parameters to be passed in: path to the
     * file for which the initial comment should be corrected (if the supplied path
     * points to a directory, it will be traversed) and path to the file which
     * contains the desired text of the initial comment.
     *
     * <p>
     * An optional third parameter can also be supplied - desired line length for
     * the intial comment. The default value for it is 85 characters. If the supplied
     * parameter is invalid - the default value will be used.
     *
     * @param args Command-line parameters.
     * @throws java.io.IOException if an I/O errors occurs during the update
     *      procedure.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {                                              // NOMAGI
            System.out.println(
                    "Wrong number of input parameters."); // NOI18N
            System.out.println(
                    "Usage:"); // NOI18N
            System.out.println(
                    "    java -jar comment-correcter.jar " + // NOI18N
                    "<file> <text> [length]"); // NOI18N
            System.out.println(
                    "        * <file> is a path to the directory/file on"); // NOI18N
            System.out.println(
                    "          which ot perform the correction"); // NOI18N
            System.out.println(
                    "        * <text> is a path to the file " + // NOI18N
                    "which contains the"); // NOI18N
            System.out.println(
                    "        * text of the comment"); // NOI18N
            System.out.println(
                    "        * [length] is the desired line length " + // NOI18N
                    "for the text"); // NOI18N
            System.exit(0);
        }
        
        final File file = new File(args[0]);                                // NOMAGI
        final File text = new File(args[1]);                                // NOMAGI
        
        if (args.length > 2) {                                              // NOMAGI
            getInstance().setLineLength(Integer.parseInt(args[2]));         // NOMAGI
        } else {
            getInstance().setLineLength(DEFAULT_LINE_LENGTH);
        }
        
        getInstance().setText(Utils.readFile(text));
        getInstance().updateFile(file);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    /**
     * The only instance of the {@link CommentCorrecter} class.
     */
    private static CommentCorrecter instance;
    
    /**
     * Returns the only instance of the class.
     *
     * @return The instance of the {@link CommentCorrecter} class.
     */
    public static synchronized CommentCorrecter getInstance() {
        if (instance == null) {
            instance = new CommentCorrecter();
            
            instance.addHandler(new SourcesFileHandler());
            instance.addHandler(new XmlFileHandler());
            instance.addHandler(new PropertiesFileHandler());
            instance.addHandler(new ShellFileHandler());
            instance.addHandler(new MakefileFileHandler());
            instance.addHandler(new HtmlFileHandler());
            instance.addHandler(new JspFileHandler());
        }
        
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * List of the registered file type handlers. One of these will be picked to
     * perform the actual update procedure.
     */
    private List<FileHandler> handlers;
    
    /**
     * The desired initial comment line length. Defaults to 85 characters.
     */
    private int lineLength;
    
    /**
     * The desired text of the initial comment.
     */
    private String text;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * Constructs a new instance of {@link CommentCorrecter}. The functionality of
     * the constructor is limited to intializing the instance fields to default
     * values.
     */
    private CommentCorrecter() {
        handlers = new LinkedList<FileHandler>();
        lineLength = DEFAULT_LINE_LENGTH;
    }
    
    // public ///////////////////////////////////////////////////////////////////////
    /**
     * Registers a new file type handler.
     *
     * @param handler A file type handler instance which should be registered in
     *      the comment corrceting utility.
     * @throws java.lang.IllegalArgumentException if the parameter is null.
     */
    public void addHandler(final FileHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "The 'handler' parameter cannot be null."); // NOI18N
        }
        handlers.add(handler);
    }
    
    /**
     * Sets the desired line length for the initial comment.
     *
     * @param lineLength The desired line length.
     * @throws java.lang.IllegalArgumentException if the parameter is not a positive 
     *      integer.
     */
    public void setLineLength(final int lineLength) {
        if (lineLength <= 0) {
            throw new IllegalArgumentException(
                    "The 'lineLength' parameter must be positive."); // NOI18N
        }
        this.lineLength = lineLength;
    }
    
    /**
     * Sets the desired text of the initial comment.
     *
     * @param text The desired text of the initial comment.
     * @throws java.lang.IllegalArgumentException if the parameter is null.
     */
    public void setText(final String text) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "The 'text' parameter cannot be null."); // NOI18N
        }
        this.text = text;
    }
    
    /**
     * Performs the initial comment update operation on the given file. If the file
     * is a directory, its children will be parsed instead.
     *
     * <p>
     * This method is CLI-interactive - for each parsed file a query will be
     * displayed requiring the user to tell the utility what to do with the file.
     * The available options are: insert the new initial comment, update the current
     * initial comment (if it exists), skip the file.
     *
     * @param file The file for which the comment update procedure should be
     *      performed.
     * @throws java.io.IOException if an I/O error occurs of there are problems
     *      with validating the supplied file.
     * @throws java.lang.IllegalArgumentException if the parameter is null.
     * @throws java.lang.IllegalStateException if the class has not been 
     *      initialized, i.e. the <code>text</code> property is null.
     */
    public void updateFile(final File file) throws IOException {
        // basic validation
        if (file == null) {
            throw new IllegalArgumentException(
                    "The 'file' parameter cannot be null."); // NOI18N
        }
        if (text == null) {
            throw new IllegalStateException(
                    "The 'text' property has not been initialized."); // NOI18N
        }
        
        // file validation
        if (!file.exists()) {
            throw new IOException(
                    "The given file '" + file + // NOI18N
                    "' does not exist."); // NOI18N
        }
        
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            
            if (children != null) {
                for (File child: children) {
                    updateFile(child);
                }
            }
        } else {
            System.out.println(file.getAbsolutePath());
            
            final FileHandler handler = getHandler(file);
            if (handler != null) {
                handler.load(file);
                
                final String comment = handler.getCurrentComment();
                
                System.out.println("current initial comment:"); // NOI18N
                System.out.println();
                System.out.println(
                        comment != null ? comment : "<none>"); // NOI18N
                System.out.println();
                
                System.out.print(
                        "Insert (I), Update (U), Skip? [Skip]: "); // NOI18N
                
                final String input = new BufferedReader(
                        new InputStreamReader(System.in)).readLine();
                
                if (input.startsWith("I") || // NOI18N
                        input.startsWith("i")) { // NOI18N
                    handler.insertComment(text, lineLength);
                    handler.save(file);
                }
                
                if (input.startsWith("U") || // NOI18N
                        input.startsWith("u")) { // NOI18N
                    handler.updateComment(text, lineLength);
                    handler.save(file);
                }
                
                System.out.println();
            } else {
                System.out.println(
                        "   ...not recognized by any handler - skipping"); // NOI18N
                System.out.println();
            }
        }
    }
    
    /**
     * Finds the appropriate file handler for the given file. This method iterates
     * over the list of registered handlers and returns the first which agrees to
     * work with the file.
     *
     * @param file The file for which to find a handler.
     * @return An instance {@link FileHandler} which is capable of working with the
     *      given file, or <code>null</code> if such a handler was not found.
     * @throws java.lang.IllegalArgumentException if the parameter is null.
     */
    public FileHandler getHandler(final File file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "The 'file' parameter cannot be null."); // NOI18N
        }
        
        for (FileHandler handler: handlers) {
            if (handler.accept(file)) {
                return handler;
            }
        }
        
        return null;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * The default line length for the initial comment.
     */
    public static final int DEFAULT_LINE_LENGTH = 85;
}
