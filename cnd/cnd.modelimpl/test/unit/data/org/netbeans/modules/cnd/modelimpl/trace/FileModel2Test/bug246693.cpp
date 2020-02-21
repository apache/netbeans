namespace bug246693 {
    int main246693() {
        // this is C99 feature, but both gcc and clang have it in C++
        int var = (int) {0}; 
        if ((int) {var} == 5)  {
            return 0;
        }
        return 0;
    }
}