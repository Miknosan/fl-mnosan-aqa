package io.testautomation.demoqa.registration.data;

import io.testautomation.demoqa.registration.model.Gender;
import io.testautomation.demoqa.registration.model.Hobby;
import io.testautomation.demoqa.registration.model.StudentRegistrationData;
import io.testautomation.demoqa.registration.model.UploadedFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class StudentRegistrationDataFactory {
    public StudentRegistrationData completeRegistration() {
        return new StudentRegistrationData(
                "Olivia",
                "Bennett",
                "olivia.bennett." + uniqueToken() + "@example.com",
                Gender.FEMALE,
                "9876543210",
                LocalDate.of(1995, 2, 15),
                List.of("Maths", "English"),
                List.of(Hobby.READING, Hobby.MUSIC),
                studentProfilePicture(),
                "42 Quality Avenue, Kyiv",
                "NCR",
                "Delhi");
    }

    public StudentRegistrationData requiredRegistration() {
        StudentRegistrationData complete = completeRegistration();
        return new StudentRegistrationData(
                complete.firstName(),
                complete.lastName(),
                "",
                complete.gender(),
                complete.mobile(),
                null,
                List.of(),
                List.of(),
                null,
                "",
                "",
                "");
    }

    private static UploadedFile studentProfilePicture() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.MAGENTA.getRGB());
        image.setRGB(1, 0, Color.WHITE.getRGB());
        image.setRGB(0, 1, Color.WHITE.getRGB());
        image.setRGB(1, 1, Color.MAGENTA.getRGB());
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return new UploadedFile("student-profile.png", "image/png", output.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot create student profile image", exception);
        }
    }

    private static String uniqueToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
