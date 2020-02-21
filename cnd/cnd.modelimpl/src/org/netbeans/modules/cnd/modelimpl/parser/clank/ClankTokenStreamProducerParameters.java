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
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;

/**
 *
 */
/*package*/ class ClankTokenStreamProducerParameters {

    /*package*/static final class YesNoInterested {

        public static final int ALWAYS = 0;
        public static final int NEVER = 1;
        public static final int INTERESTED = 2;

        private YesNoInterested() {
        }
    }

    public final /*YesNoInterested*/ int needTokens;
    public final /*YesNoInterested*/ int needPPDirectives;
    public final /*YesNoInterested*/ int needSkippedRanges;
    public final /*YesNoInterested*/ int needMacroExpansion;
    public final /*YesNoInterested*/ int needComments;
    public final boolean triggerParsingActivity;
    public final boolean applyLanguageFilter;

    private ClankTokenStreamProducerParameters(/*YesNoInterested*/int needTokens, boolean triggerParsingActivity,
            /*YesNoInterested*/ int needPPDirectives, /*YesNoInterested*/ int needMacroExpansion, /*YesNoInterested*/ int needSkippedRanges,
            /*YesNoInterested*/ int needComments, boolean applyLanguageFilter) {
        this.triggerParsingActivity = triggerParsingActivity;
        this.needPPDirectives = needPPDirectives;
        this.needSkippedRanges = needSkippedRanges;
        this.needTokens = needTokens;
        this.needMacroExpansion = needMacroExpansion;
        this.needComments = needComments;
        this.applyLanguageFilter = applyLanguageFilter;
    }

    public static ClankTokenStreamProducerParameters createForParsing(String language) {
        return APTLanguageSupport.FORTRAN.equals(language) ? FORTRAN_PARSING : PARSING ;
    }
    private static final ClankTokenStreamProducerParameters PARSING = new ClankTokenStreamProducerParameters(
            YesNoInterested.ALWAYS,
            true,
            YesNoInterested.ALWAYS,
            APTTraceFlags.DEFERRED_MACRO_USAGES ? YesNoInterested.NEVER : YesNoInterested.ALWAYS,
            YesNoInterested.ALWAYS,
            YesNoInterested.NEVER, // no comments needed for just parsing except fortran
            true);
    private static final ClankTokenStreamProducerParameters FORTRAN_PARSING = new ClankTokenStreamProducerParameters(
            YesNoInterested.ALWAYS,
            true,
            YesNoInterested.ALWAYS,
            APTTraceFlags.DEFERRED_MACRO_USAGES ? YesNoInterested.NEVER : YesNoInterested.ALWAYS,
            YesNoInterested.ALWAYS,
            YesNoInterested.ALWAYS, // no comments needed for just parsing except fortran
            true);

    public static ClankTokenStreamProducerParameters createForIncludedTokenStream(String language) {
        return APTLanguageSupport.FORTRAN.equals(language) ? 
                FORTRAN_INCLUDED_TOKEN_STREAM : INCLUDED_TOKEN_STREAM;
    }
    private static final ClankTokenStreamProducerParameters INCLUDED_TOKEN_STREAM = new ClankTokenStreamProducerParameters(
            YesNoInterested.INTERESTED,
            false,
            YesNoInterested.NEVER,
            YesNoInterested.NEVER,
            YesNoInterested.INTERESTED,
            YesNoInterested.NEVER, // no comments needed for just parsing except fortran
            false);
    private static final ClankTokenStreamProducerParameters FORTRAN_INCLUDED_TOKEN_STREAM = new ClankTokenStreamProducerParameters(
            YesNoInterested.INTERESTED,
            false,
            YesNoInterested.NEVER,
            YesNoInterested.NEVER,
            YesNoInterested.INTERESTED,
            YesNoInterested.ALWAYS, // no comments needed for just parsing except fortran
            false);

    public static ClankTokenStreamProducerParameters createForParsingAndTokenStreamCaching() {
        return PARSING_TOKEN_STREAM_AND_CACHING;
    }
    private static final ClankTokenStreamProducerParameters PARSING_TOKEN_STREAM_AND_CACHING = new ClankTokenStreamProducerParameters(
            YesNoInterested.INTERESTED,
            false,
            YesNoInterested.INTERESTED,
            YesNoInterested.INTERESTED,
            YesNoInterested.INTERESTED,
            YesNoInterested.INTERESTED, // we need comments for macro views
            false); //cache only unfiltered

    public static ClankTokenStreamProducerParameters createForTokenStreamCaching() {
        return TOKEN_STREAM_CACHING;
    }
    private static final ClankTokenStreamProducerParameters TOKEN_STREAM_CACHING = new ClankTokenStreamProducerParameters(
            YesNoInterested.INTERESTED,
            false,
            YesNoInterested.NEVER,
            YesNoInterested.INTERESTED, // we need start/end expansion toknes
            YesNoInterested.INTERESTED,
            YesNoInterested.INTERESTED, // we need comments for macro views
            false); //cache only unfiltered

    public static ClankTokenStreamProducerParameters createForMacroUsages() {
        return MACRO_USAGES;
    }
    private static final ClankTokenStreamProducerParameters MACRO_USAGES = new ClankTokenStreamProducerParameters(
            YesNoInterested.NEVER,
            false,
            YesNoInterested.INTERESTED,
            YesNoInterested.INTERESTED,
            YesNoInterested.NEVER,
            YesNoInterested.NEVER,
            false
    );

    @Override
    public String toString() {
        return "needTokens=" + needTokens + ", needPPDirectives=" + needPPDirectives + //NOI18N
                ", needSkippedRanges=" + needSkippedRanges + ", needMacroExpansion=" + needMacroExpansion + //NOI18N
                ", needComments=" + needComments + ", triggerParsingActivity=" + triggerParsingActivity + //NOI18N
                ", applyLanguageFilter=" + applyLanguageFilter; //NOI18N
    }

}
