package com.medilabosolutions.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class DateFomatValidator implements ConstraintValidator<CustomDateFormat, String> {

    @Override
    public void initialize(CustomDateFormat customDate) {

    }

    @Override
    public boolean isValid(String date, ConstraintValidatorContext context) {

        Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}$");
        boolean validRegex = pattern.matcher(date).matches();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setLenient(false);

        try {
            simpleDateFormat.parse(date);
            return validRegex;
        } catch (ParseException e) {
            return false;
        }
    }

}
