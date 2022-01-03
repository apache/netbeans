
namespace N{
    int i;
};

typedef int (N::*P);

int main(){
    P p = &N::i;
    (*p)++;

    return 0;
}
