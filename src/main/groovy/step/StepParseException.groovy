package step

/**
 * Thrown when a STEP file cannot be parsed or is structurally invalid.
 */
class StepParseException extends RuntimeException {
    StepParseException(String message) { super(message) }
    StepParseException(String message, Throwable cause) { super(message, cause) }
}
