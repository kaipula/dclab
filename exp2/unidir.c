#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>

int main() {
    int fd[2];
    char msg[100];

    pipe(fd);   // Create pipe

    if (fork() == 0) {   // Child process
        close(fd[1]);    // Close write end
        read(fd[0], msg, sizeof(msg));
        printf("Child received: %s\n", msg);
        close(fd[0]);
    } 
    else {               // Parent process
        close(fd[0]);    // Close read end
        char text[] = "Hello from Parent";
        write(fd[1], text, strlen(text) + 1);
        close(fd[1]);
    }

    return 0;
}