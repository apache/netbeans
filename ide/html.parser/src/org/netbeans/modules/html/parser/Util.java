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
package org.netbeans.modules.html.parser;

/**
 *
 * @author marekfukala
 */
public class Util {

    public static final String[] TOKENIZER_STATE_NAMES = new String[]{
        "DATA",
        "RCDATA",
        "SCRIPT_DATA",
        "RAWTEXT",
        "SCRIPT_DATA_ESCAPED",
        "ATTRIBUTE_VALUE_DOUBLE_QUOTED",
        "ATTRIBUTE_VALUE_SINGLE_QUOTED",
        "ATTRIBUTE_VALUE_UNQUOTED",
        "PLAINTEXT",
        "TAG_OPEN",
        "CLOSE_TAG_OPEN",
        "TAG_NAME",
        "BEFORE_ATTRIBUTE_NAME",
        "ATTRIBUTE_NAME",
        "AFTER_ATTRIBUTE_NAME",
        "BEFORE_ATTRIBUTE_VALUE",
        "AFTER_ATTRIBUTE_VALUE_QUOTED",
        "BOGUS_COMMENT",
        "MARKUP_DECLARATION_OPEN",
        "DOCTYPE",
        "BEFORE_DOCTYPE_NAME",
        "DOCTYPE_NAME",
        "AFTER_DOCTYPE_NAME",
        "BEFORE_DOCTYPE_PUBLIC_IDENTIFIER",
        "DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED",
        "DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED",
        "AFTER_DOCTYPE_PUBLIC_IDENTIFIER",
        "BEFORE_DOCTYPE_SYSTEM_IDENTIFIER",
        "DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED",
        "DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED",
        "AFTER_DOCTYPE_SYSTEM_IDENTIFIER",
        "BOGUS_DOCTYPE",
        "COMMENT_START",
        "COMMENT_START_DASH",
        "COMMENT",
        "COMMENT_END_DASH",
        "COMMENT_END",
        "COMMENT_END_SPACE",
        "COMMENT_END_BANG",
        "NON_DATA_END_TAG_NAME",
        "MARKUP_DECLARATION_HYPHEN",
        "MARKUP_DECLARATION_OCTYPE",
        "DOCTYPE_UBLIC",
        "DOCTYPE_YSTEM",
        "AFTER_DOCTYPE_PUBLIC_KEYWORD",
        "BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS",
        "AFTER_DOCTYPE_SYSTEM_KEYWORD",
        "CONSUME_CHARACTER_REFERENCE",
        "CONSUME_NCR",
        "CHARACTER_REFERENCE_TAIL",
        "HEX_NCR_LOOP",
        "DECIMAL_NRC_LOOP",
        "HANDLE_NCR_VALUE",
        "HANDLE_NCR_VALUE_RECONSUME",
        "CHARACTER_REFERENCE_HILO_LOOKUP",
        "SELF_CLOSING_START_TAG",
        "CDATA_START",
        "CDATA_SECTION",
        "CDATA_RSQB",
        "CDATA_RSQB_RSQB",
        "SCRIPT_DATA_LESS_THAN_SIGN",
        "SCRIPT_DATA_ESCAPE_START",
        "SCRIPT_DATA_ESCAPE_START_DASH",
        "SCRIPT_DATA_ESCAPED_DASH",
        "SCRIPT_DATA_ESCAPED_DASH_DASH",
        "BOGUS_COMMENT_HYPHEN",
        "RAWTEXT_RCDATA_LESS_THAN_SIGN",
        "SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN",
        "SCRIPT_DATA_DOUBLE_ESCAPE_START",
        "SCRIPT_DATA_DOUBLE_ESCAPED",
        "SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN",
        "SCRIPT_DATA_DOUBLE_ESCAPED_DASH",
        "SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH",
        "SCRIPT_DATA_DOUBLE_ESCAPE_END"
    }; //NOI18N
    
}
