class A
{
public:
  void pub() { }
protected:
  void pro() { }
private:
  void pri() { }
};

class B : private A
{
public:
  using A::pub;
  using A::pro;
  using A::pri; // should cause error, does not
};

class C : protected A
{
public:
  using A::pub;
  using A::pro;
  using A::pri; // should cause error, does not
};

class D : public A
{
public:
  using A::pub;
  using A::pro;
  using A::pri; // should cause error, does not
};

/*
 *
 */
int main(int argc, char** argv)
{
  B b;
  C c;
  D d;

  b.pub(); // Unable to resolve identifier
  c.pub(); // Unable to resolve identifier
  d.pub(); // works

  b.pro(); // Unable to resolve identifier
  c.pro(); // Unable to resolve identifier
  d.pro(); // Unable to resolve identifier

  b.pri(); // Unable to resolve identifier
  c.pri(); // Unable to resolve identifier
  d.pri(); // Unable to resolve identifier

  return 0;
}