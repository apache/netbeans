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

package org.netbeans.modules.lexer.gen.javacc;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.lexer.gen.DescriptionReader;
import org.netbeans.modules.lexer.gen.LanguageGenerator;
import org.netbeans.modules.lexer.gen.LanguageData;
import org.netbeans.modules.lexer.gen.util.LexerGenUtilities;
import org.xml.sax.SAXException;

/**
 * Language class generator for javacc generated lexers.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaCCLanguageGenerator extends LanguageGenerator {

    protected void appendClassStart(StringBuffer sb, LanguageData data) {
        LexerGenUtilities.appendSpaces(sb, 4);
        sb.append("/** Maximum lexer state determined from xxxConstants class. */\n");
        LexerGenUtilities.appendSpaces(sb, 4);
        sb.append("static final int MAX_STATE = ");
        JavaCCTokenTypes jcctt = (JavaCCTokenTypes)data.getTokenTypes();
        sb.append(jcctt.getMaxState());
        sb.append(";\n\n");
        
        super.appendClassStart(sb, data);
    }
    
    public String generate(String langClassName, String lexerClassName,
    String tokenTypesClassName, File xmlLangDescFile)
    throws ClassNotFoundException, SAXException, IOException {

        LanguageData data = new LanguageData();
        data.setLanguageClassName(langClassName);
        data.setLexerClassName(lexerClassName);

        // Apply token constants class info
        if (tokenTypesClassName != null) {
            Class tokenTypesClass = Class.forName(tokenTypesClassName);
            JavaCCTokenTypes tokenTypes = new JavaCCTokenTypes(tokenTypesClass);
            data.registerTokenTypes(tokenTypes);
        }

        // Apply possible xml description
        if (xmlLangDescFile != null) {
            DescriptionReader xmlLangDesc = new DescriptionReader(
                xmlLangDescFile.getAbsolutePath());

            xmlLangDesc.applyTo(data);

        }

        // Update int ids that do not have counterparts in token types
        data.updateUnassignedIntIds();

        return createSource(data);
    }

}

