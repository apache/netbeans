int o[] = {
        1,  2
};

int o1[] = {
        1,  2,
};

int options[][2] = {
{ 3,              4 },
{ 5,              6 },
};

struct Menu {
    int a;  
    int b;
} m;


Menu menus[] = {
  { 7, 8},  
  { 9, 10},
};

struct Opt {
    int a;
    int b;
};

struct MMDesc {
    int a;  
    int b;
    Opt c;  
};

#define RECENT_MENU \
{ \
    { 1, 2, {3, 4} }, \
    { 5, 6, {7, 8} }, \
}

MMDesc command_recent_menu[] = RECENT_MENU;

void foo() {
    
}

