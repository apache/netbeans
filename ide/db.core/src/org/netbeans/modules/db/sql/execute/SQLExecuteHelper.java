/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.sql.execute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.api.DataView;
import org.netbeans.modules.db.dataview.api.DataViewPageContext;
import org.netbeans.modules.db.sql.history.SQLHistoryEntry;
import org.netbeans.modules.db.sql.history.SQLHistoryManager;

/**
 * Support class for executing SQL statements.
 *
 * @author Andrei Badea
 */
public final class SQLExecuteHelper {

    private static final Logger LOGGER = Logger.getLogger(SQLExecuteHelper.class.getName());

    /**
     * Executes a SQL string, possibly containing multiple statements. Returns the execution
     * result, but only if the string contained a single statement.
     *
     * @param sqlScript the SQL script to execute. If it contains multiple lines
     * they have to be delimited by '\n' characters.
     */
    public static SQLExecutionResults execute(String sqlScript, int startOffset, int endOffset,
            DatabaseConnection conn, SQLExecutionLogger executionLogger) {
        return execute(sqlScript, startOffset, endOffset, conn, executionLogger, DataViewPageContext.DEFAULT_PAGE_SIZE);
    }

    /**
     * Executes a SQL string, possibly containing multiple statements. Returns the execution
     * result, but only if the string contained a single statement.
     *
     * @param sqlScript the SQL script to execute. If it contains multiple lines
     * they have to be delimited by '\n' characters.
     */
    public static SQLExecutionResults execute(String sqlScript, int startOffset, int endOffset, 
            DatabaseConnection conn, SQLExecutionLogger executionLogger, int pageSize) {
        
        boolean cancelled = false;
        
        List<StatementInfo> statements = getStatements(sqlScript, startOffset, endOffset,
                                                       getCompatibility(conn));
        
        List<SQLExecutionResult> results = new ArrayList<>();
        long start = System.currentTimeMillis();
        String url = conn.getDatabaseURL();

        for (StatementInfo info : statements) {

            cancelled = Thread.currentThread().isInterrupted();
            if (cancelled) {
                break;
            }
            
            String sql = info.getSQL();

            LOGGER.log(Level.FINE, "Executing: {0}", sql);
                
            DataView view = DataView.create(conn, sql, pageSize);

            // Save SQL statements executed for the SQLHistoryManager
            SQLHistoryManager.getInstance().saveSQL(new SQLHistoryEntry(url, sql, new Date()));

            SQLExecutionResult result = new SQLExecutionResult(info, view);

            boolean isIllegal = false;
            for (Throwable th : view.getExceptions()) {
                if (th instanceof IllegalStateException) {
                    LOGGER.log(Level.INFO, th.getLocalizedMessage(), th);
                    isIllegal = true;
                    break;
                }
            }
            if (isIllegal) {
                // don't continue any more
                break;
            }

            executionLogger.log(result);

            results.add(result);
        }

        long end = System.currentTimeMillis();
        
        if (!cancelled) {
            executionLogger.finish(end - start);
        } else {
            LOGGER.log(Level.FINE, "Execution cancelled"); // NOI18N
            executionLogger.cancel();
        }
                
        // Persist SQL executed
        SQLHistoryManager.getInstance().save();

        if (!cancelled) {
            return new SQLExecutionResults(results);
        } else {
            return null;
        }
    }
    
    static Compatibility getCompatibility(DatabaseConnection conn) {
        String driverClass = conn.getDriverClass();
        
        if (driverClass.contains("mysql")) { //NOI18N
            return Compatibility.COMPAT_MYSQL;
        }

        if (driverClass.contains("postgresql")) { //NOI18N
            return Compatibility.COMPAT_POSTGERSQL;
        }
        
        return Compatibility.COMPAT_GENERIC;
    }
    
