
class Database {
public:
    template <class T> 
        int cursor(int i) {
        return 0;
    }
};

class DataSource {
public:
    template <class T>
    int cursor(int i) {
        Database db;
        return db.template cursor<T> (1);
    }
};
