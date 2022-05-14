#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include <linux/input.h>
#include <fcntl.h>

#include <stdarg.h>
#include <stdint.h>
#include <string.h>

#include <linux/fb.h>
//#include <linux/vt.h>
//#include <linux/kd.h>
//#include <sys/stat.h>
#include <sys/mman.h>
//#include <sys/wait.h>


static struct input_event eventStruct() {
    struct input_event inputEvent;
    return inputEvent;
}

struct ReadBuffer {
    struct input_event event;
    ssize_t readBytes;
};

struct ReadBuffer readBuffer(int fd) {
    struct ReadBuffer readBuffer;
    struct input_event buffer;

    ssize_t readBytes = read(fd, &buffer, sizeof(struct input_event));

    readBuffer.event = buffer;
    readBuffer.readBytes = readBytes;

    return readBuffer;

}

static size_t getEventPointerSize() {
    return sizeof(struct input_event);
}


typedef void (*OnCharRead)(
        const char content,
        const char *error
);

typedef void (*OnLineRead)(
        const char *content,
        const char *error
);

static unsigned int readChar(const char *path, OnCharRead onCharRead) {
    FILE *file;

    file = fopen(path, "rb");

    const char *error = NULL;

    if (file) {
        char *character;
        while ((character = (char) fgetc(file)) != EOF) {
            onCharRead(character, NULL);
        }
        fclose(file);
        return 1;
    } else {
        char error[] = "Missing File: ";

        onCharRead("", strcat(error, path));
    }

    return 0;

}

static void readLine(const char *path, OnLineRead onLineRead) {
    FILE *file;

    file = fopen(path, "r");

    const char *error = NULL;


    if (file == NULL) {
        error = "FileMissingException";
        onLineRead("", strcat(error, path));
    } else {
        char character;

        while ((character = (char) fgetc(file)) != EOF) {
            onLineRead(character, error);

        }
    }


    fclose(file);

    return;

}

struct CommandResult {
    const char *result;
};



typedef void (*OnFail)(const char *message);

/*FEEL IT'S A BIT SLOW NOT SURE WHY YET BUT CODE DOESN'T LOOK GREAT EITHER*/
static char *execute(const char *command, OnFail onFail) {
    FILE *stream;

    stream = popen(command, "r");

    if (stream != NULL) {

        char *result;

        int i = 1;

        while (!feof(stream)) {
            char *current = result;
            result = (char *) malloc(i);
            int l = i - 1;
            result[l] = (char) fgetc(stream);;
            int c = 0;

            while (c < l) {
                result[c] = current[c];
                c++;
            }

            current = NULL;

            i++;

        }

        pclose(stream);

        return result;

    } else {
        char msg[] = "Can't execute command: ";
        strcat(msg, command);
        onFail(msg);
    }
}