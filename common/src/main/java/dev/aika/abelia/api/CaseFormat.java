package dev.aika.abelia.api;

public enum CaseFormat {
    AS_IS,
    SNAKE_CASE; // e.g., "hello_world"

    public String convert(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return switch (this) {
            case AS_IS -> input;
            case SNAKE_CASE -> {
                StringBuilder sb = new StringBuilder();
                for (final char c : input.toCharArray()) {
                    if (Character.isUpperCase(c)) sb.append('_').append(Character.toLowerCase(c));
                    else sb.append(c);
                }
                yield sb.toString();
            }
        };
    }
}
