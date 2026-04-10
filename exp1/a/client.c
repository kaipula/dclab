#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>

int main() {
    int sock_fd;
    struct sockaddr_in server_addr;
    char buffer[1024];

    sock_fd = socket(AF_INET, SOCK_STREAM, 0);

    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(8080);
    inet_pton(AF_INET, "10.10.18.36", &server_addr.sin_addr);

    connect(sock_fd, (struct sockaddr*)&server_addr, sizeof(server_addr));

    strcpy(buffer, "Hello Server");
    send(sock_fd, buffer, strlen(buffer), 0);

    recv(sock_fd, buffer, sizeof(buffer), 0);
    printf("Message from server: %s\n", buffer);

    close(sock_fd);

    return 0;
}