    private static List<StatementInfo> getStatements(String script, int startOffset, int endOffset,
            Compatibility compat) {
        if ((startOffset == 0 && endOffset == script.length()) || (startOffset == endOffset)) {
            // Either the whole script, or the statement at offset startOffset.
            List<StatementInfo> allStatements = split(script, compat);
            if (startOffset == 0 && endOffset == script.length()) {
                return allStatements;
            }
            // Just the statement at offset startOffset.
            StatementInfo foundStatement = findStatementAtOffset(
                    allStatements, startOffset, script);
            return foundStatement == null
                    ? Collections.<StatementInfo>emptyList()
                    : Collections.singletonList(foundStatement);
        } else {
            // Just execute the selected subscript.
            return split(script.substring(startOffset, endOffset), compat);
        }
    }

    static StatementInfo findStatementAtOffset(List<StatementInfo> statements,
            int offset, String script) {

        StatementInfo prev = null;
        for (StatementInfo stmt : statements) {
            if (offset <= stmt.getRawEndOffset()) {
                if (offset >= stmt.getStartOffset()) {
                    return stmt; //directly in the script
                } else if (offset >= stmt.getRawStartOffset()) {
                    if (prev == null) {
                        return stmt;
                    } else {
                        // check whether we are on the same line as the end of
                        // previous statement
                        String between = script.substring(
                                prev.getRawEndOffset(), offset);
                        return between.contains("\n") ? stmt : prev;    //NOI18N
                    }
                }
            }
            prev = stmt;
        }
        return prev;
    }
        
    public static List<StatementInfo> split(String script) {
        return split(script, Compatibility.COMPAT_MYSQL);
    }
    
    static List<StatementInfo> split(String script, Compatibility compat) {
        return new SQLSplitter(script, compat).getStatements();
    }

    private static final class SQLSplitter {
        private final String sql;
        private final List<Integer> newLineOffsets;
        private final Compatibility compat;
        
        private final StringBuilder statement = new StringBuilder();
        private final List<StatementInfo> statements = new ArrayList<>();
        private final Map<Integer,Integer> positionMap = new HashMap<>();
        
        private int pos = 0;
        
        private int lastAddedEndPos = 0;
        private int rawStartOffset = 0;
        private int endOffset = 0;
        private int rawEndOffset = 0;

        private String delimiter = ";"; // NOI18N
        private static final String DELIMITER_TOKEN = "delimiter"; // NOI18N
                
        /**
         * @param sql the SQL string to parse. If it contains multiple lines
         * they have to be delimited by '\n' characters.
         * @param useHashComments True if hash symbol (#) should be used as
         * start of line comment (MySQL supports it).
         */
        public SQLSplitter(String sql, Compatibility compat) {
            assert sql != null;
            this.sql = sql;
            this.newLineOffsets = extractNewLineOffsets();
            this.compat = compat;
            parse();
        }
        
        private void appendSQL(int startPos, int endPos) {
            if(statement.length() == 0) {
                // Skip Whitespace on appending
                for(int i = startPos; i <= endPos && i < sql.length(); i++) {
                    if(Character.isWhitespace(sql.charAt(i))) {
                        startPos++;
                    } else {
                        break;
                    }
                }
                if(startPos >= endPos) {
                    return;
                }
                positionMap.put(0, startPos);
                lastAddedEndPos = 0;
            } else {
                if(startPos != lastAddedEndPos) {
                    positionMap.put(statement.length(), startPos);
                }
            }
            statement.append(sql.substring(startPos, endPos));
            lastAddedEndPos = endPos;
            for(int i = endPos - 1; i >= startPos; i--) {
                if(! Character.isWhitespace(sql.charAt(i))) {
                    this.endOffset = i;
                    break;
                }
            }
        }

        private List<Integer> extractNewLineOffsets() {
            List<Integer> newlines = new ArrayList<>();
            int nextNewLine = this.sql.indexOf('\n');
            while(nextNewLine >= 0) {
                newlines.add(nextNewLine);
                nextNewLine = this.sql.indexOf('\n', nextNewLine + 1);
            }
            return newlines;
        }
        
