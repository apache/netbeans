/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <unistd.h>
//#include <assert.h>

#include "../src/fs_common.h"
#include "../src/queue.h"
#include "../src/dirtab.h"
#include "../src/array.h"

static void _assert_true(bool condition, const char* conditionText) {
    if (condition) {
        fprintf(stdout, "Check for (%s) passed.\n", conditionText);
        fflush(stdout);
    } else {
        fprintf(stderr, "Check for (%s) FAILED.\n", conditionText);
        fflush(stderr);
        exit(1);
    }
}

#define assert_true(condition) _assert_true(condition, #condition)

static void test_list() {
    queue l;
    queue_init(&l);
    assert_true(queue_size(&l) == 0);
    assert_true(l.head == NULL);
    assert_true(l.tail == NULL);
    assert_true(queue_poll(&l)== NULL);
    queue_add(&l, "a");
    assert_true(queue_size(&l) == 1);
    assert_true(l.head== l.tail);
    const char* s;
    s = queue_poll(&l);
    assert_true(s != NULL);
    assert_true(queue_size(&l) == 0);
    assert_true(strcmp(s, "a") == 0);
    queue_add(&l, "a");
    s = queue_poll(&l);
    assert_true(strcmp(s, "a") == 0);
    assert_true(l.head== l.tail);    
    queue_add(&l, "A");
    queue_add(&l, "B");
    assert_true(queue_size(&l) == 2);
    queue_add(&l, "C");
    assert_true(queue_size(&l) == 3);
    
    s = queue_poll(&l);
    assert_true(s != NULL);
    assert_true(strcmp(s, "A") == 0);
    
    s = queue_poll(&l);
    assert_true(s != NULL);
    assert_true(strcmp(s, "B") == 0);
    
    s = queue_poll(&l);
    assert_true(s != NULL);
    assert_true(strcmp(s, "C") == 0);    

//    assert_true(list_size(&l) == 3);
//    assert_true(strcmp(l.tail->data, "c") == 0);
}

static void test_dirtab_get_cache(const char* path, const char* reference_cache_path, int passes) {
    int i;
    for (i = 0; i < passes; i++) {
        dirtab_element* el = dirtab_get_element(path);
        const char *cache_path = dirtab_get_element_cache_path(el);
        fprintf(stdout, "%s -> %s\n", path, cache_path);
        assert_true(strcmp(cache_path, reference_cache_path) == 0);        
    }
    fflush(stdout);
}

static void test_dirtab_2() {
    fprintf(stdout, "testing dirtab persistence...\n");
    dirtab_init(false, DE_WSTATE_NONE);
    assert_true(chdir(dirtab_get_basedir())== 0);
    test_dirtab_get_cache("/home/xxx123", "cache/1024", 3);
}

static void test_dirtab_1() {
    assert_true(system("rm -rf ~/.netbeans/remotefs") == 0);        
    fprintf(stdout, "testing dirtab...\n");
    dirtab_init(false, DE_WSTATE_NONE);
    assert_true(chdir(dirtab_get_basedir())== 0);
    test_dirtab_get_cache("/home", "cache/0", 2);
    
    int i;
    for (i = 1; i < 1024; i++) {
        char path[32];
        sprintf(path, "/home/%d", i);
        char reference_cache_path[32];
        sprintf(reference_cache_path, "cache/%d", i);
        test_dirtab_get_cache(path, reference_cache_path, 3);
    }
    
    test_dirtab_get_cache("/home/xxx123", "cache/1024", 3);
       
    fprintf(stdout, "storing dirtab...\n");
    fflush(stdout);
    assert_true(dirtab_flush());
}

static int string_comparator (const void *element1, const void *element2) {
    const char *str1 = *((char**)element1);
    const char *str2 = *((char**)element2);
    int res = strcmp(str1, str2);
    return res;
}

static const void *string_finder(const void *element, void* arg) {
    const char *p = element;
    if (strcmp(p, arg) == 0) {
        return p;
    }
    return NULL;
}

