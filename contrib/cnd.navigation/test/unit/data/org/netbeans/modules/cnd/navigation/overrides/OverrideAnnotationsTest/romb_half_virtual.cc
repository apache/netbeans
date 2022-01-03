struct BaseRomb {
    virtual int foo(int) = 0;
    int boo(int);
};
int BaseRomb::boo(int){return 1;}

struct CatRomb : public BaseRomb {
    int foo(int);
    int boo(int);
};
int CatRomb::foo(int){return 2;}
int CatRomb::boo(int){return 3;}

struct DogRomb : public BaseRomb {
    int foo(int);
    virtual int boo(int);
};
int DogRomb::foo(int){return 4;}
int DogRomb::boo(int){return 5;}

struct CatDogRomb : public CatRomb, DogRomb {
    int foo(int);
    int boo(int);
};
int CatDogRomb::foo(int){return 6;}
int CatDogRomb::boo(int){return 7;}
