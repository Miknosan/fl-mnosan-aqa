package io.testautomation.demoqa.registration.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.FilePayload;
import io.testautomation.demoqa.platform.page.BasePage;
import io.testautomation.demoqa.registration.component.DatePickerComponent;
import io.testautomation.demoqa.registration.component.ReactSelectComponent;
import io.testautomation.demoqa.registration.component.RegistrationResultModal;
import io.testautomation.demoqa.registration.model.Gender;
import io.testautomation.demoqa.registration.model.Hobby;
import io.testautomation.demoqa.registration.model.RegistrationFormState;
import io.testautomation.demoqa.registration.model.RequiredRegistrationField;
import io.testautomation.demoqa.registration.model.StudentRegistrationData;

import java.net.URI;

public final class StudentRegistrationPage extends BasePage {
    private static final String PATH = "/automation-practice-form";
    private final Locator firstName;
    private final Locator lastName;
    private final Locator email;
    private final Locator mobile;
    private final Locator address;
    private final Locator submit;
    private final DatePickerComponent datePicker;
    private final ReactSelectComponent subjects;
    private final ReactSelectComponent state;
    private final ReactSelectComponent city;
    private final RegistrationResultModal resultModal;

    public StudentRegistrationPage(Page page, URI baseUrl) {
        super(page, baseUrl);
        firstName = page.locator("#firstName");
        lastName = page.locator("#lastName");
        email = page.locator("#userEmail");
        mobile = page.locator("#userNumber");
        address = page.locator("#currentAddress");
        submit = page.locator("#submit");
        datePicker = new DatePickerComponent(page);
        subjects = new ReactSelectComponent(page, "#subjectsInput");
        state = new ReactSelectComponent(page, "#react-select-3-input");
        city = new ReactSelectComponent(page, "#react-select-4-input");
        resultModal = new RegistrationResultModal(page);
    }

    public StudentRegistrationPage open() {
        navigate(PATH);
        waitUntilVisible("#userForm");
        return this;
    }

    public void complete(StudentRegistrationData data) {
        fillWhenPresent(firstName, data.firstName());
        fillWhenPresent(lastName, data.lastName());
        fillWhenPresent(email, data.email());
        if (data.gender() != null) {
            genderLabel(data.gender()).click();
        }
        fillWhenPresent(mobile, data.mobile());
        if (data.dateOfBirth() != null) {
            datePicker.select(data.dateOfBirth());
        }
        data.subjects().forEach(subjects::select);
        data.hobbies().forEach(hobby -> hobbyLabel(hobby).click());
        if (data.picture() != null) {
            page.locator("#uploadPicture").setInputFiles(new FilePayload(
                    data.picture().name(),
                    data.picture().mimeType(),
                    data.picture().content()));
        }
        fillWhenPresent(address, data.currentAddress());
        if (!data.state().isBlank()) {
            state.select(data.state());
        }
        if (!data.city().isBlank()) {
            city.select(data.city());
        }
    }

    public void submit() {
        submit.click();
    }

    public RegistrationResultModal resultModal() {
        return resultModal;
    }

    public boolean isInvalid(RequiredRegistrationField field) {
        Locator control = switch (field) {
            case FIRST_NAME -> firstName;
            case LAST_NAME -> lastName;
            case GENDER -> page.locator("#gender-radio-1");
            case MOBILE -> mobile;
        };
        return Boolean.TRUE.equals(control.evaluate("element => !element.checkValidity()"));
    }

    public boolean isEmailInvalid() {
        return Boolean.TRUE.equals(email.evaluate("element => !element.checkValidity()"));
    }

    public boolean isMobileInvalid() {
        return Boolean.TRUE.equals(mobile.evaluate("element => !element.checkValidity()"));
    }

    public RegistrationFormState requiredState() {
        return new RegistrationFormState(
                firstName.inputValue(),
                lastName.inputValue(),
                selectedGender(),
                mobile.inputValue());
    }

    private Gender selectedGender() {
        for (Gender gender : Gender.values()) {
            if (genderInput(gender).isChecked()) {
                return gender;
            }
        }
        return null;
    }

    private Locator genderInput(Gender gender) {
        return page.locator(switch (gender) {
            case MALE -> "#gender-radio-1";
            case FEMALE -> "#gender-radio-2";
            case OTHER -> "#gender-radio-3";
        });
    }

    private Locator genderLabel(Gender gender) {
        return page.locator(switch (gender) {
            case MALE -> "label[for='gender-radio-1']";
            case FEMALE -> "label[for='gender-radio-2']";
            case OTHER -> "label[for='gender-radio-3']";
        });
    }

    private Locator hobbyLabel(Hobby hobby) {
        return page.locator(switch (hobby) {
            case SPORTS -> "label[for='hobbies-checkbox-1']";
            case READING -> "label[for='hobbies-checkbox-2']";
            case MUSIC -> "label[for='hobbies-checkbox-3']";
        });
    }

    private static void fillWhenPresent(Locator locator, String value) {
        if (value != null && !value.isBlank()) {
            locator.fill(value);
        }
    }
}
