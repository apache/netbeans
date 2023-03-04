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
import java.util.List;

/**
 *
 * @author Tomas Stupka
 */
public class SvnHookContext extends VCSHookContext {

    private final String msg;
    private final List<LogEntry> logEntries;
    private String warning;

    public SvnHookContext(File[] files, String msg, List<LogEntry> logEntries) {
        super(files);
        this.msg = msg;
        this.logEntries = logEntries;
    }

    public String getMessage() {
        return msg;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public String getWarning() {
        return warning;                                                              // NOI18N
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public static class LogEntry {
        private final String message;
        private final String author;
        private final long revision;
        private final Date date;

        public LogEntry(String message, String author, long revision, Date date) {
            this.message = message;
            this.author = author;
            this.revision = revision;
            this.date = date;
        }

        public String getAuthor() {
            return author;
        }
        public long getRevision() {
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
