
package com.coursehub.sorting;

import com.coursehub.model.Registration;

import java.util.ArrayList;
import java.util.List;

public class RegistrationInsertionSort {

    public static List<Registration> sortByRegistrationTime(
            List<Registration> registrations) {

        // Create a copy so the original list is not modified
        List<Registration> sorted = new ArrayList<>(registrations);

        // Start from the second registration
        for (int i = 1; i < sorted.size(); i++) {

            Registration key = sorted.get(i);

            int j = i - 1;

            // Shift registrations with later times to the right
            while (j >= 0 &&
                    sorted.get(j).getRegistrationDateTime()
                            .isAfter(key.getRegistrationDateTime())) {

                sorted.set(j + 1, sorted.get(j));
                j--;
            }

            // Insert the key in its correct position
            sorted.set(j + 1, key);
        }

        return sorted;
    }
}
