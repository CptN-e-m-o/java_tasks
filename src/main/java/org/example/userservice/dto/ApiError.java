
package org.example.userservice.dto;

public record ApiError(
        int status,
        String message
) {
}
