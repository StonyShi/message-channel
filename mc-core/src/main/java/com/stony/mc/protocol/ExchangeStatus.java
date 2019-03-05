package com.stony.mc.protocol;

/**
 * <p>mc-core
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午3:30
 * @since 2019/1/3
 */
public class ExchangeStatus implements Comparable<ExchangeStatus> {
    /**
     * 100 Continue
     */
    public static final ExchangeStatus CONTINUE = new ExchangeStatus(100, "Continue");

    /**
     * 101 Switching Protocols
     */
    public static final ExchangeStatus SWITCHING_PROTOCOLS = new ExchangeStatus(101, "Switching Protocols");

    /**
     * 102 Processing (WebDAV, RFC2518)
     */
    public static final ExchangeStatus PROCESSING = new ExchangeStatus(102, "Processing");

    /**
     * 200 OK
     */
    public static final ExchangeStatus OK = new ExchangeStatus(200, "OK");

    /**
     * 201 Created
     */
    public static final ExchangeStatus CREATED = new ExchangeStatus(201, "Created");

    /**
     * 202 Accepted
     */
    public static final ExchangeStatus ACCEPTED = new ExchangeStatus(202, "Accepted");

    /**
     * 203 Non-Authoritative Information (since HTTP/1.1)
     */
    public static final ExchangeStatus NON_AUTHORITATIVE_INFORMATION =
            new ExchangeStatus(203, "Non-Authoritative Information");

    /**
     * 204 No Content
     */
    public static final ExchangeStatus NO_CONTENT = new ExchangeStatus(204, "No Content");

    /**
     * 205 Reset Content
     */
    public static final ExchangeStatus RESET_CONTENT = new ExchangeStatus(205, "Reset Content");

    /**
     * 206 Partial Content
     */
    public static final ExchangeStatus PARTIAL_CONTENT = new ExchangeStatus(206, "Partial Content");

    /**
     * 207 Multi-ExchangeStatus (WebDAV, RFC2518)
     */
    public static final ExchangeStatus MULTI_STATUS = new ExchangeStatus(207, "Multi-Status");

    /**
     * 300 Multiple Choices
     */
    public static final ExchangeStatus MULTIPLE_CHOICES = new ExchangeStatus(300, "Multiple Choices");

    /**
     * 301 Moved Permanently
     */
    public static final ExchangeStatus MOVED_PERMANENTLY = new ExchangeStatus(301, "Moved Permanently");

    /**
     * 302 Found
     */
    public static final ExchangeStatus FOUND = new ExchangeStatus(302, "Found");

    /**
     * 303 See Other (since HTTP/1.1)
     */
    public static final ExchangeStatus SEE_OTHER = new ExchangeStatus(303, "See Other");

    /**
     * 304 Not Modified
     */
    public static final ExchangeStatus NOT_MODIFIED = new ExchangeStatus(304, "Not Modified");

    /**
     * 305 Use Proxy (since HTTP/1.1)
     */
    public static final ExchangeStatus USE_PROXY = new ExchangeStatus(305, "Use Proxy");

    /**
     * 307 Temporary Redirect (since HTTP/1.1)
     */
    public static final ExchangeStatus TEMPORARY_REDIRECT = new ExchangeStatus(307, "Temporary Redirect");

    /**
     * 400 Bad Request
     */
    public static final ExchangeStatus BAD_REQUEST = new ExchangeStatus(400, "Bad Request");

    /**
     * 401 Unauthorized
     */
    public static final ExchangeStatus UNAUTHORIZED = new ExchangeStatus(401, "Unauthorized");

    /**
     * 402 Payment Required
     */
    public static final ExchangeStatus PAYMENT_REQUIRED = new ExchangeStatus(402, "Payment Required");

    /**
     * 403 Forbidden
     */
    public static final ExchangeStatus FORBIDDEN = new ExchangeStatus(403, "Forbidden");

    /**
     * 404 Not Found
     */
    public static final ExchangeStatus NOT_FOUND = new ExchangeStatus(404, "Not Found");

    /**
     * 405 Method Not Allowed
     */
    public static final ExchangeStatus METHOD_NOT_ALLOWED = new ExchangeStatus(405, "Method Not Allowed");

    /**
     * 406 Not Acceptable
     */
    public static final ExchangeStatus NOT_ACCEPTABLE = new ExchangeStatus(406, "Not Acceptable");

    /**
     * 407 Proxy Authentication Required
     */
    public static final ExchangeStatus PROXY_AUTHENTICATION_REQUIRED =
            new ExchangeStatus(407, "Proxy Authentication Required");

    /**
     * 408 Request Timeout
     */
    public static final ExchangeStatus REQUEST_TIMEOUT = new ExchangeStatus(408, "Request Timeout");

