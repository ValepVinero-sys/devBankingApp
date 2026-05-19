package com.Bank;

/**
 * The exception that is thrown when attempting to access a resource that does not exist.
 * <p>
 *     Used in the following situations:
 * <ul>
 * <li>Search for an account by a non-existent number</li>
 * <li>Search for a user by a non-existent email</li>
 * <li>Search for a transaction by a non-existent ID</li>
 * <li>Search for any other entity not found in the database</li>
 * </ul>
 * </p>
 * <p><b>HTTP CODE: </b> 404 NOT FOUND</p>
 * <p><b>Example of use: </b></p>
 * <pre>
 * public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
 *     return ResponseEntity.status(HttpStatus.NOT_FOUND)
 *         .body(ErrorResponse.builder()
 *             .status(404)
 *             .error("Not Found")
 *             .message(ex.getMessage())
 *             .build());
 * }}<<pre>
 * @author Valep Vinreo
 * @version 1.0
 * @see InsufficientFundsException
 * @see Global_Exception_Handler
 * @since 04-2026
 */
public class ResourceNotFoundException extends RuntimeException{
    /**
     * The resource type that was not found.
     * <p>
     * Possible values: "Account", "User", "Transaction", "Card"
     * </p>
     */
    private final String resourceType;

    /**
     * Resource identification (number, email, ID).
     */
    private final String resourceIdentifier;

    /**
     * Throws an exception with only a message.
     *
     * @param message - error message (e.g., "Account 1234567890 not found")
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceIdentifier = null;
    }
    /**
     * Throws an exception with the resource type and identifier.
     * <p>
     * Automatically generates the message:
     * {@code "Account 1234567890 not found"}
     * </p>
     *
     * @param resourceType resource type (Account, User, Transaction)
     * @param resourceIdentifier resource identifier
     */
    public ResourceNotFoundException(String resourceType, String resourceIdentifier) {
        super(String.format("%s '%s' not found", resourceType, resourceIdentifier));
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
    }

    /**
     * Throws exemption with type recourse & digit's ID.
     *
     * @param resourceType Resource type
     * @param id digits identification
     */
    //Not implemented yet. Saved for future
    public ResourceNotFoundException(String resourceType, Long id) {
        this(resourceType, String.valueOf(id));
    }
    /**
     * Returns the type of resource that was not found.
     *
     * @return Resource type (Account, User, Transaction) or null
     */
    //Not implemented yet. Saved for future
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Return identification of Resource.
     *
     * @return identification (account number, email, ID) or null
     */
    //Not implemented yet. Saved for future
    public String getResourceIdentifier() {
        return resourceIdentifier;
    }
}
