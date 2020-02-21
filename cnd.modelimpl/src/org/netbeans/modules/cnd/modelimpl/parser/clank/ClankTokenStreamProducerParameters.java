/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