    /**
     * 409 Conflict
     */
    public static final ExchangeStatus CONFLICT = new ExchangeStatus(409, "Conflict");

    /**
     * 410 Gone
     */
    public static final ExchangeStatus GONE = new ExchangeStatus(410, "Gone");

    /**
     * 411 Length Required
     */
    public static final ExchangeStatus LENGTH_REQUIRED = new ExchangeStatus(411, "Length Required");

    /**
     * 412 Precondition Failed
     */
    public static final ExchangeStatus PRECONDITION_FAILED = new ExchangeStatus(412, "Precondition Failed");

    /**
     * 413 Request Entity Too Large
     */
    public static final ExchangeStatus REQUEST_ENTITY_TOO_LARGE =
            new ExchangeStatus(413, "Request Entity Too Large");

    /**
     * 414 Request-URI Too Long
     */
    public static final ExchangeStatus REQUEST_URI_TOO_LONG = new ExchangeStatus(414, "Request-URI Too Long");

    /**
     * 415 Unsupported Media EventType
     */
    public static final ExchangeStatus UNSUPPORTED_MEDIA_TYPE =
            new ExchangeStatus(415, "Unsupported Media EventType");

    /**
     * 416 Requested Range Not Satisfiable
     */
    public static final ExchangeStatus REQUESTED_RANGE_NOT_SATISFIABLE =
            new ExchangeStatus(416, "Requested Range Not Satisfiable");

    /**
     * 417 Expectation Failed
     */
    public static final ExchangeStatus EXPECTATION_FAILED = new ExchangeStatus(417, "Expectation Failed");

    /**
     * 422 Unprocessable Entity (WebDAV, RFC4918)
     */
    public static final ExchangeStatus UNPROCESSABLE_ENTITY = new ExchangeStatus(422, "Unprocessable Entity");

    /**
     * 423 Locked (WebDAV, RFC4918)
     */
    public static final ExchangeStatus LOCKED = new ExchangeStatus(423, "Locked");

    /**
     * 424 Failed Dependency (WebDAV, RFC4918)
     */
    public static final ExchangeStatus FAILED_DEPENDENCY = new ExchangeStatus(424, "Failed Dependency");

    /**
     * 425 Unordered Collection (WebDAV, RFC3648)
     */
    public static final ExchangeStatus UNORDERED_COLLECTION = new ExchangeStatus(425, "Unordered Collection");

    /**
     * 426 Upgrade Required (RFC2817)
     */
    public static final ExchangeStatus UPGRADE_REQUIRED = new ExchangeStatus(426, "Upgrade Required");

    /**
     * 431 Request Header Fields Too Large (RFC6585)
     */
    public static final ExchangeStatus REQUEST_HEADER_FIELDS_TOO_LARGE =
            new ExchangeStatus(431, "Request Header Fields Too Large");

    /**
     * 500 Internal Server Error
     */
    public static final ExchangeStatus INTERNAL_SERVER_ERROR =
            new ExchangeStatus(500, "Internal Server Error");

    /**
     * 501 Not Implemented
     */
    public static final ExchangeStatus NOT_IMPLEMENTED = new ExchangeStatus(501, "Not Implemented");

    /**
     * 502 Bad Gateway
     */
    public static final ExchangeStatus BAD_GATEWAY = new ExchangeStatus(502, "Bad Gateway");

    /**
     * 503 Service Unavailable
     */
    public static final ExchangeStatus SERVICE_UNAVAILABLE = new ExchangeStatus(503, "Service Unavailable");

    /**
     * 504 Gateway Timeout
     */
    public static final ExchangeStatus GATEWAY_TIMEOUT = new ExchangeStatus(504, "Gateway Timeout");

    /**
     * 505 HTTP Version Not Supported
     */
    public static final ExchangeStatus HTTP_VERSION_NOT_SUPPORTED =
            new ExchangeStatus(505, "HTTP Version Not Supported");

    /**
     * 506 Variant Also Negotiates (RFC2295)
     */
    public static final ExchangeStatus VARIANT_ALSO_NEGOTIATES =
            new ExchangeStatus(506, "Variant Also Negotiates");

    /**
     * 507 Insufficient Storage (WebDAV, RFC4918)
     */
    public static final ExchangeStatus INSUFFICIENT_STORAGE = new ExchangeStatus(507, "Insufficient Storage");

    /**
     * 510 Not Extended (RFC2774)
     */
    public static final ExchangeStatus NOT_EXTENDED = new ExchangeStatus(510, "Not Extended");

