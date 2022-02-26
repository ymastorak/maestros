package com.ymastorak.maestros.service;

import com.ymastorak.maestros.api.dtos.response.MaestrosServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExceptionsHandler {

    public ResponseEntity<MaestrosServiceResponse> handleException(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException) {
            return handleValidationException((MethodArgumentNotValidException) ex);
        } else if (ex instanceof MaestrosLogicException) {
            return handleMaestrosLogicException((MaestrosLogicException) ex);
        } else if (ex instanceof DataIntegrityViolationException) {
            return handleConstraintViolationException((DataIntegrityViolationException) ex);
        } else {
            return handleGenericException(ex);
        }
    }

    private ResponseEntity<MaestrosServiceResponse> handleConstraintViolationException(DataIntegrityViolationException ex) {
        List<String> errors = new ArrayList<>();
//        for (ConstraintViolation v : ex.getConstraintViolations()) {
//            errors.add(ex.getMessage());
//        }
        MaestrosServiceResponse response = MaestrosServiceResponse.builder()
                .outcome(MaestrosServiceResponse.ServiceOutcome.FAILED)
                .error("Constraint Violation Error")
                .errorDetails(errors.toString())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<MaestrosServiceResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(fieldName+" "+errorMessage);
        });
        MaestrosServiceResponse response = MaestrosServiceResponse.builder()
                .outcome(MaestrosServiceResponse.ServiceOutcome.FAILED)
                .error("Validation Error")
                .errorDetails(errors.toString())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<MaestrosServiceResponse> handleMaestrosLogicException(MaestrosLogicException ex) {
        MaestrosServiceResponse response = MaestrosServiceResponse.builder()
                .outcome(MaestrosServiceResponse.ServiceOutcome.FAILED)
                .error("Maestros Logic Error")
                .errorDetails(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    private ResponseEntity<MaestrosServiceResponse> handleGenericException(Exception ex) {
        log.error(ex.toString());
        ex.printStackTrace();

        MaestrosServiceResponse response = MaestrosServiceResponse.builder()
                .outcome(MaestrosServiceResponse.ServiceOutcome.FAILED)
                .error("Internal Error")
                .errorDetails(ex.getMessage())
                .build();
        return ResponseEntity.internalServerError().body(response);
    }
}
