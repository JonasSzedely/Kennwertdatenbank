package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculationTest {

    @Test
    void testGetName(){
        String name = "Name";
        String calc = "Value";
        Calculation calculation = new Calculation(name, calc);
        assertEquals(name, calculation.getName());
    }

    @Test
    void testGetCalculation(){
        String name = "Name";
        String calc = "Value";
        Calculation calculation = new Calculation(name, calc);
        assertEquals(calc, calculation.getCalculation());
    }

    @Test
    void testSeparator(){
        Calculation separator = Calculation.SEPARATOR;
        assertEquals("", separator.getCalculation());
        assertEquals("", separator.getName());
    }
}
