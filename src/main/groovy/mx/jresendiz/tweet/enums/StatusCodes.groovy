package mx.jresendiz.tweet.enums

import groovy.transform.CompileStatic

@CompileStatic
enum StatusCodes {
    NOT_IMPLEMENTED(501),
    OK(201),
    NOT_FOUND(404),
    SERVER_ERROR(500)

    StatusCodes(int value) {
        this.value = value
    }
    private final int value

    int getValue() {
        value
    }

    String toString() {
        return name() + " = " + value
    }
}
