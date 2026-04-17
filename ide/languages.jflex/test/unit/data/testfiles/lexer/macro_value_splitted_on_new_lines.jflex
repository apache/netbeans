package org.netbeans.modules.languages.jflex;

%%
%class Lexer
%type Token

KEYWORD = "true"
//indented macro
    EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
    DocumentationComment = "/**" {CommentContent} "*"+ "/"
    CommentContent       = ( [^*] | \*+ [^/*] )*

MACRO = 
  "if"
  | "elseif"
  | {TEST}
  | "else"

END_MACRO = "endif" //test
| "endforeach"

SYMBOL=[a-zA-Z0-9_]+(\-[a-zA-Z0-9_]+)*
%%

<YYINITIAL>[^] {}
