namespace bug257030 {
    template <typename T>
    void print257030(const T& values) {
        for (const auto& value : values) {
            value->getV(); // unresolved identifier 'getV'
        }
    }  
}
