//#include <stdio.h>
namespace iz143977_0 {
    class Object {
    public:
        const char* toString() const;
        int hashCode() const;
    };
    template<class K, class V = K> struct Pair {
        K first;
        V second;
    };
    int hash(Pair<Object> pair) {
        int hash = 7;
        hash = 79 * hash + pair.first.hashCode(); // hashCode should be resolved 
        hash = 79 * hash + pair.second.hashCode(); // hashCode should be resolved 
        return hash;
    }
    template<class K, class V = Object> class Map {
    public:
        V& operator [] (K& key);
    };
    Object printValue(Map<Object> m, Object key) {
        printf("value = %s\n", m[key].toString()); // toString should be resolved
        return m[key];
    }
}
