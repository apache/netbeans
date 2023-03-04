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

package org.netbeans.modules.xml.text.syntax;

import org.netbeans.editor.BaseTokenID;

/**
 * Enumeration of all XML TokenIds.
 *
 * @author  Petr Kuzel
 * @see XMLDefaultTokenContext
 * @see XMLTokenId
 */
@Deprecated
public interface XMLTokenIDs {

    // Token categories

    // Numeric-ids for token-ids
    public static final int TEXT_ID = 1;
    public static final int WS_ID = 2;
    public static final int ERROR_ID = 3;
    public static final int TAG_ID = 4;
    public static final int ARGUMENT_ID = 5;
    public static final int OPERATOR_ID = 6;
    public static final int VALUE_ID = 7;
    public static final int BLOCK_COMMENT_ID = 8;
//    public static final int SGML_COMMENT_ID = 9;
    public static final int DECLARATION_ID = 10;
    public static final int CHARACTER_ID = 11;
    
    public static final int EOL_ID = 12;

    public static final int PI_START_ID = 13;
    public static final int PI_TARGET_ID = 14;
    public static final int PI_CONTENT_ID = 15;
    public static final int PI_END_ID = 16;

    public static final int CDATA_SECTION_ID = 17;
    
    // Token-ids
    /** Plain text */
    public static final BaseTokenID TEXT = new BaseTokenID( "text", TEXT_ID );
    /** Erroneous Text */
    public static final BaseTokenID WS = new BaseTokenID( "ws", WS_ID );
    /** Plain Text*/
    public static final BaseTokenID ERROR = new BaseTokenID( "error", ERROR_ID );
    /** XML Tag */
    public static final BaseTokenID TAG = new BaseTokenID( "tag", TAG_ID );
    /** Argument of a tag */
    public static final BaseTokenID ARGUMENT = new BaseTokenID( "attribute", ARGUMENT_ID );
    /** Operators - '=' between arg and value */
    public static final BaseTokenID OPERATOR = new BaseTokenID( "operator", OPERATOR_ID );
    /** Value - value of an argument */
    public static final BaseTokenID VALUE = new BaseTokenID( "value", VALUE_ID );
    /** Block comment */
    public static final BaseTokenID BLOCK_COMMENT = new BaseTokenID( "comment", BLOCK_COMMENT_ID );
    /** SGML declaration in XML document - e.g. <!DOCTYPE> */
    public static final BaseTokenID DECLARATION = new BaseTokenID( "doctype", DECLARATION_ID );
    /** Character reference, e.g. &amp;lt; = &lt; */
    public static final BaseTokenID CHARACTER = new BaseTokenID( "ref", CHARACTER_ID );
    
    /** End of line */
    public static final BaseTokenID EOL = new BaseTokenID( "EOL", EOL_ID );

    /* PI start delimiter <sample><b>&lt;?</b>target content of pi ?></sample> */
    public static final BaseTokenID PI_START = new BaseTokenID( "pi-start", PI_START_ID);    
    /* PI target <sample>&lt;?<b>target</b> content of pi ?></sample> */
    public static final BaseTokenID PI_TARGET = new BaseTokenID( "pi-target", PI_TARGET_ID);
    /* PI conetnt <sample>&lt;?target <b>content of pi </b>?></sample> */
    public static final BaseTokenID PI_CONTENT = new BaseTokenID( "pi-content", PI_CONTENT_ID);
    /* PI end delimiter <sample>&lt;?target <content of pi <b>?></b></sample> */
    public static final BaseTokenID PI_END = new BaseTokenID( "pi-end", PI_END_ID);
    /** Cdata section including its delimiters. */
    public static final BaseTokenID CDATA_SECTION = new BaseTokenID( "cdata-section", CDATA_SECTION_ID);
}
