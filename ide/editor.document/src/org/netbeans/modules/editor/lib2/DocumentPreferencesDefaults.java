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

package org.netbeans.modules.editor.lib2;

/**
 * This class contains settings default values copied over from SettingsDefaults and ExtSettingsDefaults.
 * It exists merely to allow editor infrastructure to use these constants for
 * backwards compatibility reasons without having to depend on editor.deprecated.pre61settings module.
 */
public final class DocumentPreferencesDefaults {

    private DocumentPreferencesDefaults() {
        // no-op
    }

    // -----------------------------------------------------------------------
    // --- from SettingsDefaults
    // -----------------------------------------------------------------------
    
    public static final int defaultTabSize = 8;
    public static final int defaultSpacesPerTab = 4;
    public static final int defaultShiftWidth = 4; // usually
    // not used as there's a Evaluator for shift width

    public static final int defaultTextLimitWidth = 80;

    public static final Acceptor defaultIdentifierAcceptor = AcceptorFactory.LETTER_DIGIT;
    public static final Acceptor defaultWhitespaceAcceptor = AcceptorFactory.WHITESPACE;

    public static final int defaultTextLeftMarginWidth = 2;

    public static final int defaultReadBufferSize = 16384;
    public static final int defaultWriteBufferSize = 16384;
    public static final int defaultReadMarkDistance = 180;
    public static final int defaultMarkDistance = 100;
    public static final int defaultMaxMarkDistance = 150;
    public static final int defaultMinMarkDistance = 50;
    public static final int defaultSyntaxUpdateBatchSize = defaultMarkDistance * 7;
    public static final int defaultLineBatchSize = 2;

    public static final boolean defaultExpandTabs = true;

    public static final Acceptor defaultAbbrevExpandAcceptor = AcceptorFactory.WHITESPACE;
    public static final Acceptor defaultAbbrevAddTypedCharAcceptor = AcceptorFactory.NL;
    public static final Acceptor defaultAbbrevResetAcceptor = AcceptorFactory.NON_JAVA_IDENTIFIER;
    
    public static final boolean defaultHomeKeyColumnOne = false;
    public static final boolean defaultWordMoveNewlineStop = true;
    public static final boolean defaultFindHighlightSearch = true;
    public static final boolean defaultFindIncSearch = true;
    public static final boolean defaultFindBackwardSearch = false;
    public static final boolean defaultFindWrapSearch = true;
    public static final boolean defaultFindMatchCase = false;
    public static final boolean defaultFindWholeWords = false;
    public static final boolean defaultFindRegExp = false;
    public static final int defaultFindHistorySize = 30;
    public static final int defaultWordMatchSearchLen = Integer.MAX_VALUE;
    public static final boolean defaultWordMatchWrapSearch = true;
    public static final boolean defaultWordMatchMatchOneChar = true;
    public static final boolean defaultWordMatchMatchCase = false;
    public static final boolean defaultWordMatchSmartCase = false;
}
