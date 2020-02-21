#define Pragma(x) _Pragma(#x)
#define OMP(directive) Pragma(omp directive)

void main() {

    OMP(parallel) {
        int i;
    }
}
