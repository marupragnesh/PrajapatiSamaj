package com.matrimonial.repository.specification;

import com.matrimonial.dto.request.DiscoverFilterRequest;
import com.matrimonial.entity.Profile;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * SPECIFICATION: ProfileSpecification
 *
 * Dynamic Specification builder for filtering Profiles on the Discover page.
 * Strictly enforces:
 *   - Profile must be complete (isComplete = true)
 *   - Profile must not belong to the logged-in user (user.id != currentUserId)
 *   - Gender filtering (if preferred gender is specified)
 *   - Optional filters: minAge, maxAge, maritalStatus, diet, minHeight, maxHeight
 *
 * Layer: Repository Specification (DB Query criteria build only)
 */
public class ProfileSpecification {

    public static Specification<Profile> getDiscoverSpecification(
            Long currentUserId,
            Profile.Gender preferredGender,
            DiscoverFilterRequest filter) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory: Only complete profiles
            predicates.add(criteriaBuilder.equal(root.get("isComplete"), true));

            // 2. Mandatory: Exclude logged-in user
            predicates.add(criteriaBuilder.notEqual(root.get("user").get("id"), currentUserId));

            // 3. Gender preference filter (if specified and not ANY)
            if (preferredGender != null) {
                predicates.add(criteriaBuilder.equal(root.get("gender"), preferredGender));
            }

            // 4. Optional filters from DiscoverFilterRequest
            if (filter != null) {
                // Min Age
                if (filter.getMinAge() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("age"), filter.getMinAge()));
                }

                // Max Age
                if (filter.getMaxAge() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("age"), filter.getMaxAge()));
                }

                // Marital Status
                if (filter.getMaritalStatus() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("maritalStatus"), filter.getMaritalStatus()));
                }

                // Diet
                if (filter.getDiet() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("diet"), filter.getDiet()));
                }

                // Surname (trimmed and case-insensitive matching)
                if (hasValue(filter.getSurname())) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.lower(criteriaBuilder.trim(root.get("surname"))),
                            filter.getSurname().trim().toLowerCase()
                    ));
                }

                // Height Range
                if (hasValue(filter.getMinHeight()) || hasValue(filter.getMaxHeight())) {
                    List<String> validHeights = getHeightsInRange(filter.getMinHeight(), filter.getMaxHeight());
                    if (!validHeights.isEmpty()) {
                        predicates.add(root.get("height").in(validHeights));
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasValue(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private static int parseHeightToInches(String heightStr, int defaultInches) {
        if (!hasValue(heightStr)) {
            return defaultInches;
        }
        try {
            String clean = heightStr.replace("\"", "").replace("ft", "'").replace("in", "").trim();

            // Check dot format e.g. "4.8", "5.8", "6.2"
            if (clean.contains(".")) {
                String[] parts = clean.split("\\.");
                if (parts.length >= 2) {
                    int feet = Integer.parseInt(parts[0].trim());
                    int inches = Integer.parseInt(parts[1].trim());
                    return (feet * 12) + inches;
                }
            }

            // Check apostrophe format e.g. "4'8", "5'8"
            if (clean.contains("'")) {
                String[] parts = clean.split("'");
                if (parts.length >= 2) {
                    int feet = Integer.parseInt(parts[0].trim());
                    int inches = !parts[1].trim().isEmpty() ? Integer.parseInt(parts[1].trim()) : 0;
                    return (feet * 12) + inches;
                } else if (parts.length == 1) {
                    int feet = Integer.parseInt(parts[0].trim());
                    return feet * 12;
                }
            }

            // Simple number format
            double val = Double.parseDouble(clean);
            int feet = (int) val;
            int inches = (int) Math.round((val - feet) * 10);
            return (feet * 12) + inches;
        } catch (Exception ignored) {
        }
        return defaultInches;
    }

    private static List<String> getHeightsInRange(String minHeight, String maxHeight) {
        int minInches = parseHeightToInches(minHeight, 48); // default 4'0" (48 inches)
        int maxInches = parseHeightToInches(maxHeight, 84); // default 7'0" (84 inches)

        List<String> validHeights = new ArrayList<>();
        for (int inches = minInches; inches <= maxInches; inches++) {
            int feet = inches / 12;
            int inchRemainder = inches % 12;

            // Generate format variations for DB matching (e.g. 5'8", 5'8, 5.8, 5.8", 5' 8", 5' 8)
            validHeights.add(feet + "'" + inchRemainder + "\"");
            validHeights.add(feet + "'" + inchRemainder);
            validHeights.add(feet + "." + inchRemainder);
            validHeights.add(feet + "." + inchRemainder + "\"");
            validHeights.add(feet + "' " + inchRemainder + "\"");
            validHeights.add(feet + "' " + inchRemainder);
        }
        return validHeights;
    }
}
