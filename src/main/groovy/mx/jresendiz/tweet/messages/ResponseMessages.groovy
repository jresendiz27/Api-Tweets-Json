package mx.jresendiz.tweet.messages

import groovy.transform.CompileStatic
import io.vertx.core.json.JsonObject
import mx.jresendiz.tweet.enums.StatusCodes

@CompileStatic
class ResponseMessages {
    static JsonObject serverMessage(Integer code, String description, String message = "") {
        JsonObject jsonObject = new JsonObject()
            .put("code", code)
            .put("description", description)
            .put("message", message)

        return jsonObject
    }

    static JsonObject notFound() {
        return serverMessage(StatusCodes.NOT_FOUND.value, StatusCodes.NOT_FOUND.toString())
    }

    static JsonObject notImplemented() {
        return serverMessage(StatusCodes.NOT_IMPLEMENTED.value, StatusCodes.NOT_IMPLEMENTED.toString())
    }

    static JsonObject serverError() {
        return serverMessage(StatusCodes.SERVER_ERROR.value, StatusCodes.SERVER_ERROR.toString())
    }

    static JsonObject serverError(String errorMessage) {
        return serverMessage(StatusCodes.SERVER_ERROR.value, StatusCodes.SERVER_ERROR.toString(), errorMessage)
    }

}
