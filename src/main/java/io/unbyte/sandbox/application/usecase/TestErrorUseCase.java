package io.unbyte.sandbox.application.usecase;

import io.unbyte.sandbox.application.command.TestErrorCommand;
import io.unbyte.sandbox.application.exception.ApplicationServiceException;
import io.unbyte.sandbox.domain.exception.DomainException;
import io.unbyte.sandbox.shared.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Use case for testing error scenarios
 * Contains the business logic for error testing
 * 
 * This class is framework-agnostic and should not have Spring annotations.
 * Dependency injection is handled by the infrastructure layer.
 */
public class TestErrorUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(TestErrorUseCase.class);
    
    /**
     * Test a specific error type
     * @param command the command containing the error type to test
     * @return the result message (this will throw an exception)
     */
    public String testError(TestErrorCommand command) {
        logger.info("Testing error type: {}", command.errorType());
        
        try {
            // Business logic: Determine which error to throw based on type
            return simulateError(command);
            
        } catch (Exception e) {
            logger.error("Error testing failed for type: {}", command.errorType(), e);
            throw e; // Re-throw the exception as this is expected behavior
        }
    }
    
    /**
     * Simulate the error based on the command
     * @param command the test error command
     * @return never returns (always throws an exception)
     */
    private String simulateError(TestErrorCommand command) {
        String errorType = command.getErrorTypeLowercase();
        
        switch (errorType) {
            case "domain_validation":
                throw DomainException.validationFailed("testField", "Test validation error");
                
            case "domain_entity_not_found":
                throw DomainException.entityNotFound("TestEntity", "test-id-123");
                
            case "domain_business_rule":
                throw DomainException.businessRuleViolation("TestRule", "Business rule violation");
                
            case "application_use_case":
                throw new ApplicationServiceException(
                    ErrorCode.APPLICATION_USE_CASE_FAILED,
                    "Test use case failed"
                );
                
            case "infrastructure_database":
                throw new ApplicationServiceException(
                    ErrorCode.INFRASTRUCTURE_DATABASE_ERROR,
                    "Test database error"
                );
                
            case "system_internal":
                throw new RuntimeException("Test system error");
                
            default:
                throw new IllegalArgumentException("Unknown error type: " + command.errorType());
        }
    }
}
