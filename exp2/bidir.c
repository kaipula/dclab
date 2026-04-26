#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <sys/wait.h>

int main() {
    int p1[2], p2[2];
    char msg1[100], msg2[100];

    pipe(p1);   // Parent -> Child
    pipe(p2);   // Child -> Parent

    if (fork() == 0) {   // Child
        close(p1[1]); // close write end of p1
        read(p1[0], msg1, sizeof(msg1));
        printf("Child received: %s\n", msg1);
        close(p1[0]);

        close(p2[0]); // close read end of p2
        char reply[] = "Hello Parent, I got your message";
        write(p2[1], reply, strlen(reply) + 1);
        close(p2[1]);
    }
    else {              // Parent
        close(p1[0]); // close read end of p1
        char text[] = "Hello Child";
        write(p1[1], text, strlen(text) + 1);
        close(p1[1]);

        close(p2[1]); // close write end of p2
        read(p2[0], msg2, sizeof(msg2));
        printf("Parent received: %s\n", msg2);
        close(p2[0]);

        wait(NULL);
    }

    return 0;
}