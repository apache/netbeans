namespace bug243522 {
    struct OK_243522 {
      constexpr OK_243522() {}
      constexpr operator int() { return 8; }
    } constexpr ok_243522;

    // [dcl.align]p2: When the alignment-specifier is of the form
    // alignas(assignment-expression), the assignment-expression shall be an
    // integral constant expression
    int alignas(ok_243522) alignas1_243522;
}