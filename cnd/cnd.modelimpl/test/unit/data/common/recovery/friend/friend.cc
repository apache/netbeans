
namespace clang {

    class FullSourceLoc {
        unsigned ID;
    public:

        explicit FullSourceLoc() : ID(0) {
        }

        unsigned getRawEncoding() const {
            return ID;
        }

        friend inline bool
        operator==(const FullSourceLoc &LHS, const FullSourceLoc &RHS) {
            return LHS.getRawEncoding() == RHS.getRawEncoding();
        }

        friend inline bool
        operator!=(const FullSourceLoc &LHS, const FullSourceLoc &RHS) {
            return !(LHS == RHS);
        }
    };
}
