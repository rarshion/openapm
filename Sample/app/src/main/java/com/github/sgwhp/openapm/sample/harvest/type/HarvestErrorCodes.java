package com.github.sgwhp.openapm.sample.harvest.type;

/**
 * Created by user on 2016/8/1.
 */
public interface HarvestErrorCodes {
    public static final int NSURLErrorUnknown = -1;
    public static final int NSURLErrorBadURL = -1000;
    public static final int NSURLErrorTimedOut = -1001;
    public static final int NSURLErrorCannotFindHost = -1003;
    public static final int NSURLErrorCannotConnectToHost = -1004;
    public static final int NSURLErrorDNSLookupFailed = -1006;
    public static final int NSURLErrorBadServerResponse = -1011;
    public static final int NSURLErrorRequestBodyStreamExhausted = -1021;
    public static final int NRURLErrorFileDoesNotExist = -1100;
    public static final int NSURLErrorSecureConnectionFailed = -1200;
}
