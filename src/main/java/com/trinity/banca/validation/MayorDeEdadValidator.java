package com.trinity.banca.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class MayorDeEdadValidator implements ConstraintValidator<MayorDeEdad, LocalDate> {
    private int edadMinima;
    
    @Override
    public void initialize(MayorDeEdad anotacion){
        this.edadMinima = anotacion.edadMinima();
    }

    @Override 
    public boolean isValid(LocalDate fechaNacimiento, 
        ConstraintValidatorContext context) {
        if (fechaNacimiento == null){
            return true;
        }
        return Period.between(fechaNacimiento, 
            LocalDate.now()).getYears() >= edadMinima;
    }

}
