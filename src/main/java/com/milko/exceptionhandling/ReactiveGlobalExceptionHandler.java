package com.milko.exceptionhandling;

import com.milko.exception.EntityNotFoundException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Singleton
public class ReactiveGlobalExceptionHandler implements ExceptionHandler<Throwable, Mono<HttpResponse<?>>> {

    @Override
    public Mono<HttpResponse<?>> handle(HttpRequest request, Throwable exception) {
        ErrorResponse errorResponse;

        if (exception instanceof EntityNotFoundException){
            errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    "404",
                    "EntityNotFoundException",
                    exception.getMessage(),
                    request.getPath()
            );
            return Mono.just(HttpResponse
                    .notFound(errorResponse));
        }

        if (exception instanceof R2dbcDataIntegrityViolationException){
            errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    "409",
                    "R2dbcDataIntegrityViolationException",
                    exception.getMessage(),
                    request.getPath()
            );
            return Mono.just(HttpResponse
                    .status(HttpStatus.CONFLICT)
                    .body(errorResponse));
        }

        return Mono.just(HttpResponse.serverError(new ErrorResponse(
                LocalDateTime.now(),
                "500",
                exception.getClass().getName(),
                exception.getMessage(),
                request.getPath()
        )));
    }
}
