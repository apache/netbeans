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

package org.netbeans.lexer.gen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Abstract class for supporting a language source generation.
 *
 * @author Miloslav Metelka
 */
public class StringReplace extends Task {

    private String replaceWhat;

    private String replaceWith;

    private String replaceIn;

    private String property;

    public StringReplace() {
    }

    public String getReplaceWhat() {
        return replaceWhat;
    }
    
    public void setReplaceWhat(String replaceWhat) {
        this.replaceWhat = replaceWhat;
    }
    
    public String getReplaceWith() {
        return replaceWith;
    }
    
    public void setReplaceWith(String replaceWith) {
        this.replaceWith = replaceWith;
    }
    
    public String getReplaceIn() {
        return replaceIn;
    }
    
    public void setReplaceIn(String replaceIn) {
        this.replaceIn = replaceIn;
    }

    public String getProperty() {
        return property;
    }
    
    public void setProperty(String property) {
        this.property = property;
    }

    public void execute() throws BuildException {
        if (getReplaceWhat() == null || "".equals(getReplaceWhat())) {
            throw new BuildException("replaceWhat attribute must be set");
        }
        if (getProperty() == null || "".equals(getProperty())) {
            throw new BuildException("property attribute must be set");
        }

        String output = getReplaceIn();
        int startIndex = 0;
        while (true) {
            int foundIndex = output.indexOf(getReplaceWhat(), startIndex);
            if (foundIndex == -1) {
                break;
            }

            output = output.substring(0, foundIndex)
                + getReplaceWith()
                + output.substring(foundIndex + getReplaceWhat().length());

            foundIndex += getReplaceWith().length();
        }
        
        // Set the target property
        getOwningTarget().getProject().setProperty(getProperty(), output);
    }
    
}
