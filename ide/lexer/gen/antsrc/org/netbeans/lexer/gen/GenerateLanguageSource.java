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
import org.xml.sax.SAXException;

/**
 * Abstract class for supporting a language source generation.
 *
 * @author Miloslav Metelka
 */
public abstract class GenerateLanguageSource extends Task {

    private String languageClassName;

    private String lexerClassName;

    private String tokenTypesClassName;

    private File languageDescriptionFile;
    
    private File languageSourceFile;
    
    private boolean ignoreTokenTypes;
    
    private boolean ignoreLanguageDescriptionFile;
    
    public GenerateLanguageSource() {
    }

    public String getLanguageClassName() {
        return languageClassName;
    }
    
    public void setLanguageClassName(String languageClassName) {
        this.languageClassName = languageClassName;
    }
    
    public String getLexerClassName() {
        return lexerClassName;
    }
    
    public void setLexerClassName(String lexerClassName) {
        this.lexerClassName = lexerClassName;
    }
    
    public String getTokenTypesClassName() {
        return tokenTypesClassName;
    }
    
    public void setTokenTypesClassName(String tokenTypesClassName) {
        this.tokenTypesClassName = tokenTypesClassName;
    }
    
    public File getLanguageDescriptionFile() {
        return languageDescriptionFile;
    }
    
    public void setLanguageDescriptionFile(File languageDescriptionFile) {
        this.languageDescriptionFile = languageDescriptionFile;
    }

    public File getLanguageSourceFile() {
        return languageSourceFile;
    }
    
    public void setLanguageSourceFile(File languageSourceFile) {
        this.languageSourceFile = languageSourceFile;
    }
    
    public boolean getIgnoreTokenTypes() {
        return ignoreTokenTypes;
    }
    
    public void setIgnoreTokenTypes(boolean ignoreTokenTypes) {
        this.ignoreTokenTypes = ignoreTokenTypes;
    }
    
    public boolean getIgnoreLanguageDescriptionFile() {
        return ignoreLanguageDescriptionFile;
    }
    
    public void setIgnoreLanguageDescriptionFile(boolean ignoreLanguageDescriptionFile) {
        this.ignoreLanguageDescriptionFile = ignoreLanguageDescriptionFile;
    }

    public void execute() throws BuildException {
        String langClassName = getLanguageClassName();
        String lexerClassName = getLexerClassName();
        String tokenTypesClassName = getTokenTypesClassName();
        File langDescFile = getLanguageDescriptionFile();
        File langSrcFile = getLanguageSourceFile();
        
        if (getIgnoreTokenTypes()) {
            tokenTypesClassName = null;
        }
        if (getIgnoreLanguageDescriptionFile()) {
            langDescFile = null;
        }

        if (langClassName == null || "".equals(langClassName)) {
            throw new BuildException("languageClassName attribute must be set");
        }
        if (lexerClassName == null || "".equals(lexerClassName)) {
            throw new BuildException("lexerClassName attribute must be set");
        }
        if (langSrcFile == null) {
            throw new BuildException("languageSourceFile attribute must be set");
        }
        
        // Check if file exists
        if (langDescFile != null && !langDescFile.exists()) {
            langDescFile = null;
        }

        String output;
        try {
            output = generate(langClassName, lexerClassName,
                tokenTypesClassName, langDescFile);
        } catch (SAXException e) {
            e.printStackTrace();
            if (e.getException() != null) {
                System.err.println("Nested exception:");
                e.getException().printStackTrace();
            }
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        } catch (ClassNotFoundException e) {
            throw new BuildException(e);
        }
        
        try {
            FileWriter langSrcWriter = new FileWriter(langSrcFile);
            langSrcWriter.write(output);
            langSrcWriter.close();
        } catch (IOException e) {
            throw new BuildException("IOException occurred", e);
        }
        
        String msg = "Generated language source " + langSrcFile
            + " based on information from ";
        if (tokenTypesClassName != null) {
            msg += "class " + tokenTypesClassName;
        }
        if (langDescFile != null) {
            if (tokenTypesClassName != null) {
                msg += " and ";
            }
            msg += "description in " + langDescFile;
        }

        log(msg);
    }
    
    protected abstract String generate(String langClassName, String lexerClassName,
    String tokenTypesClassName, File langDescFile)
    throws ClassNotFoundException, SAXException, IOException;
            
}
