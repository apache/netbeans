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
lexer grammar TerraformLexer;
options { superClass = org.netbeans.modules.languages.hcl.HCLHereDocAdaptor; }
import HCLLexer;

BOOL
    : 'bool'
    ;

DATA
    : 'data'
    ;

LIST
    : 'list'
    ;

LOCALS
    : 'locals'
    ;

LOCAL
    : 'local'
    ;

MAP
    : 'map'
    ;

MODULE
    : 'module'
    ;

MOVED
    : 'moved'
    ;

NUMBER
    : 'number'
    ;

OBJECT
    : 'object'
    ;

OPTIONAL
    : 'optional'
    ;

OUTPUT
    : 'output'
    ;

PROVIDER
    : 'provider'
    ;

RESOURCE
    : 'resource'
    ;

STRING
    : 'string'
    ;

TERRAFORM
    : 'terraform'
    ;

VARIABLE
    : 'variable'
    ;

VAR
    : 'var'
    ;
