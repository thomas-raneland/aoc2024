import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Day01 {
    private static final String INPUT = """
            3   4
            4   3
            2   5
            1   3
            3   9
            3   3""";

    public static void main(String... args) {
        partI();
        partII();
    }

    private static void partI() {
        List<Long> left = new ArrayList<>();
        List<Long> right = new ArrayList<>();

        INPUT.lines().forEach(line -> {
            var split = line.split("\\s+");
            left.add(Long.parseLong(split[0]));
            right.add(Long.parseLong(split[1]));
        });

        left.sort(Comparator.comparing(Long::longValue));
        right.sort(Comparator.comparing(Long::longValue));

        long sum = IntStream.range(0, left.size())
                            .mapToLong(i -> Math.abs(right.get(i) - left.get(i)))
                            .sum();

        System.out.println("Part I: " + sum);
    }

    private static void partII() {
        Map<Long, Long> left = new HashMap<>();
        Map<Long, Long> right = new HashMap<>();

        INPUT.lines().forEach(line -> {
            var split = line.split("\\s+");
            left.merge(Long.parseLong(split[0]), 1L, Long::sum);
            right.merge(Long.parseLong(split[1]), 1L, Long::sum);
        });

        long sum = left.keySet()
                       .stream()
                       .mapToLong(id -> left.get(id) * id * right.getOrDefault(id, 0L))
                       .sum();

        System.out.println("Part II: " + sum);
    }
}
