#define __P(x) x
extern struct ifnet *get_unit __P((char *, int, ipf_stack_t *));
extern char *get_variable __P((char *, char **, int));
