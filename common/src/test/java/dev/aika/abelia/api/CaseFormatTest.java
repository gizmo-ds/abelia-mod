package dev.aika.abelia.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CaseFormatTest {
    @Test
    public void testCaseFormat() {
        assertEquals("helloWorld", CaseFormat.AS_IS.convert("helloWorld"));
        assertEquals("hello_world", CaseFormat.SNAKE_CASE.convert("helloWorld"));
    }
}
