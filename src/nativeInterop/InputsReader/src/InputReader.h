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

typedef void (*OnEvent)(const char *description, struct input_event, const char *error);


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


static int readEvent(const char *eventPath, OnEvent onEvent) {

    int fileId = open(eventPath, O_RDONLY);
    struct input_event inputEvent;

    if (fileId == -1) {
        onEvent("Failed to read events from:", inputEvent, eventPath);
        return -1;
    }

    while (read(fileId, &inputEvent, sizeof(struct input_event))) {
        onEvent("Success", inputEvent, NULL);
    }

    close(fileId);


    return 1;

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


typedef void (*OnFail)(const char *message);

/*FEEL IT'S A BIT SLOW NOT SURE WHY YET BUT CODE DOESN'T LOOK GREAT EITHER*/
static char *execute(const char *command, OnFail onFail) {
    FILE *file;

    file = popen(command, "r");

    if (command) {
        char *result;

        int i = 1;

        while (!feof(file)) {
            char *current = result;
            result = (char *) malloc(i);
            int l = i - 1;
            result[l] = (char) fgetc(file);;
            int c = 0;

            while (c < l) {
                result[c] = current[c];
                c++;
            }

            current = NULL;

            i++;
        }

        pclose(file);

        return result;

    } else {
        char msg[] = "Can't execute command: ";
        strcat(msg, command);
        onFail(msg);
    }
}