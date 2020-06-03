
namespace NSWithExtern {
    extern int extVar;
    extern int outVar;
}

using NSWithExtern::extVar;

namespace NSWithExtern {
    int extVar;
}

using NSWithExtern::outVar;