/*

Class hierarchy:

Hierarchy       v_foo presence
---------       --------------
A               +
|
+-->B1          +
|   +-->B1C1    +
|   +-->B1C2
|   +-->B1C3
+-->B2
|   +-->B2C1
|   +-->B2C2    +
|   +-->B2C3    +
+-->B3
    +-->B3C1
    +-->B3C2
    +-->B3C3

*/

class B1C1 {
    virtual void v_foo();
};

class B1C2 {
};

class B1C3 {    
};

class B1 : B1C1, B1C2, B1C3 {
    virtual void v_foo();
};

class B2C1 {
};

class B2C2 {
    virtual void v_foo();
};

class B2C3 {
    virtual void v_foo();
};

class  B2: B2C1, B2C2, B2C3 {
};

class B3C1 {
};

class B3C2 {
};

class B3C3 {
};

class B3 : B3C1, B3C2, B3C3 {
};

class A : B1, B2, B3 {
    void v_foo();
};
