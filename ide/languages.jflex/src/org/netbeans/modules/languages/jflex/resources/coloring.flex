/*
    coloring example
*/
package myPackage;

%%
%class Lexer
%type Token
%public

%{
    private void popState() {
        yybegin(stack.pop());
    }
%}

LineTerminator = \r|\n|\r\n
Ws     = {LineTerminator} | [ \t\f]
Identifier = [:jletter:] [:jletterdigit:]*

DecIntegerLiteral = [0-9]

Experiment = ('dd'~\\")
TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*
DecIntegerLiteral = 0 | [1-9][0-9]*

%state STRING

%%

<YYINITIAL> "string" {
    return yytext();
}

<YYINITIAL> {
    {Identifier} {
        return null;
    }

    /* whitespace */
    {Ws}                   { /* ignore */ }
}

[^] {
    throw new Exception("Invalid token");
}
