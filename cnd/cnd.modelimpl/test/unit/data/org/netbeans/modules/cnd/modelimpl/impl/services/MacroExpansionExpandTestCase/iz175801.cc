#define __ERRDEF(_n, _d) { #_n, _d, L## #_n, L##_d }

typedef enum
{
    CR_OK = 0,
    CR_EEXISTS,
    CR_ELAST
} status_t;

static const struct
{
    const char *name;
    const char *desc;
    const wchar_t *wname;
    const wchar_t *wdesc;
} err_list[CR_ELAST] = {
    __ERRDEF (CR_OK, "no error"),
    __ERRDEF (CR_EEXISTS, "object already exists")
};
