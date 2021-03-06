package il.ac.bgu.se.bp.utils;

import il.ac.bgu.se.bp.error.ErrorCode;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

import java.util.concurrent.FutureTask;

public class FutureHelper {

    public static FutureTask<BooleanResponse> createResolvedSuccessFuture() {
        return resolveFuture(new FutureTask<>(FutureHelper::createSuccessResult));
    }

    public static FutureTask<BooleanResponse> createResolvedErrorFuture(final ErrorCode errorCode) {
        return resolveFuture(new FutureTask<>(() -> createErrorResult(errorCode)));
    }

    private static <T> FutureTask<T> resolveFuture(FutureTask<T> future) {
        future.run();
        return future;
    }

    public static BooleanResponse createSuccessResult() {
        return new BooleanResponse(true);
    }

    private static BooleanResponse createErrorResult(ErrorCode errorCode) {
        return new BooleanResponse(false, errorCode);
    }


}
