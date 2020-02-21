#define RELEASE Sun
#define VERSION 10
#define VER_BUILD_NAME Aten
#define VER_BUILD_DATE 11/11/2011
#define VER_MAGIC_PREFIX @(#)RELEASE VERSION
#define _VERSION_ALL_TOKS VER_BUILD_NAME VER_BUILD_DATE
#define _version_str(a) # a
#define _version_xstr(a) _version_str(a)
#define VER_STRING_WITH_MAGIC _version_xstr(VER_MAGIC_PREFIX _VERSION_ALL_TOKS)

int main(int argc, char** argv) {

    char* str = VER_STRING_WITH_MAGIC;
    return 0;
}

#define BOOST_SPIRIT_LOG2 (#error)(#error) (2) (3)

#define YYY (#error) #x + 1 + #y
