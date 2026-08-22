package com.croi.common.constants;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static final String USER_NOT_FOUND = "User not found";
    public static final String ORGANIZATION_NOT_FOUND = "Organization not found";
    public static final String ORGANIZATION_MEMBER_NOT_FOUND = "Organization member not found";
    public static final String CONVERSATION_NOT_FOUND = "Conversation not found";
    public static final String MESSAGE_NOT_FOUND = "Message not found";
    public static final String DOCUMENT_NOT_FOUND = "Document not found";
    public static final String APPOINTMENT_NOT_FOUND = "Appointment not found";

    public static final String APPOINTMENT_SLOT_UNAVAILABLE = "That time is no longer available";

    public static final String EMAIL_ALREADY_EXISTS = "An account with this email already exists";
    public static final String SLUG_ALREADY_EXISTS = "An organization with this slug already exists";
    public static final String MEMBER_ALREADY_EXISTS = "This user is already a member of the organization";

    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String UNAUTHORIZED = "You are not authorized to perform this action";
    public static final String INVALID_TOKEN = "Invalid or expired token";
}
