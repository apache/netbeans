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

package org.netbeans.modules.versioning.hooks;

import java.io.File;
import java.util.Date;

/**
 *
 * @author Tomas Stupka
 */
public class HgHookContext extends VCSHookContext {

    private final String msg;
    private final LogEntry[] logEntry;
    private String warning;

    public HgHookContext(File[] files, String msg, LogEntry... logEntry) {
        super(files);
        this.msg = msg;
        this.logEntry = logEntry;
    }

    public String getMessage() {
        return msg;
    }

    public LogEntry[] getLogEntries() {
        return logEntry;
    }

    public String getWarning() {
        return ""; 
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public static class LogEntry {
        private final String message;
        private final String author;
        private final String revision;
        private final Date date;

        public LogEntry(String message, String author, String revision, Date date) {
            this.message = message;
            this.author = author;
            this.revision = revision;
            this.date = date;
        }

        public String getAuthor() {
            return author;
        }
        public String getChangeset() {
            return revision;
        }
        public Date getDate() {
            return date;
        }
        public String getMessage() {
            return message;
        }
    }
}
