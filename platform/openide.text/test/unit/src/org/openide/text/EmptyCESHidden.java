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


package org.openide.text;


import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.util.Date;

import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditor;
import org.openide.text.Line;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;


/**
 * Empty implementstion of <code>CloneableEditorSupport</code>.
 * Helper test class. Is used by regression test TextTest to reproduce
 * deadlock from bug #10449.
 *
 * @author  Marek Slama, Yarda Tulach
 */
public class EmptyCESHidden extends CloneableEditorSupport {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private Line.Set pls;

    public EmptyCESHidden(CloneableEditorSupport.Env env) {
        super (env);
        pls = getLineSet();
    }
        
    /** A method to create a new component. Must be overridden in subclasses.
     * @return the cloneable top component for this support
    */
    protected CloneableTopComponent createCloneableTopComponent() {
        // initializes the document if not initialized
        prepareDocument ();
        
        Thread tstThread = new Thread
        (new Runnable() {
            public void run() {
                synchronized (new java.awt.Panel().getTreeLock()) {
                System.out.println(System.currentTimeMillis() + " Thread runs");
                pls.getOriginal(0);
                System.out.println(System.currentTimeMillis() + " Thread finish");
                }
            }
        },"Test"
        );
        
        tstThread.start();

        System.out.println(System.currentTimeMillis() + " Main sleeping");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        System.out.println(System.currentTimeMillis() + " Main wakeuped");
        
        CloneableEditor ed = createCloneableEditor ();
        initializeCloneableEditor (ed);
        return ed;
    }
    
    /** Message to display when an object is being opened.
     * @return the message or null if nothing should be displayed
    */
    protected String messageOpening() {
        return "Test Opening";
    }
    
    /** Message to display when an object has been opened.
     * @return the message or null if nothing should be displayed
    */
    protected String messageOpened() {
        return "Test Opened";
    }
    
    /** Constructs message that should be used to name the editor component.
     *
     * @return name of the editor
 */
    protected String messageName() {
        return "Test Name";
    }
    
    /** Constructs message that should be displayed when the data object
     * is modified and is being closed.
     *
     * @return text to show to the user
 */
    protected String messageSave() {
        return "Test Save";
    }
    
    /** Text to use as tooltip for component.
     *
     * @return text to show to the user
 */
    protected String messageToolTip() {
        return "Test Tool Tip";
    }
    
    public static class Env implements CloneableEditorSupport.Env {
        private Date creation = new Date();

        private StringBufferInputStream input;
        private FileOutputStream outFile;
        private CloneableOpenSupport tstInst;
        
        public void setInstance(CloneableOpenSupport tst) {
            tstInst = tst;
        }
        
        /** Support for marking the environement modified.
         * @exception IOException if the environment cannot be marked modified
         *   (for example when the file is readonly), when such exception
         *   is the support should discard all previous changes
 */
        public void markModified() throws java.io.IOException {
        }
        
        /** Reverse method that can be called to make the environment 
         * unmodified.
 */
        public void unmarkModified() {
        }
        
        /** Removes property listener.
 */
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        /** Test whether the object is modified or not.
         * @return true if the object is modified
 */
        public boolean isModified() {
            return false;
        }
        
        /** Removes veto listener.
         */
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }
        
        /** Test whether the support is in valid state or not.
         * It could be invalid after deserialization when the object it
         * referenced to does not exist anymore.
         *
         * @return true or false depending on its state
         */
        public boolean isValid() {
            return true;
        }
        
        /** Adds veto listener.
        */
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }
        
        /** Adds property listener.
        */
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        /** Method that allows environment to find its 
         * cloneable open support.
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return tstInst;
        }
        
        /** The time when the data has been modified
         */
        public Date getTime() {
            return creation;
        }
        
        /** Obtains the output stream.
         * @exception IOException if an I/O error occures
         */
        public OutputStream outputStream() throws IOException {
            if (outFile == null) {
                outFile = new FileOutputStream("outFile");
            }
            return outFile;
        }
        
        /** Mime type of the document.
         * @return the mime type to use for the document
         */
        public String getMimeType() {
            return "text/plain";
        }
        
        /** Obtains the input stream.
         * @exception IOException if an I/O error occures
         */
        public InputStream inputStream() throws IOException {
            if (input == null) {
                input = new StringBufferInputStream("Test text");
            }
            return input;
        }
        
    }
}
