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
package org.netbeans.lib.profiler.ui.jdbc;

import java.awt.Color;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
final class SQLFormatter {
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.jdbc.Bundle"); // NOI18N
    private static final String DATABASE_PING = messages.getString("SQLFormatter_DatabasePing"); // NOI18N

    private static final String PING_TEXT = " - <b>"+DATABASE_PING+"</b>";  // NOI18N

    private static String keywords[] = {
        "AS",
        "ALL",
        "AND",
        "ASC",
        "AVG",
        "BY",
        "COUNT",
        "CROSS",
        "DESC",
        "DISTINCT",
        "FROM",
        "FULL",
        "GROUP",
        "HAVING",
        "INNER",
        "LEFT",
        "JOIN",
        "MAX",
        "MIN",
        "NATURAL",
        "NOT",
        "ON",
        "OR",
        "ORDER",
        "OUTER",
        "RIGHT",
        "SELECT",
        "SUM",
        "WHERE",
        "CREATE TABLE",
        "ALTER TABLE",
        "TRUNCATE TABLE",
        "DROP TABLE",
        "INSERT INTO",
        "ALTER SESSION",
        "DELETE",
        "UPDATE",
        "VALUES",
        "SET",
        "'[^']*'"
    };

    private static final Pattern keywordsPattern = Pattern.compile(getPattern(keywords), Pattern.CASE_INSENSITIVE);

    private static String getPattern(String[] patterns) {
        StringBuilder pattern = new StringBuilder();

        for (String patternString : patterns) {
            pattern.append("(");    // NOI18N
            if (Character.isLetter(patternString.charAt(0))) {
                pattern.append("\\b");  // NOI18N
                pattern.append(patternString);
                pattern.append("\\b");  // NOI18N
            } else {
                pattern.append(patternString);
            }
            pattern.append(")|");   // NOI18N
        }
        return pattern.substring(0, pattern.length()-1);
    }

    private static final String pingSQL[] = {
        "^SELECT\\s+1",
        "^VALUES\\s*\\(\\s*1\\s*\\)"
    };

    private static final Pattern pingSQLPattern = Pattern.compile(getPattern(pingSQL), Pattern.CASE_INSENSITIVE);


    static String format(String command) {
        String formattedCommand;
        StringBuilder s = new StringBuilder();
        int offset = 0;
        Matcher m;
        
        command = htmlize(command);
        m = keywordsPattern.matcher(command);
        s.append("<html>"); // NOI18N
        while(m.find()) {
            String kw = m.group();
            s.append(command.substring(offset, m.start()));
            if (kw.startsWith("'")) {       // NOI18N
                // string literal
                s.append(kw);
            } else {
                s.append("<b>");    // NOI18N
                s.append(kw);
                s.append("</b>");   // NOI18N
            }
            offset = m.end();
        }
        s.append(command.substring(offset));
        s.append(checkPingSQL(command));
        s.append("</html>"); // NOI18N

        formattedCommand = s.toString();
        formattedCommand = formattedCommand.replace(")", ")</font>");   // NOI18N
        formattedCommand = formattedCommand.replace("(", "<font color='" + getGrayHTMLString() + "'>(");   // NOI18N
        return formattedCommand;
    }

    private static String htmlize(String value) {
        return value.replace(">", "&gt;").replace("<", "&lt;");     // NOI18N
    }
    
    private static String checkPingSQL(String command) {                         
        Matcher m = pingSQLPattern.matcher(command);
        if (m.find()) {
            return PING_TEXT;
        }
        return "";
    }
    
    
    private static String grayHTMLString;
    private static String getGrayHTMLString() {
        if (grayHTMLString == null) {
            Color grayColor = UIUtils.getDisabledForeground(new JLabel().getForeground());
            grayHTMLString = "rgb(" + grayColor.getRed() + "," + grayColor.getGreen() + "," + grayColor.getBlue() + ")"; //NOI18N
        }
        return grayHTMLString;
    }
    
}