        private void parse() {
            if (sql.contains("\r")) {
                // the string should not contain these
                LOGGER.log(Level.FINE, "The SQL string contained non-supported \\r characters."); // NOI18N
            }

            rawStartOffset = 0;
            
            while (pos < sql.length()) {
                if(isDelimiter()) {
                    rawEndOffset = pos;
                    addStatement();
                    pos += delimiter.length();
                    rawStartOffset = pos;
                } else if(! (consumeDelimiterStatement() 
                        || consumeCommentString()
                        || consumeQuotedString())) {
                    appendSQL(pos, pos + 1);
                    pos++;
                }
            }

            rawEndOffset = pos;
            addStatement();
        }

        /**
         * Consume comment and check for embedded delimiter statement
         * 
         * <p>Contents is not added to final SQL</p>
         */
        private boolean consumeCommentString() {
            String first = null;
            String firstTwo = null;
            
            if((pos + 1) <= sql.length()) {
                first = sql.substring(pos, pos + 1);
            }
            if((pos + 2) <= sql.length()) {
                firstTwo = sql.substring(pos, pos + 2);
            }
            
            int startPos = pos;
            int startLength = 0;
            String endString = null;
            
            if(firstTwo != null && "/*".equals(firstTwo)) {
                    startLength = 2;
                    endString = "*/";
            } else if (firstTwo != null && "--".equals(firstTwo)) {
                    startLength = 2;
                    endString = "\n";
            } else if (first != null && "#".equals(first)) {
                    startLength = 1;
                    if(compat.isUseHashComments()) {
                        endString = "\n";
                    }
            }
            
            if(endString == null) {
                return false;
            }
            
            int endCandidate = sql.indexOf(endString, pos + startLength);
            if (endCandidate == -1) {
                pos = sql.length();
            } else {
                pos = endCandidate + endString.length();
            }
            
            checkForDelimiterStmt(startPos + startLength, pos - endString.length() - 1);
            
            return true;
        }
        
        /**
         * Consume quoted Strings.
         * 
         * <p>Contents is added to extracted SQL</p>
         */
        private boolean consumeQuotedString() {
            String ch = sql.substring(pos, pos + 1);
            
            int startPos = pos;
            String endString = null;
            int quoteLength = 0;
            boolean doubleEscapePossible = false;
                    
            switch(ch) {
                case SQL99_STRING_QUOTE:
                    quoteLength = SQL99_STRING_QUOTE.length();
                    endString = SQL99_STRING_QUOTE;
                    doubleEscapePossible = true;
                    break;
                case SQL99_IDENTIFIER_QUOTE:
                    quoteLength = SQL99_IDENTIFIER_QUOTE.length();
                    endString = SQL99_IDENTIFIER_QUOTE;
                    doubleEscapePossible = true;
                    break;
                case MYSQL_QUOTE:
                    quoteLength = MYSQL_QUOTE.length();
                    endString = MYSQL_QUOTE;
                    doubleEscapePossible = true;
                    break;
                case MSSQL_BEGIN_QUOTE:
                    quoteLength = MSSQL_BEGIN_QUOTE.length();
                    endString = MSSQL_END_QUOTE;
                    doubleEscapePossible = true;
                    break;
                case "$":
                    if(compat.isUseDollarQuotes()) {
                        int nextDollar = sql.indexOf('$', pos + 1);
                        if(nextDollar >= 0) {
                            String tagWithDollar = sql.substring(pos, nextDollar + 1);
                            if(tagWithDollar.matches("\\$\\S*?\\$")) {
                                quoteLength = tagWithDollar.length();
                                endString = tagWithDollar;
                            } else {
                                return false;
                            }
                        }
                    }
                    doubleEscapePossible = false;
                    break;
            }
            
            if(endString == null) {
                return false;
            }

            do {
                int endCandidate = sql.indexOf(endString, pos + quoteLength);
                // EndQuote not found ...
                if (endCandidate == -1) {
                    pos = sql.length();
                } else {
                    pos = endCandidate + endString.length();
                }
                // If the string following the quote is the endquote and 
                // doubling the endquote escaped it, continue scanning
            } while (doubleEscapePossible
                    && pos < sql.length()
                    && sql.startsWith(endString, pos));

            appendSQL(startPos, pos);
            
            return true;
        }
        
