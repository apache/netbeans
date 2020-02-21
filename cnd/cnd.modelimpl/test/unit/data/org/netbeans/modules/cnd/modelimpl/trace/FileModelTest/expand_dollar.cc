#define $(type, x) type* x = new type

class widget {
public:
  widget() {}
  void show() {}
};

int main() {
    $(widget, x);
    x->show();
    return 0;
}
