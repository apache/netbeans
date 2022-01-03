// NB! Not compilable case
namespace bug250325 {
    namespace tratata250325 {
        enum class BBB250325 : int {
            val250325
        };
    }
    struct ccc250325 {
        enum class tratata250325::BBB250325 : int; // error: opaque-enum-specifier must use a simple identifier 
        enum tratata250325::BBB250325; 
    };
}