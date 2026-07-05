package com.matrimonial.dto.request;

import com.matrimonial.entity.enums.Diet;
import com.matrimonial.entity.enums.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: DiscoverFilterRequest
 *
 * Encapsulates optional search filters for the Discover page.
 * All fields are optional/nullable.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscoverFilterRequest {

    private Integer minAge;
    private Integer maxAge;
    private MaritalStatus maritalStatus;
    private String minHeight;
    private String maxHeight;
    private Diet diet;
}
