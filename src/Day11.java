import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Day11 {
    private static final String TEST_INPUT = """
            125 17
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(11);
        String realInput = AocUtils.download(11);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        List<Long> stones = parse(input);
        long total = stones.stream().mapToLong(stone -> count(stone, 25, new HashMap<>())).sum();
        System.out.println("Part I: " + total);
    }

    private static void partII(String input) {
        List<Long> stones = parse(input);
        long total = stones.stream().mapToLong(stone -> count(stone, 75, new HashMap<>())).sum();
        System.out.println("Part II: " + total);
    }

    private static List<Long> parse(String input) {
        return Stream.of(input.replace("\r", "").replace("\n", "").split(" ")).map(Long::parseLong).toList();
    }

    private static long count(Long stone, int blinksLeft, Map<CacheKey, Long> cache) {
        if (blinksLeft == 0) {
            return 1;
        }

        CacheKey key = new CacheKey(stone, blinksLeft);
        Long count = cache.get(key);

        if (count == null) {
            if (stone == 0) {
                count = count(1L, blinksLeft - 1, cache);
            } else if (stone.toString().length() % 2 == 0) {
                String left = stone.toString().substring(0, stone.toString().length() / 2);
                String right = stone.toString().substring(stone.toString().length() / 2);
                count = count(Long.parseLong(left), blinksLeft - 1, cache) +
                        count(Long.parseLong(right), blinksLeft - 1, cache);
            } else {
                count = count(2024L * stone, blinksLeft - 1, cache);
            }
        }

        cache.put(key, count);
        return count;
    }

    private record CacheKey(long stone, long blinksLeft) {}
}
