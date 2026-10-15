package org.netbeans.modules.languages.jflex;

%%
%class Lexer
%type Token

Identifier = [:jletter:] [:jletterdigit:]*

%%

<YYINITIAL> "abstract"           { return symbol(sym.ABSTRACT); }
<YYINITIAL> "true" | "false"     { return symbol(sym.KEYWORD); }

<YYINITIAL> {
      /* identifiers */ 
      {Identifier}  {
          return symbol(sym.IDENTIFIER);
      }

      "true" | "false" { return symbol(sym.KEYWORD); }
}
