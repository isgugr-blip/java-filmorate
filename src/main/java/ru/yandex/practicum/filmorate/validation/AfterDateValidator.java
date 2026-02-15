package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AfterDateValidator implements ConstraintValidator<AfterDate, Date> {
    private Date minDate;

    @Override
    public void initialize(AfterDate annotation) {
        try {
            minDate = new SimpleDateFormat("yyyy-MM-dd").parse(annotation.value());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + annotation.value(), e);
        }
    }

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.after(minDate);
    }
}