        private static final String SQL99_STRING_QUOTE = "'";
        private static final String SQL99_IDENTIFIER_QUOTE = "\"";
        private static final String MYSQL_QUOTE = "`";
        private static final String MSSQL_END_QUOTE = "]";
        private static final String MSSQL_BEGIN_QUOTE = "[";
        
        /**
         * Consume a delimiter statement.
         * 
         * <p>See if the user wants to use a different delimiter for splitting
         * up statements.  This is useful if, for example, their SQL contains
         * stored procedures or triggers or other blocks that contain multiple
         * statements but should be executed as a single unit. </p>
         * 
         * <p>If we see the delimiter token, we read in what the new delimiter 
         * should be, and then return the new character position past the
         * delimiter statement, as this shouldn't be passed on to the 
         * database.</p>
         * 
         * <p>Contents won't be part of the extracted statement</p>
         */
        private boolean consumeDelimiterStatement() {     
            if ( pos == sql.length()) {
                return false;
            }
            
            if ( ! isToken(DELIMITER_TOKEN)) {
                return false;
            }
            
            if ( statement.length() > 0 ) {
                return false;
            }
            
            int startPos = pos;
            
            // Skip past the delimiter token
            int tokenLength = DELIMITER_TOKEN.length();
            pos += tokenLength;
            
            // Skip over Whitespace
            while ( pos < sql.length() &&
                    Character.isWhitespace(sql.charAt(pos))) {
                pos++;
            }
            
            // 
            int endPos = pos;
            while ( endPos < sql.length() &&
                    ! Character.isWhitespace(sql.charAt(endPos))) {
                endPos++;
            }
            
            if ( startPos == endPos ) {
                return false;
            }
            
            delimiter = sql.substring(pos, endPos);

            return true;
        }
        
        /**
         * Check for Delimiter Statement in comment.
         * 
         * <p>Simulates regular behaviour found in checkDelimiterStatement</p>
         */
        private void checkForDelimiterStmt(int initialOffset, int lastPosToScan) {
            int start = initialOffset;
            while(start < lastPosToScan && Character.isWhitespace(sql.charAt(start))) {
                start++;
            }
            if(start >= lastPosToScan && (lastPosToScan - start + 1) <= DELIMITER_TOKEN.length()) {
                return;
            }
            boolean delimiterMatched = true;
            for(int i = 0; i < DELIMITER_TOKEN.length(); i++) {
                if(Character.toUpperCase(sql.charAt(start)) != 
                        Character.toUpperCase(DELIMITER_TOKEN.charAt(i))) {
                    delimiterMatched = false;
                    break;
                }
                start++;
            }
            if(! delimiterMatched) {
                return;
            }
            while (Character.isWhitespace(sql.charAt(start)) && start < lastPosToScan) {
                start++;
            }
            StringBuilder sb = new  StringBuilder();
            for(int i = start; i <= lastPosToScan; i++) {
                if(! Character.isWhitespace(sql.charAt(i))) {
                    sb.append(sql.charAt(i));
                } else {
                    break;
                }
            }
            if(sb.length() > 0) {
                delimiter = sb.toString();
            }
        }
        
        private boolean isDelimiter() {
            int length = delimiter.length();
            
            if ( pos + length > sql.length()) {
                return false;
            }
            
            for ( int i = 0 ; i < length ; i++ ) {
                if (delimiter.charAt(i) != sql.charAt(pos + i)) {
                    return false;
                }
                i++;
            }
            
            return true;
        }
        
