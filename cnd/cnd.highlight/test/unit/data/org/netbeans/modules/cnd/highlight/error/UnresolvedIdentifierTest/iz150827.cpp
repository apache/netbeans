int ar0;
class TestArchive {
    template <class Archive>
    void serialize(Archive &ar, const unsigned int version) {
        ar & myInt;
        ar0 & myInt;
    }
    int myInt;
};
