#define _ASSERT_VOID_CAST(x) (void)(x)

#define likely(x) (x)
#define unlikely(x) (x)

void ddd_assert_fail (const char *assertion, const char *file,
		      unsigned int line, const char *function);


#define _assert_fn ""


#define assert(ex) \
_ASSERT_VOID_CAST(unlikely(ex) ? \
  0 : \
  (ddd_assert_fail (#ex, __FILE__, __LINE__, _assert_fn), 0) \
  )
  
void foo() {
    assert(1    ||              
	   2);
	assert(1 + 3 <=
	       6);    
    ddd_assert_fail("1\"1a""2"  "3"             
                        "4"
                        "5"
                         "6", 0, 0, 0);
    assert ("1\"1a23456");
    assert ("1\"1a""2"  "3"             
                        "4"
                        "5"
                         "6");
}
