#if 1 % 0
@#if A
@#endif
int b;
#endif

int main() {
   _asm {
	movd dx, 5             ; A.....B
   }
   return 0;
}

