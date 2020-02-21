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
package org.netbeans.modules.subversion.remote.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * 
 */
public class SVNRevision {
    public enum Kind {
        unspecified,
        number,
        date,
        committed,
        previous,
        base,
        working,
        head;
    }
    
    public static final SVNRevision HEAD = new SVNRevision(Kind.head);
    public static final SVNRevision START = new SVNRevision(Kind.head);
    public static final SVNRevision COMMITTED = new SVNRevision(Kind.committed);
    public static final SVNRevision PREVIOUS = new SVNRevision(Kind.previous);
    public static final SVNRevision BASE = new SVNRevision(Kind.base);
    public static final SVNRevision WORKING = new SVNRevision(Kind.working);
    public static final Number INVALID_REVISION = new Number(-1);
    
    private final Kind kind;
    private SVNRevision(Kind kind) {
        this.kind = kind;
    }
    
    public static SVNRevision getRevision(String revision) throws ParseException {
         return getRevision(revision, DateSpec.dateFormat);
    }
    
    public static SVNRevision getRevision(String revision, SimpleDateFormat aDateFormat) throws ParseException {
        if (revision == null || revision.equals("")) { //NOI18N
            return null;
        }
        if (revision.compareToIgnoreCase("HEAD") == 0) { //NOI18N
            return HEAD;
        }
        if (revision.compareToIgnoreCase("BASE") == 0) { //NOI18N
            return BASE;
        }
        if (revision.compareToIgnoreCase("COMMITED") == 0) { //NOI18N
            return COMMITTED;
        }
        if (revision.compareToIgnoreCase("PREV") == 0) { //NOI18N
            return PREVIOUS;
        }
        int revisionNumber = Integer.parseInt(revision);
        if (revisionNumber >= 0) {
            return new Number(revisionNumber);
        }
        if (aDateFormat == null) {
            aDateFormat = DateSpec.dateFormat;
        }
        try {
            Date revisionDate = aDateFormat.parse(revision);
            return new DateSpec(revisionDate);
        } catch (ParseException e) {
            throw new ParseException("Invalid revision '"+revision+"'", 0); //NOI18N
        }
    }
    
    public Kind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        switch(kind) {
            case base:
                return "BASE"; //NOI18N
            case committed:
                return "COMMITTED"; //NOI18N
            case head:
                return "HEAD"; //NOI18N
            case previous:
                return "PREV"; //NOI18N
            case unspecified:
                return "START"; //NOI18N
            case working:
                return "WORKING"; //NOI18N
            case date:
            case number:
            default:
                return super.toString();
        }
    }
    
    public static class Number extends SVNRevision implements Comparable<Number> {
        private final long number;
        public Number(long number) {
            super(Kind.number);
            this.number = number;
        }
        public long getNumber() {
            return number;
        }
        
        @Override
        public int compareTo(Number o) {
            return (int)(number - o.number);
        }

        @Override
        public String toString() {
            return Long.toString(number);
        }
        
    }
    
    public static class DateSpec extends SVNRevision {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ", Locale.US); //NOI18N

        protected final Date revDate;

        public DateSpec(Date date) {
            super(Kind.date);
            revDate = date;
        }

        public Date getDate() {
            return revDate;
        }

        @Override
        public String toString() {
            return "{"+dateFormat.format(revDate)+"}"; //NOI18N
        }
    }
}
