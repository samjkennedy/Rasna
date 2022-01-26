#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>

int main()
{
    char* hello = "Hello, ";
    char* world = "World!";

    printf("%s/n", strcat(hello, world));
}