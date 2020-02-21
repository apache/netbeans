/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
