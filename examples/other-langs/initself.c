#include <stdio.h>
char *a = "outer";

void b() {
   char *a = a;
   
   char *b = "oh";
   {
       char *b = b;
       printf("b %s\n", b);
   }
   
   printf("a %s\n", a);
}

int main() {
b();
}
