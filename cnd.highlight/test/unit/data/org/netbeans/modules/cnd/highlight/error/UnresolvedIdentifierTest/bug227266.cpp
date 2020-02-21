#define EXECPROC(NAME) void cx##NAME(void)
#define PARSEPROC(NAME) char *cp##NAME(char *buffer)
#define STDPARSE(NAME) PARSEPROC(NAME) {return buffer;}
#define LISTPROC(NAME) char *cl##NAME(char *buffer)
#define STDLIST(NAME) LISTPROC(NAME) {return buffer;}
#define stringify(s) #s
#define token(tok) stringify(tok), &cx##tok, &cp##tok, &cl##tok
// Define that creates a standard runtime-word(required) and no special treatment
#define STDPROC(NAME) STDLIST(NAME) STDPARSE(NAME) EXECPROC(NAME)
// Call the executable of a runtime word
#define STDCALL(NAME) cx##NAME()
#define UNIMPLEMENTED(NAME) STDPROC(NAME) { error(69); }
STDPROC(OPEN) {
  int a;
  a=0;
  return;
}