        /** 
         * See if the SQL text starting at the given position is a given token 
         * 
         * @param sql - the full SQL text
         * @param ch - the character at the current position
         * @param pos - the current position index for the SQL text
         * @param token - the token we are looking for
         * 
         * @return true if the token is found at the current position
         */
        private boolean isToken(String token) {
            char ch = sql.charAt(pos);
            
            // Simple check to see if there's potential.  In most cases this
            // will return false and we don't have to waste our time doing
            // any other processing.  Move along, move along...
            if ( Character.toUpperCase(ch) != 
                    Character.toUpperCase(token.charAt(0)) ) {
                return false;
            }

            // Don't want to recognize larger strings that contain the token
            if ( pos > 0 &&  !Character.isWhitespace(sql.charAt(pos - 1)) ) {
                return false;
            }
            
            if ( sql.length() > pos + token.length() &&
                    Character.isLetterOrDigit(sql.charAt(pos + token.length())) ) {
                return false;
            }
        

            // Create a substring that contains just the potential token
            // This way we don't have to uppercase the entire SQL string.
            String substr;
            try {
                substr = sql.substring(pos, pos + token.length()); // NOI18N
            } catch ( IndexOutOfBoundsException e ) {
                return false;
            }
            
            if ( substr.equalsIgnoreCase(token)) { // NOI18N
                return true;
            }
            
            return false;            
        }
        
        private void addStatement() {
            String sqlTrimmed = statement.toString().trim();
            statement.setLength(0);
            if (sqlTrimmed.length() <= 0) {
                return;
            }
            
            int line = 0;
            int newLinePos = -1;
            for(Integer offset: newLineOffsets) {
                if(offset < positionMap.get(0)) {
                    line++;
                    newLinePos = offset;
                } else {
                    break;
                }
            }
            
            StatementInfo info = new StatementInfo(sqlTrimmed, rawStartOffset, positionMap.get(0), line, positionMap.get(0) - newLinePos - 1, endOffset + 1, rawEndOffset, positionMap, newLineOffsets);
            statements.add(info);
            positionMap.clear();
        }
        
        public List<StatementInfo> getStatements() {
            return Collections.unmodifiableList(statements);
        }
        
    }
    
    /**
     * Hold information about compatiblity settings to apply when cutting
     * SQL string.
     */
    static class Compatibility {

        public static final Compatibility COMPAT_GENERIC = new Compatibility(false, false);

        public static final Compatibility COMPAT_MYSQL = new Compatibility(true, false);

        public static final Compatibility COMPAT_POSTGERSQL = new Compatibility(false, true);
        
        /**
         * Mysql supports "#" ('hash'-Char) as line comment in addition to
         * SQL-Standard "--"
         * 
         * See:
         * http://dev.mysql.com/doc/refman/5.7/en/comments.html
         */
        private final boolean useHashComments;

        /**
         * Refer to PostgreSQL documentation:
         * http://www.postgresql.org/docs/9.0/static/sql-syntax-lexical.html#SQL-SYNTAX-DOLLAR-QUOTING
         *
         * These two string are "equivalent" in PostgreSQL notation, first one
         * uses '$$' as quote mark, second one uses '$SomeTag$': $$Dianne's
         * horse$$ $SomeTag$Dianne's horse$SomeTag$
         *
         * The most typical usage - quoting of the body of stored procedures to
         * avoid splitting of sql on ';' occurring inside of 'CREATE FUNCTION'
         * statement
         * http://www.postgresql.org/docs/9.1/static/sql-createfunction.html
         *
         * CREATE OR REPLACE FUNCTION dummy(IN dummy_arg varchar) RETURNS
         * varchar LANGUAGE plpgsql AS $$ DECLARE dummy_result varchar; BEGIN
         * select concat('dummy(', dummy_arg, ')') into dummy_result; return
         * dummy_result; END $$;
         */
        private final boolean useDollarQuotes;

        public Compatibility(boolean useHashComments, boolean useDollarQuotes) {
            this.useHashComments = useHashComments;
            this.useDollarQuotes = useDollarQuotes;
        }

        public boolean isUseHashComments() {
            return useHashComments;
        }

        public boolean isUseDollarQuotes() {
            return useDollarQuotes;
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.useHashComments ? 1 : 0);
            hash = 29 * hash + (this.useDollarQuotes ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Compatibility other = (Compatibility) obj;
            if (this.useHashComments != other.useHashComments) {
                return false;
            }
            if (this.useDollarQuotes != other.useDollarQuotes) {
                return false;
            }
            return true;
        }
    }
}