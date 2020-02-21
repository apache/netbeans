struct Tab {
    int defined;
};
Tab Symtab[] = {};
#define SYMTAB Symtab
#define sy_(node) 			SYMTAB[node]
#define sy_defined_(node) 		sy_(node).defined
#if defined(D)
#endif

int main(int argc, char** argv) {
    int node = 0;
    if (sy_defined_(node)) {
        return 0;
    }
}
