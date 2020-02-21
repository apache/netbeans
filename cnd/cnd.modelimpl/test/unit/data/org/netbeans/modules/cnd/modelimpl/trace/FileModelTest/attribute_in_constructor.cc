
#define BOOST_USED  __attribute__ ((used));

template<class T, class Archive>
class pointer_oserializer {
private:
    explicit pointer_oserializer() BOOST_USED;
};

class object {
public:
    __attribute__ ((visibility("default"))) explicit object() {} // line is highlighted as error
};
