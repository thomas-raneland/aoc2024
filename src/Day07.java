import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Day07 {
    private static final String INPUT = """
            190: 10 19
            3267: 81 40 27
            83: 17 5
            156: 15 6
            7290: 6 8 6 15
            161011: 16 10 13
            192: 17 8 14
            21037: 9 7 18 13
            292: 11 6 16 20
            """;

    public static void main(String... args) {
        partI();
        partII();
    }

    private static void partI() {
        List<Operator> operators = List.of(Operator.ADD, Operator.MUL);

        long total = parse()
                .filter(eq -> AocUtils.product(operators, eq.operatorsNeeded()).stream().anyMatch(eq::isTrue))
                .mapToLong(Equation::result)
                .sum();

        System.out.println("Part I: " + total);
    }

    private static void partII() {
        List<Operator> operators = List.of(Operator.ADD, Operator.MUL, Operator.CONCAT);

        long total = parse()
                .filter(eq -> AocUtils.product(operators, eq.operatorsNeeded()).stream().anyMatch(eq::isTrue))
                .mapToLong(Equation::result)
                .sum();

        System.out.println("Part II: " + total);
    }

    private static Stream<Equation> parse() {
        return INPUT.lines()
                    .map(line -> {
                        List<String> parts = List.of(line.split(" "));
                        long result = Long.parseLong(parts.getFirst().replace(":", ""));
                        List<Long> factors = parts.stream().skip(1).map(Long::parseLong).toList();
                        return new Equation(result, factors);
                    });
    }

    private static Stream<List<Operator>> combinations(int size, Operator... operators) {
        List<List<Operator>> combinations = new ArrayList<>();
        addCombinations(combinations, size, operators, List.of());
        return combinations.stream();
    }

    private static void addCombinations(List<List<Operator>> combinations, int size, Operator[] ops, List<Operator> prefix) {
        if (prefix.size() == size) {
            combinations.add(prefix);
        } else {
            for (Operator op : ops) {
                addCombinations(combinations, size, ops, Stream.concat(prefix.stream(), Stream.of(op)).toList());
            }
        }
    }

    private record Equation(long result, List<Long> factors) {
        int operatorsNeeded() {
            return factors.size() - 1;
        }

        boolean isTrue(List<Operator> operators) {
            long value = factors.getFirst();

            for (int i = 0; i < operators.size(); i++) {
                value = operators.get(i).apply(value, factors.get(i + 1));
            }

            return value == result;
        }
    }

    private enum Operator {
        ADD, MUL, CONCAT;

        long apply(long a, long b) {
            return switch (this) {
                case ADD -> a + b;
                case MUL -> a * b;
                case CONCAT -> Long.parseLong(String.valueOf(a) + b);
            };
        }
    }
}
