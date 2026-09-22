package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.application.exception.JobApplicationNotFoundException;
import com.josyantl.joblens.job.application.exception.InterviewNotFoundException;
import com.josyantl.joblens.job.application.exception.StaleInterviewVersionException;
import com.josyantl.joblens.job.domain.exception.InterviewStateException;
import com.josyantl.joblens.job.application.exception.FollowUpTaskNotFoundException;
import com.josyantl.joblens.job.application.exception.FollowUpTaskConflictException;
import com.josyantl.joblens.job.application.exception.StaleJobApplicationVersionException;
import com.josyantl.joblens.job.domain.exception.InvalidApplicationStatusTransitionException;
import com.josyantl.joblens.identity.application.exception.EmailAlreadyRegisteredException;
import com.josyantl.joblens.notification.application.exception.NotificationNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ProblemDetail handleNotificationNotFound(NotificationNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Notification not found");
        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Invalid email or password");
        problem.setTitle("Authentication failed");
        return problem;
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ProblemDetail handleDuplicateEmail(EmailAlreadyRegisteredException exception) {
        return conflictProblem("Email already registered", exception.getMessage());
    }

    @ExceptionHandler(InterviewNotFoundException.class)
    public ProblemDetail handleInterviewNotFound(InterviewNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Interview not found");
        return problem;
    }

    @ExceptionHandler(StaleInterviewVersionException.class)
    public ProblemDetail handleInterviewVersion(StaleInterviewVersionException exception) {
        return conflictProblem("Stale interview version", exception.getMessage());
    }

    @ExceptionHandler(InterviewStateException.class)
    public ProblemDetail handleInterviewState(InterviewStateException exception) {
        return conflictProblem("Invalid interview operation", exception.getMessage());
    }

    @ExceptionHandler(FollowUpTaskNotFoundException.class)
    public ProblemDetail handleTaskNotFound(FollowUpTaskNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Follow-up task not found");
        return problem;
    }

    @ExceptionHandler(FollowUpTaskConflictException.class)
    public ProblemDetail handleTaskConflict(FollowUpTaskConflictException exception) {
        return conflictProblem("Stale follow-up task version", exception.getMessage());
    }

    @ExceptionHandler(JobApplicationNotFoundException.class)
    public ProblemDetail handleNotFound(JobApplicationNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
        problem.setTitle("Job application not found");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidArgument(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
        problem.setTitle("Invalid request");
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
        problem.setTitle("Invalid request");
        return problem;
    }

    @ExceptionHandler(InvalidApplicationStatusTransitionException.class)
    public ProblemDetail handleStatusTransitionConflict(
            InvalidApplicationStatusTransitionException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
        problem.setTitle("Invalid application status transition");
        return problem;
    }

    @ExceptionHandler(StaleJobApplicationVersionException.class)
    public ProblemDetail handleStaleVersion(StaleJobApplicationVersionException exception) {
        return conflictProblem("Stale job application version", exception.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingFailure(
            OptimisticLockingFailureException exception
    ) {
        return conflictProblem(
                "Concurrent update",
                "The record was modified by another request; reload it and try again"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleRequestValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        ProblemDetail problem = invalidRequestProblem("Request validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return invalidRequestProblem("Request body is malformed or contains an unsupported value");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return invalidRequestProblem("Invalid value for parameter: " + exception.getName());
    }

    private ProblemDetail invalidRequestProblem(String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Invalid request");
        return problem;
    }

    private ProblemDetail conflictProblem(String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        problem.setTitle(title);
        return problem;
    }
}
