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