    /**
     * Returns the {@link ExchangeStatus} represented by the specified code.
     * If the specified code is a standard HTTP status code, a cached instance
     * will be returned.  Otherwise, a new instance will be returned.
     */
    public static ExchangeStatus valueOf(int code) {
        switch (code) {
            case 100:
                return CONTINUE;
            case 101:
                return SWITCHING_PROTOCOLS;
            case 102:
                return PROCESSING;
            case 200:
                return OK;
            case 201:
                return CREATED;
            case 202:
                return ACCEPTED;
            case 203:
                return NON_AUTHORITATIVE_INFORMATION;
            case 204:
                return NO_CONTENT;
            case 205:
                return RESET_CONTENT;
            case 206:
                return PARTIAL_CONTENT;
            case 207:
                return MULTI_STATUS;
            case 300:
                return MULTIPLE_CHOICES;
            case 301:
                return MOVED_PERMANENTLY;
            case 302:
                return FOUND;
            case 303:
                return SEE_OTHER;
            case 304:
                return NOT_MODIFIED;
            case 305:
                return USE_PROXY;
            case 307:
                return TEMPORARY_REDIRECT;
            case 400:
                return BAD_REQUEST;
            case 401:
                return UNAUTHORIZED;
            case 402:
                return PAYMENT_REQUIRED;
            case 403:
                return FORBIDDEN;
            case 404:
                return NOT_FOUND;
            case 405:
                return METHOD_NOT_ALLOWED;
            case 406:
                return NOT_ACCEPTABLE;
            case 407:
                return PROXY_AUTHENTICATION_REQUIRED;
            case 408:
                return REQUEST_TIMEOUT;
            case 409:
                return CONFLICT;
            case 410:
                return GONE;
            case 411:
                return LENGTH_REQUIRED;
            case 412:
                return PRECONDITION_FAILED;
            case 413:
                return REQUEST_ENTITY_TOO_LARGE;
            case 414:
                return REQUEST_URI_TOO_LONG;
            case 415:
                return UNSUPPORTED_MEDIA_TYPE;
            case 416:
                return REQUESTED_RANGE_NOT_SATISFIABLE;
            case 417:
                return EXPECTATION_FAILED;
            case 422:
                return UNPROCESSABLE_ENTITY;
            case 423:
                return LOCKED;
            case 424:
                return FAILED_DEPENDENCY;
            case 425:
                return UNORDERED_COLLECTION;
            case 426:
                return UPGRADE_REQUIRED;
            case 500:
                return INTERNAL_SERVER_ERROR;
            case 501:
                return NOT_IMPLEMENTED;
            case 502:
                return BAD_GATEWAY;
            case 503:
                return SERVICE_UNAVAILABLE;
            case 504:
                return GATEWAY_TIMEOUT;
            case 505:
                return HTTP_VERSION_NOT_SUPPORTED;
            case 506:
                return VARIANT_ALSO_NEGOTIATES;
            case 507:
                return INSUFFICIENT_STORAGE;
            case 510:
                return NOT_EXTENDED;
        }

        final String reasonPhrase;

        if (code < 100) {
            reasonPhrase = "Unknown Status";
        } else if (code < 200) {
            reasonPhrase = "Informational";
        } else if (code < 300) {
            reasonPhrase = "Successful";
        } else if (code < 400) {
            reasonPhrase = "Redirection";
        } else if (code < 500) {
            reasonPhrase = "Client Error";
        } else if (code < 600) {
            reasonPhrase = "Server Error";
        } else {
            reasonPhrase = "Unknown Status";
        }

        return new ExchangeStatus(code, reasonPhrase + " (" + code + ')');
    }

    private final int code;

    private final String reasonPhrase;

    /**
     * Creates a new instance with the specified {@code code} and its
     * {@code reasonPhrase}.
     */
    public ExchangeStatus(int code, String reasonPhrase) {
        if (code < 0) {
            throw new IllegalArgumentException(
                    "code: " + code + " (expected: 0+)");
        }

        if (reasonPhrase == null) {
            throw new NullPointerException("reasonPhrase");
        }

        for (int i = 0; i < reasonPhrase.length(); i++) {
            char c = reasonPhrase.charAt(i);
            // Check prohibited characters.
            switch (c) {
                case '\n':
                case '\r':
                    throw new IllegalArgumentException(
                            "reasonPhrase contains one of the following prohibited characters: " +
                                    "\\r\\n: " + reasonPhrase);
            }
        }

        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Returns the code of this status.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the reason phrase of this status.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public int hashCode() {
        return getCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExchangeStatus)) {
            return false;
        }

        return getCode() == ((ExchangeStatus) o).getCode();
    }

    public int compareTo(ExchangeStatus o) {
        return getCode() - o.getCode();
    }

    public boolean isOk() {
        return this.code == OK.code;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(reasonPhrase.length() + 5);
        buf.append(code);
        buf.append(' ');
        buf.append(reasonPhrase);
        return buf.toString();
    }

    public static ExchangeStatus wrap(int code, String msg) {
        return new ExchangeStatus(code, msg);
    }
}