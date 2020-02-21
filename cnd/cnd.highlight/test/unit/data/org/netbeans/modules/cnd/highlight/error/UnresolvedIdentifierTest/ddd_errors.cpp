#include "ddd_errors.h"


typedef QueueIter<Command> CommandQueueIter;

void checkIZ151763() {

    Command c;
    CommandQueueIter i(0);
    CommandQueueIter pos(0);

    while (i.ok() && c.priority <= i().priority) {
        pos = i;
        i = i.next();
    }

}
typedef QueueIter<struct string> StringQueueIter;

struct string {
    bool empty();
protected:
    int rep;
    // Split string into array RES at SEPARATORS; return number of elements
    friend int split(const string& x, string *res, int maxn);
};


void checkEmptyAfterColon() {
    string orig;
    string s;
    StringQueueIter iter(0);
    if (!(s = orig, s).empty()) {
        iter().empty();
    }
}

typedef struct _NODE NODE;

struct _NODE {
    NODE *left;             /* same level - to the left */
    NODE *right;            /* same level - to the right */
};

int operator + (int v1, int v2) {

}

int split(const string& src, string *results, int n)
{
    results[n].rep = src.rep;
    return 0;
}

void checkIZadf() {
    NODE **tmp;
    NODE* array;
    for (int i = 1; i < 100; i++) {
        (*tmp)->right = *(tmp + 1);
        (*(tmp + 1))->left = *tmp;
        tmp++;
    }
}

// Return A <= B
inline bool default_le(int a, int b) { return a <= b; }

// Sort A
static void sort(bool (*le)(int, int) = default_le)
{

}

static void static_foo1(){
}

typedef void (*pf_Static) ();

struct C_Static {
    static pf_Static f;
};

pf_Static C_Static::f = static_foo1; // unresolved

struct S_Static {
    void (*f)();
};

static void static_foo2(){
}

struct CC_Static {
    static S_Static s;
};

S_Static CC_Static::s =
{
    static_foo2, // unresolved
    default_le
};

int main() {
    CC_Static::s.f();
    C_Static::f();
    return 0;
}
