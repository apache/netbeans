namespace bug243510 {
    constexpr class C3_243510 {} c3_243510 = C3_243510();
    constexpr struct S3_243510 {} s3_243510 = S3_243510();
    constexpr union U3_243510 {} u3_243510 = {};
    constexpr enum E3_243510 { V3_243510 } e3_243510 = V3_243510;
    class C4_243510 {} constexpr c4_243510 = C4_243510();
    struct S4_243510 {} constexpr s4_243510 = S4_243510();
    union U4_243510 {} constexpr u4_243510 = {};
    enum E4_243510 { V4_243510 } constexpr e4_243510 = V4_243510;
}
namespace bug243510_1 {
    enum bug243510::E4_243510 var_243510 = bug243510::V4_243510; 
}