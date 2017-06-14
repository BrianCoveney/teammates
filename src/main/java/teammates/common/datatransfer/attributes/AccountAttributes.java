package teammates.common.datatransfer.attributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import teammates.common.util.Assumption;
import teammates.common.util.FieldValidator;
import teammates.common.util.JsonUtils;
import teammates.common.util.SanitizationHelper;
import teammates.common.util.StringHelper;
import teammates.storage.entity.Account;
import teammates.storage.entity.StudentProfile;

/**
 * A data transfer object for Account entities.
 */
public class AccountAttributes extends EntityAttributes {

    //Note: be careful when changing these variables as their names are used in *.json files.

    // required fields
    public String googleId;
    public String name;
    public String email;
    public String institute;

    // optional fields
    public boolean isInstructor;
    public Date createdAt;
    public StudentProfileAttributes studentProfile;

    public AccountAttributes() {
        // attributes to be set after construction
    }

    public AccountAttributes(String googleId, String name, boolean isInstructor,
                             String email, String institute, StudentProfileAttributes studentProfileAttributes) {
        this.googleId = SanitizationHelper.sanitizeGoogleId(googleId);
        this.name = SanitizationHelper.sanitizeName(name);
        this.isInstructor = isInstructor;
        this.email = SanitizationHelper.sanitizeEmail(email);
        this.institute = SanitizationHelper.sanitizeTitle(institute);
        this.studentProfile = studentProfileAttributes;
        this.studentProfile.sanitizeForSaving();
    }

    public AccountAttributes(String googleId, String name, boolean isInstructor,
                             String email, String institute) {
        this.googleId = SanitizationHelper.sanitizeGoogleId(googleId);
        this.name = SanitizationHelper.sanitizeName(name);
        this.isInstructor = isInstructor;
        this.email = SanitizationHelper.sanitizeEmail(email);
        this.institute = SanitizationHelper.sanitizeTitle(institute);
        this.studentProfile = new StudentProfileAttributes();
        this.studentProfile.googleId = this.googleId;
    }

    public static Builder builder(String googleId, String name, String email, String institute) {
        return new Builder(googleId, name, email, institute);
    }

    public static AccountAttributes valueOf(Account account) {
        return builder(
                account.getGoogleId(),
                account.getName(),
                account.getEmail(),
                account.getInstitute())
                .withCreatedAt(account.getCreatedAt())
                .withIsInstructor(account.isInstructor())
                .withStudentProfileAttributes(account.getStudentProfile()
                        == null ? null : new StudentProfileAttributes(account.getStudentProfile()))
                .build();
    }

    @Override
    public Account toEntity() {
        Assumption.assertNotNull(this.studentProfile);
        return new Account(googleId, name, isInstructor, email, institute, (StudentProfile) studentProfile.toEntity());
    }

    /**
     * Gets a deep copy of this object.
     */
    public AccountAttributes getCopy() {
        // toEntity() requires a non-null student profile
        boolean isStudentProfileNull = this.studentProfile == null;
        if (isStudentProfileNull) {
            this.studentProfile = new StudentProfileAttributes();
        }

        AccountAttributes copy = new AccountAttributes.Builder(googleId, name, email, institute)
                .withStudentProfileAttributes(studentProfile)
                .withCreatedAt(createdAt)
                .withIsInstructor(isInstructor)
                .build();

        if (isStudentProfileNull) {
            copy.studentProfile = null;
            this.studentProfile = null;
        }
        return copy;
    }

    public boolean isInstructor() {
        return isInstructor;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getTruncatedGoogleId() {
        return StringHelper.truncateLongId(googleId);
    }

    public String getInstitute() {
        return institute;
    }

    @Override
    public List<String> getInvalidityInfo() {
        FieldValidator validator = new FieldValidator();
        List<String> errors = new ArrayList<String>();

        addNonEmptyError(validator.getInvalidityInfoForPersonName(name), errors);

        addNonEmptyError(validator.getInvalidityInfoForGoogleId(googleId), errors);

        addNonEmptyError(validator.getInvalidityInfoForEmail(email), errors);

        addNonEmptyError(validator.getInvalidityInfoForInstituteName(institute), errors);

        Assumption.assertTrue("Non-null value expected for studentProfile", this.studentProfile != null);
        // only check profile if the account is proper
        if (errors.isEmpty()) {
            errors.addAll(this.studentProfile.getInvalidityInfo());
        }

        //No validation for isInstructor and createdAt fields.
        return errors;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, AccountAttributes.class);
    }

    @Override
    public String getIdentificationString() {
        return this.googleId;
    }

    @Override
    public String getEntityTypeAsString() {
        return "Account";
    }

    @Override
    public String getBackupIdentifier() {
        return "Account";
    }

    @Override
    public String getJsonString() {
        return JsonUtils.toJson(this, AccountAttributes.class);
    }

    @Override
    public void sanitizeForSaving() {
        this.googleId = SanitizationHelper.sanitizeForHtml(googleId);
        this.name = SanitizationHelper.sanitizeForHtml(name);
        this.email = SanitizationHelper.sanitizeForHtml(email);
        this.institute = SanitizationHelper.sanitizeForHtml(institute);
        if (studentProfile == null) {
            return;
        }
        this.studentProfile.sanitizeForSaving();
    }

    public boolean isUserRegistered() {
        return googleId != null && !googleId.isEmpty();
    }

    /**
     * A Builder class for {@link AccountAttributes}.
     */
    public static class Builder {
        private final AccountAttributes accountAttributes;

        public Builder(String googleId, String name, String email, String institute) {
            accountAttributes = new AccountAttributes();

            accountAttributes.googleId = SanitizationHelper.sanitizeGoogleId(googleId);
            accountAttributes.name = SanitizationHelper.sanitizeName(name);
            accountAttributes.email = SanitizationHelper.sanitizeEmail(email);
            accountAttributes.institute = SanitizationHelper.sanitizeForHtml(institute);

            accountAttributes.studentProfile = new StudentProfileAttributes();
            accountAttributes.isInstructor = true;
            accountAttributes.createdAt = new Date();
        }

        public Builder withStudentProfileAttributes(StudentProfileAttributes studentProfile) {
            accountAttributes.studentProfile = studentProfile;
            return this;
        }

        public Builder withIsInstructor(boolean isInstructor) {
            accountAttributes.isInstructor = isInstructor;
            return this;
        }

        public Builder withCreatedAt(Date createdAt) {
            accountAttributes.createdAt = createdAt;
            return this;
        }

        public AccountAttributes build() {
            return accountAttributes;
        }
    }

}