static void test_array() {
    
   array a;
   array_init(&a, 4);
   assert_true(array_size(&a) == 0);
   
   array_add(&a, "4");
   assert_true(array_size(&a) == 1);
   assert_true(strcmp(array_get(&a, 0), "4") == 0);
   
   array_add(&a, "2");
   assert_true(array_size(&a) == 2);
   assert_true(strcmp(array_get(&a, 0), "4") == 0);
   assert_true(strcmp(array_get(&a, 1), "2") == 0);
   
   array_add(&a, "1");
   assert_true(array_size(&a) == 3);
   assert_true(strcmp(array_get(&a, 0), "4") == 0);
   assert_true(strcmp(array_get(&a, 1), "2") == 0);
   assert_true(strcmp(array_get(&a, 2), "1") == 0);
   
   array_add(&a, "3");
   assert_true(array_size(&a) == 4);
   assert_true(strcmp(array_get(&a, 0), "4") == 0);
   assert_true(strcmp(array_get(&a, 1), "2") == 0);
   assert_true(strcmp(array_get(&a, 2), "1") == 0);
   assert_true(strcmp(array_get(&a, 3), "3") == 0);
   
   array_qsort(&a, string_comparator);

   assert_true(strcmp(array_get(&a, 0), "1") == 0);
   assert_true(strcmp(array_get(&a, 1), "2") == 0);
   assert_true(strcmp(array_get(&a, 2), "3") == 0);
   assert_true(strcmp(array_get(&a, 3), "4") == 0);
   
   array_add(&a, "a");
   array_add(&a, "x");
   array_add(&a, "b");
   array_add(&a, "y");
   array_add(&a, "c");
   array_add(&a, "z");
   array_qsort(&a, string_comparator);
   
   assert_true(strcmp(array_iterate(&a, string_finder, "z"), "z") == 0);
}

static void test_escape_unescape(const char *unescaped, const char* escaped) {
    bool failed = false;
    char buf[2048];
    escape_strcpy(buf, unescaped);
    if (strcmp(buf, escaped) == 0) {
        fprintf(stdout, "check for escape_strcpy(\"%s\") == \"%s\" passed\n", unescaped, escaped);
    } else {
        fprintf(stderr, "check for escape_strcpy(\"%s\") == \"%s\" FAILED: got \"%s\"\n", unescaped, escaped, buf);
        failed = true;
    }
    unescape_strcpy(buf, escaped);
    if (strcmp(buf, unescaped) == 0) {
        fprintf(stdout, "check for unescape_strcpy(\"%s\") == \"%s\" passed\n", escaped, unescaped);
    } else {
        fprintf(stderr, "check for unescape_strcpy(\"%s\") == \"%s\" FAILED: got \"%s\"\n", escaped, unescaped, buf);
        failed = true;
    }

    {
        int ref_escaped_len = strlen(escaped);
        int escaped_len = escape_strlen(unescaped);

        if (escaped_len == ref_escaped_len) {
            fprintf(stdout, "check for escape_strlen(\"%s\") == %d passed\n", unescaped, ref_escaped_len);
        } else {
            fprintf(stderr, "check for escape_strlen(\"%s\") == %d FAILED: got %d\n", unescaped, ref_escaped_len, escaped_len);
            failed = true;
        }
    }

    {
        int ref_unescaped_len = strlen(unescaped);
        int unescaped_len = unescape_strlen(escaped);
        if (unescaped_len == ref_unescaped_len) {
            fprintf(stdout, "check for unescape_strlen(\"%s\") == %d passed\n", escaped, ref_unescaped_len);
        } else {
            fprintf(stderr, "check for unescape_strlen(\"%s\") == %d FAILED: got %d\n", escaped, ref_unescaped_len, unescaped_len);
            failed = true;
        }
    }
    if (failed) {
        exit(2);
    }
}

static void test_escapes() {
    test_escape_unescape("ab\ncd", "ab\\ncd");
    test_escape_unescape("", "");
    test_escape_unescape("qwe", "qwe");
    test_escape_unescape("\n", "\\n");
    test_escape_unescape("xx\n", "xx\\n");
    test_escape_unescape("\nyy", "\\nyy");
    test_escape_unescape("\\", "\\\\");
    test_escape_unescape("\\\\", "\\\\\\\\");
    test_escape_unescape("123\\", "123\\\\");    
}

static void test_subdir() {
    assert_true(is_subdir("/home", ""));
    assert_true(is_subdir("/home", "/"));
    assert_true(is_subdir("", "/"));
    assert_true(is_subdir("/", ""));
    assert_true(is_subdir("/home/vk/123", "/home/vk"));
    assert_true(is_subdir("/home/vk/123/", "/home/vk/"));
    assert_true(is_subdir("/home/vk/123", "/home/vk/"));
    assert_true(is_subdir("/home/vk/123/", "/home/vk"));
    assert_true(is_subdir("/home//", "/home/"));
    assert_true(is_subdir("/home///", "/home/"));
    assert_true(is_subdir("/home/", "/home//"));
    assert_true(is_subdir("/home/", "/home///"));
    assert_true(is_subdir("/home/", "/home"));
    assert_true(!is_subdir("/home/", "/hom"));
    assert_true(!is_subdir("/hom", "/home"));
    assert_true(!is_subdir("/homemaid", "/home"));
    //assert_true(false);
}

int main(int argc, char** argv) {
    test_subdir();
    test_escapes();
    test_array();
    test_list();
    test_dirtab_1();
    test_dirtab_2();
    return (EXIT_SUCCESS);
}

