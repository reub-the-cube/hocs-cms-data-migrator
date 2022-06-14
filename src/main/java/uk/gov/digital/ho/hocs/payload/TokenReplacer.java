package uk.gov.digital.ho.hocs.payload;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class TokenReplacer {
    private static final FakeValuesService fakeValuesService = new FakeValuesService(
            new Locale("en-GB"), new RandomService());
    private static final Faker faker = new Faker();

    public static String replaceToken(String token) {
        switch (token) {
            case "@@TODAY@@":
                return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
            case "@@COMPLAINT_TEXT@@":
                return faker.lorem().paragraph();
            case "@@APPLICANT_NAME@@":
            case "@@AGENT_NAME@@":
                return faker.name().fullName();
            case "@@NATIONALITY@@":
            case "@@COUNTRY@@":
                return faker.country().name();
            case "@@CITY@@":
                return faker.address().city();
            case "@@DOB@@":
                return convertToLocalDate(faker.date().birthday()).format(DateTimeFormatter.ISO_DATE);
            case "@@APPLICANT_EMAIL@@":
            case "@@AGENT_EMAIL@@":
                return faker.internet().safeEmailAddress();
            case "@@PHONE@@":
                return "0114 4960999";
            case "@@REFERENCE@@":
                return fakeValuesService.regexify("[a-z1-9]{10}");
            default:
                return "Unknown Token";
        }
    }


    public static LocalDate convertToLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
