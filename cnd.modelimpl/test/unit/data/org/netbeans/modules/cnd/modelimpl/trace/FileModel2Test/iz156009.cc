#define LUAI_FUNC	__attribute__((visibility("hidden"))) extern
#define LUAI_DATA	LUAI_FUNC
LUAI_DATA const int luaO_nilobject_;