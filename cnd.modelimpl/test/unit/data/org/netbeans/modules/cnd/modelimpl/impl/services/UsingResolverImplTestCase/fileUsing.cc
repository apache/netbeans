
void nothingIsVisible() {
     // 
}

#include "fileNs2.h"

#include "fileNs1.h"

void beforeUsingNsTwoFun() {
     // 
}

void usingNsTwoInFun() {
     // 
    using namespace NsTwo;
     // 
}

using namespace NsTwo;

void afterUsingNsTwoFun() {
     // 
}
