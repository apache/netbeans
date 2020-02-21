#include <s1.h>//include 1
#include "h2.h" //include 2
#include <s3.h>/*include 3*/
#include "h4.h" /*include 
                    * 4
                        */
			
#include <s11.h>//include 11

#include "h21.h" //include 21

#include <s31.h>/*include 31*/

#include "h41.h" /*include 
                    * 41
                        */
						
#include <!!!Incorrent file #1,...>
#include "!!!Incorrent file #2,..."

#if 42 != 1 && 42 != 42
#   include "!!!Incorrent file #3,..."
#endif 
			
			
#define M7 \
7 /*definition*/ 77 \
777 // macro 7
#define M1 1 // macro 1
#define M2 2 // macro 2

#define M3 /* macro 3 */ 3
#define M4 4 // macro 4

#define M5 /* 
                macro 
            * 5
            */ 5  

#define M6 6// macro 6#define M6 // macro 6

#define MAX(a, b) ((a) < (b) ? (b) : (a)) // max 1

#define TR(t) int /*this is definition*/t;

#define TR1(t) int/*definition*/t;

#ifdef M5
#define M5_YES
#else
#define M5_NO
#endif

#if defined(a)

/* pp #elif */

#endif

void hello() {
    MAX(7, 9);
    TR(a);
}

#ifdef A

#endif /* do not put new line after it! It's a test */
