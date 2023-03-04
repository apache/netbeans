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
package org.netbeans.modules.lexer.demo.javacc;

public interface CalcConstants {

  int EOF = 0;
  int WHITESPACE = 1;
  int INCOMPLETE_ML_COMMENT = 2;
  int ML_COMMENT = 3;
  int PLUS = 4;
  int MINUS = 5;
  int MUL = 6;
  int DIV = 7;
  int MUL3 = 8;
  int PLUS5 = 9;
  int LPAREN = 10;
  int RPAREN = 11;
  int CONSTANT = 12;
  int FLOAT = 13;
  int INTEGER = 14;
  int DIGIT = 15;
  int ML_COMMENT_END = 16;
  int ERROR = 17;

  int DEFAULT = 0;
  int IN_ML_COMMENT = 1;

  String[] tokenImage = {
    "<EOF>",
    "<WHITESPACE>",
    "\"/*\"",
    "<ML_COMMENT>",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"***\"",
    "\"+++++\"",
    "\"(\"",
    "\")\"",
    "<CONSTANT>",
    "<FLOAT>",
    "<INTEGER>",
    "<DIGIT>",
    "\"*/\"",
    "<ERROR>",
  };

}
