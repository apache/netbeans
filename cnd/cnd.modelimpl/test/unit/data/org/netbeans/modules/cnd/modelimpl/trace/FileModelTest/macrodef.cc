#include "macrodef.h"

#define M1 hello
#include "macrodef.h"

void M1() {
    MAX(7, 9);
    TR(a);
}
