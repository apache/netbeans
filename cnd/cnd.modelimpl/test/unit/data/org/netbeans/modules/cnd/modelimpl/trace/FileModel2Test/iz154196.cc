class Student {
public:
    char name[100];
};

int main() {
    Student jackRole(), jillRole(); // jillRole is unresolved
    Student b("value"), bbb("val", false);
    return 0;
}
