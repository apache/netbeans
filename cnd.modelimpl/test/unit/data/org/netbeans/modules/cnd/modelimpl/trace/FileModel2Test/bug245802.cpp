namespace bug245802 {
    struct A245802 {
        bool operator < (const A245802& r) const;
    };

    bool A245802::operator < (const A245802& r) const {
        return false;
    }
}