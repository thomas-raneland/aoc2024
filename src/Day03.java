import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class Day03 {
    private static final String INPUT1 = """
            xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))""";

    private static final String INPUT2 = """
            xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))
            """;

    public static void main(String... args) {
        partI();
        partII();
    }

    private static void partI() {
        long res = Pattern.compile("mul\\((\\d+),(\\d+)\\)")
                          .matcher(INPUT1)
                          .results()
                          .mapToLong((r -> Long.parseLong(r.group(1)) * Long.parseLong(r.group(2))))
                          .sum();

        System.out.println("Part I: " + res);
    }

    private static void partII() {
        AtomicBoolean on = new AtomicBoolean(true);

        long res = Pattern.compile("do\\(\\)|don't\\(\\)|mul\\((\\d+),(\\d+)\\)")
                          .matcher(INPUT2)
                          .results()
                          .peek(r -> {
                              if (r.group().equals("do()")) {
                                  on.set(true);
                              } else if (r.group().equals("don't()")) {
                                  on.set(false);
                              }
                          })
                          .filter(r -> on.get())
                          .filter(r -> r.group().startsWith("mul"))
                          .mapToLong((r -> Long.parseLong(r.group(1)) * Long.parseLong(r.group(2))))
                          .sum();

        System.out.println("Part II: " + res);
    }
}
