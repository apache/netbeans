#include <stdio.h>
#include "jni.h"

int main(int argc, char* argv[]) {
  if (sizeof(jint) == sizeof(jmethodID)) {
    puts("#undef NEEDS_CONVERSION");
  } else {
    puts("#define NEEDS_CONVERSION");
  }
  return 0;
}
