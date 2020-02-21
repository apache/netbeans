int (*get_ptr(const char opCode))(int, int);

int plus(int x, int y)  { return x + y; }
int minus(int x, int y) { return x - y; }

int (*get_ptr(const char opCode))(int, int) {
	if(opCode == '+')
		return &plus;
	else
		return &minus;
}

static void (* __set_malloc_handler(void (*__f)()))()
{
   return 0;
}

int *(* genxGetAlloc(genxWriter ))(void * , int );

int *(* genxGetAlloc(genxWriter w))(void * userData, int bytes)
{
  return w->alloc;
}


