import java.util.HashMap;
import java.util.List;

public class Day19 {
    public static void main(String... args) {
        String input = AocUtils.download(19);
        partI(input);
        partII(input);
    }

    private static void partI(String input) {
        Parsed parsed = Parsed.of(input);
        long count = parsed.patterns().stream().filter(p -> solutions(parsed.towels(), p, 0, new HashMap<>()) > 0).count();
        System.out.println("Part I: " + count);
    }

    private static void partII(String input) {
        Parsed parsed = Parsed.of(input);
        long count = parsed.patterns().stream().mapToLong(p -> solutions(parsed.towels(), p, 0, new HashMap<>())).sum();
        System.out.println("Part II: " + count);
    }

    private static long solutions(List<String> towels, String pattern, int pos, HashMap<Integer, Long> cache) {
        if (pos == pattern.length()) {
            return 1;
        } else if (!cache.containsKey(pos)) {
            cache.put(pos, towels.stream()
                                 .filter(t -> pattern.substring(pos).startsWith(t))
                                 .mapToLong(t -> solutions(towels, pattern, pos + t.length(), cache))
                                 .sum());
        }

        return cache.get(pos);
    }

    record Parsed(List<String> towels, List<String> patterns) {
        static Parsed of(String input) {
            List<String> lines = input.lines().toList();
            return new Parsed(List.of(lines.getFirst().split(", ")), lines.subList(2, lines.size()));
        }
    }
}
