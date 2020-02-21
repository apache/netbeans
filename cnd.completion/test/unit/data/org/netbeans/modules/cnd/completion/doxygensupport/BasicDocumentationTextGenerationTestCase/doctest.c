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

struct sta {
    __nlink_t st_nlink;        /* Link count.  */
    __mode_t st_mode;        /* File mode.  */
    /* this is a comment
    and it extends until the closing
    star-slash comment mark */
    int a;      /*f*/
    /* This comment should be ignored*/
    
    double b;   // double slash comment
    abc d;  //double slash comment2

    // Comment 1
    dfhd cd;    // Comment 2
    // Comment 3
    dfhd ef;    /*! Comment 4*/
    /* Comment 5*/
    dfhd gh;    /// Comment 6
}

sta.st_nlink;
sta.st_mode;
sta.a;
sta.b;
sta.d;
sta.cd;
sta.ef;
sta.gh;

#define MAXISIZE 500000    // maximum iterated elements allowed to written per file
#define B(x/*param*/, y /*param2*/) /*fake comment*/ BODY + x + y  // line com