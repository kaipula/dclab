#include <stdio.h> 
#include <unistd.h> 
#include <string.h> 
int main() { 
int fd[2]; 
char msg[100]; 
pipe(fd); // create pipe 
fork(); // create child 
if (fork() == 0) { 
close(fd[1]); // close write end 
read(fd[0], msg, sizeof(msg)); 
printf("Child received: %s\n", msg); 
} else { 
close(fd[0]); // close read end 
char text[] = "Hello from Parent"; 
write(fd[1], text, strlen(text)+1); 
} 
return 0; 
} 
