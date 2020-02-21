struct tok_state {
    int k;
};

static int
check_bom(int get_char(struct tok_state *),
	  void unget_char(int, struct tok_state *),
	  int set_readline(struct tok_state *, const char *),
	  struct tok_state *tok)
{
    return 0;
}