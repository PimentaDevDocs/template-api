package com.pimentadesenvolvimento.conroledebolsaoback.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator for Pageable objects to prevent invalid sort field requests.
 * This prevents N+1 query issues and 500 errors from invalid sort properties.
 */
@Component
@Slf4j
public class PageableValidator {

    private static final Set<String> VALID_USER_SORT_FIELDS = new HashSet<>(Arrays.asList(
            "id", "username", "email", "name", "createdAt", "updatedAt"
    ));

    private static final Set<String> VALID_PERSON_SORT_FIELDS = new HashSet<>(Arrays.asList(
            "id", "name", "birthDate", "createdAt", "updatedAt"
    ));

    /**
     * Validates User entity Pageable, ensuring sort fields are valid
     *
     * @param pageable the Pageable to validate
     * @return validated Pageable with safe sort fields
     * @throws IllegalArgumentException if invalid sort fields are detected
     */
    public Pageable validateUserPageable(Pageable pageable) {
        return validatePageable(pageable, VALID_USER_SORT_FIELDS, "User");
    }

    /**
     * Validates Person entity Pageable, ensuring sort fields are valid
     *
     * @param pageable the Pageable to validate
     * @return validated Pageable with safe sort fields
     * @throws IllegalArgumentException if invalid sort fields are detected
     */
    public Pageable validatePersonPageable(Pageable pageable) {
        return validatePageable(pageable, VALID_PERSON_SORT_FIELDS, "Person");
    }

    /**
     * Generic Pageable validation
     *
     * @param pageable    the Pageable to validate
     * @param validFields set of valid field names
     * @param entityName  name of the entity being sorted
     * @return validated Pageable
     * @throws IllegalArgumentException if invalid sort fields are detected
     */
    private Pageable validatePageable(Pageable pageable, Set<String> validFields, String entityName) {
        if (pageable == null || !pageable.getSort().isSorted()) {
            return pageable;
        }

        StringBuilder invalidFields = new StringBuilder();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if (!validFields.contains(property)) {
                if (invalidFields.length() > 0) {
                    invalidFields.append(", ");
                }
                invalidFields.append(property);
            }
        }

        if (invalidFields.length() > 0) {
            String errorMessage = String.format(
                    "Invalid sort field(s) for %s entity: %s. Valid fields are: %s",
                    entityName, invalidFields, validFields
            );
            log.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        return pageable;
    }
}
