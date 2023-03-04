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
package org.netbeans.modules.xml.text.syntax.javacc;

public interface XMLSyntaxConstants {

  int EOF = 0;
  int TEXT = 1;
  int CONTENT = 2;
  int WS = 3;
  int S = 4;
  int NAME = 5;
  int RSB = 6;
  int TAG_START = 7;
  int DECL_START = 8;
  int PI_START = 9;
  int CDATA_START = 10;
  int COND_END_IN_DEFAULT = 11;
  int DTD_END_IN_DEFAULT = 12;
  int TEXT_IN_DEFAULT = 13;
  int ERR_IN_DEFAULT = 14;
  int TAG_NAME = 15;
  int ERR_IN_TAG = 16;
  int ATT_NAME = 17;
  int ERR_IN_TAG_ATTLIST = 18;
  int WS_IN_TAG_ATTLIST = 19;
  int EQ_IN_TAG_ATTLIST = 20;
  int TAG_END = 21;
  int XML_TARGET = 22;
  int PI_CONTENT_START = 23;
  int PI_END = 24;
  int ERR_IN_PI = 25;
  int PI_TARGET = 26;
  int PI_CONTENT_END = 27;
  int TEXT_IN_PI_CONTENT = 28;
  int ERR_IN_PI_CONTENT = 29;
  int KW_IN_XML_DECL = 30;
  int TEXT_IN_XML_DECL = 31;
  int BR_IN_XML_DECL = 32;
  int XML_DECL_END = 33;
  int Q_IN_XML_DECL = 34;
  int CDATA_END = 35;
  int TEXT_IN_CDATA = 36;
  int MARKUP_IN_CDATA = 37;
  int CDATA_CONTENT = 38;
  int ENTITY = 39;
  int ATTLIST = 40;
  int DOCTYPE = 41;
  int ELEMENT = 42;
  int NOTATION = 43;
  int TEXT_IN_DECL = 44;
  int WS_IN_DECL = 45;
  int ERR_IN_DECL = 46;
  int COND = 47;
  int DECL_END = 48;
  int KW_IN_ENTITY = 49;
  int TEXT_IN_ENTITY = 50;
  int ENTITY_END = 51;
  int EMPTY = 52;
  int PCDATA = 53;
  int ANY = 54;
  int TEXT_IN_ELEMENT = 55;
  int ELEMENT_END = 56;
  int SYSTEM_IN_NOTATION = 57;
  int TEXT_IN_NOTATION = 58;
  int ERR_IN_NOTATION = 59;
  int NOTATION_END = 60;
  int INCLUDE = 61;
  int IGNORE = 62;
  int TEXT_IN_COND = 63;
  int ERR_IN_COND = 64;
  int COND_END = 65;
  int ERR_IN_ATTLIST = 66;
  int REQUIRED = 67;
  int IMPLIED = 68;
  int FIXED = 69;
  int ID_IN_ATTLIST = 70;
  int CDATA = 71;
  int IDREF = 72;
  int IDREFS = 73;
  int ENTITY_IN_ATTLIST = 74;
  int ENTITIES = 75;
  int NMTOKEN = 76;
  int NMTOKENS = 77;
  int NOTATION_IN_ATTLIST = 78;
  int TEXT_IN_ATTLIST = 79;
  int ATTLIST_END = 80;
  int PUBLIC = 81;
  int SYSTEM = 82;
  int TEXT_IN_DOCTYPE = 83;
  int ERR_IN_DOCTYPE = 84;
  int DOCTYPE_END = 85;
  int PREF_START = 86;
  int TEXT_IN_PREF = 87;
  int PREF_END = 88;
  int GREF_START = 89;
  int TEXT_IN_GREF = 90;
  int ERR_IN_GREF = 91;
  int GREF_END = 92;
  int CHARS_START = 93;
  int TEXT_IN_CHARS = 94;
  int CHARS_END = 95;
  int GREF_CHARS_START = 96;
  int TEXT_IN_GREF_CHARS = 97;
  int GREF_CHARS_END = 98;
  int STRING_START = 99;
  int TEXT_IN_STRING = 100;
  int STRING_END = 101;
  int GREF_STRING_START = 102;
  int TEXT_IN_GREF_STRING = 103;
  int GREF_STRING_END = 104;
  int COMMENT_START = 105;
  int TEXT_IN_COMMENT = 106;
  int ERR_IN_COMMENT = 107;
  int COMMENT_END = 108;

  int IN_COMMENT = 0;
  int IN_STRING = 1;
  int IN_CHARS = 2;
  int IN_GREF = 3;
  int IN_GREF_STRING = 4;
  int IN_GREF_CHARS = 5;
  int IN_PREF = 6;
  int IN_DOCTYPE = 7;
  int IN_ATTLIST_DECL = 8;
  int IN_COND = 9;
  int IN_NOTATION = 10;
  int IN_ELEMENT = 11;
  int IN_ENTITY_DECL = 12;
  int IN_DECL = 13;
  int IN_CDATA = 14;
  int IN_XML_DECL = 15;
  int IN_PI_CONTENT = 16;
  int IN_PI = 17;
  int IN_TAG_ATTLIST = 18;
  int IN_TAG = 19;
  int DEFAULT = 20;

