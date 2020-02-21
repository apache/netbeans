#define MY_TEST_PARAM 1
#define F_TEST_PARAM defined(MY_TEST_PARAM)

#if F_TEST_PARAM
void
function() {
}
#endif /* F_TEST_PARAM */
