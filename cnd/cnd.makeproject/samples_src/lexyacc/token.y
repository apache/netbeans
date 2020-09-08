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

%{
#include <stdio.h>
%}

%token DIGIT LOWERCASELETTER UPPERCASELETTER OTHER STOP

%%
start :
    | start something
    ;

something : DIGIT {printf("DIGIT\n");}
    | LOWERCASELETTER {printf("LOWERCASELETTER\n");}
    | UPPERCASELETTER {printf("UPPERCASELETTER\n");}
    | OTHER {printf("OTHER\n");}
    | STOP {exit(0);}
    ;
%%

main()
{
    // Intro
    printf("Type something followed by Return. Type 'q' or 'Q' to end.\n");
    printf("\n");
    // Start the parser
    return(yyparse());
}

yyerror(s)
char *s;
{
    printf("yacc error: %s\n", s);
}

yywrap()
{
    return(0);
}