  String[] tokenImage = {
    "<EOF>", // NOI18N
    "<TEXT>", // NOI18N
    "<CONTENT>", // NOI18N
    "<WS>", // NOI18N
    "<S>", // NOI18N
    "<NAME>", // NOI18N
    "\"]\"", // NOI18N
    "\"<\"", // NOI18N
    "\"<!\"", // NOI18N
    "\"<?\"", // NOI18N
    "\"<![CDATA[\"", // NOI18N
    "\"]]>\"", // NOI18N
    "\"]>\"", // NOI18N
    "<TEXT_IN_DEFAULT>", // NOI18N
    "\"<<\"", // NOI18N
    "<TAG_NAME>", // NOI18N
    "<ERR_IN_TAG>", // NOI18N
    "<ATT_NAME>", // NOI18N
    "<ERR_IN_TAG_ATTLIST>", // NOI18N
    "<WS_IN_TAG_ATTLIST>", // NOI18N
    "\"=\"", // NOI18N
    "<TAG_END>", // NOI18N
    "\"xml\"", // NOI18N
    "<PI_CONTENT_START>", // NOI18N
    "\"?>\"", // NOI18N
    "<ERR_IN_PI>", // NOI18N
    "<PI_TARGET>", // NOI18N
    "\"?>\"", // NOI18N
    "<TEXT_IN_PI_CONTENT>", // NOI18N
    "<ERR_IN_PI_CONTENT>", // NOI18N
    "<KW_IN_XML_DECL>", // NOI18N
    "<TEXT_IN_XML_DECL>", // NOI18N
    "<BR_IN_XML_DECL>", // NOI18N
    "\"?>\"", // NOI18N
    "\"?\"", // NOI18N
    "\"]]>\"", // NOI18N
    "<TEXT_IN_CDATA>", // NOI18N
    "<MARKUP_IN_CDATA>", // NOI18N
    "<CDATA_CONTENT>", // NOI18N
    "\"ENTITY\"", // NOI18N
    "\"ATTLIST\"", // NOI18N
    "\"DOCTYPE\"", // NOI18N
    "\"ELEMENT\"", // NOI18N
    "\"NOTATION\"", // NOI18N
    "<TEXT_IN_DECL>", // NOI18N
    "<WS_IN_DECL>", // NOI18N
    "<ERR_IN_DECL>", // NOI18N
    "\"[\"", // NOI18N
    "\">\"", // NOI18N
    "<KW_IN_ENTITY>", // NOI18N
    "<TEXT_IN_ENTITY>", // NOI18N
    "\">\"", // NOI18N
    "\"EMPTY\"", // NOI18N
    "\"#PCDATA\"", // NOI18N
    "\"ANY\"", // NOI18N
    "<TEXT_IN_ELEMENT>", // NOI18N
    "\">\"", // NOI18N
    "<SYSTEM_IN_NOTATION>", // NOI18N
    "<TEXT_IN_NOTATION>", // NOI18N
    "\"<\"", // NOI18N
    "\">\"", // NOI18N
    "\"INCLUDE\"", // NOI18N
    "\"IGNORE\"", // NOI18N
    "<TEXT_IN_COND>", // NOI18N
    "<ERR_IN_COND>", // NOI18N
    "\"[\"", // NOI18N
    "<ERR_IN_ATTLIST>", // NOI18N
    "\"#REQUIRED\"", // NOI18N
    "\"#IMPLIED\"", // NOI18N
    "\"#FIXED\"", // NOI18N
    "\"ID\"", // NOI18N
    "\"CDATA\"", // NOI18N
    "\"IDREF\"", // NOI18N
    "\"IDREFS\"", // NOI18N
    "\"ENTITY\"", // NOI18N
    "\"ENTITIES\"", // NOI18N
    "\"NMTOKEN\"", // NOI18N
    "\"NMTOKENS\"", // NOI18N
    "\"NOTATION\"", // NOI18N
    "<TEXT_IN_ATTLIST>", // NOI18N
    "\">\"", // NOI18N
    "\"PUBLIC\"", // NOI18N
    "\"SYSTEM\"", // NOI18N
    "<TEXT_IN_DOCTYPE>", // NOI18N
    "<ERR_IN_DOCTYPE>", // NOI18N
    "<DOCTYPE_END>", // NOI18N
    "\"%\"", // NOI18N
    "<TEXT_IN_PREF>", // NOI18N
    "<PREF_END>", // NOI18N
    "\"&\"", // NOI18N
    "<TEXT_IN_GREF>", // NOI18N
    "<ERR_IN_GREF>", // NOI18N
    "<GREF_END>", // NOI18N
    "\"\\\'\"", // NOI18N
    "<TEXT_IN_CHARS>", // NOI18N
    "\"\\\'\"", // NOI18N
    "\"\\\'\"", // NOI18N
    "<TEXT_IN_GREF_CHARS>", // NOI18N
    "\"\\\'\"", // NOI18N
    "\"\\\"\"", // NOI18N
    "<TEXT_IN_STRING>", // NOI18N
    "\"\\\"\"", // NOI18N
    "\"\\\"\"", // NOI18N
    "<TEXT_IN_GREF_STRING>", // NOI18N
    "\"\\\"\"", // NOI18N
    "\"<!--\"", // NOI18N
    "<TEXT_IN_COMMENT>", // NOI18N
    "<ERR_IN_COMMENT>", // NOI18N
    "\"-->\"", // NOI18N
  };

}
