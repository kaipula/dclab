#include <stdio.h> 
#include <unistd.h> 
#include <string.h> 
int main() { 
int p1[2], p2[2]; 
char msg1[100], msg2[100]; 
pipe(p1); // Parent to Child 
pipe(p2); // Child to Parent 
if (fork() == 0) { 
close(p1[1]); 
read(p1[0], msg1, sizeof(msg1)); 
printf("Child received: %s\n", msg1); 
close(p2[0]); 
char reply[] = "Hello Parent, I got your message"; 
write(p2[1], reply, strlen(reply)+1); 
} else { 
close(p1[0]); 
char text[] = "Hello Child"; 
write(p1[1], text, strlen(text)+1); 
close(p2[1]); 
read(p2[0], msg2, sizeof(msg2)); 
printf("Parent received: %s\n", msg2); 
} 
return 0; 
}
