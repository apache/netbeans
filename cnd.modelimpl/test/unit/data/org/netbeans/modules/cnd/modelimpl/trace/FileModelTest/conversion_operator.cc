namespace ns {
    typedef char* string;
}

class C {
private:
    char* msg;
public:
    C(char* msg_) : msg(msg_) {}
    operator const char*() const {
        return msg;
    }
    operator ns::string() const {
        return msg;
    }
    ns::string toString() const {
        return this->operator ns::string();
    }
    const char* toCharPtr() const {
        return this->operator const char*();
    }
};
