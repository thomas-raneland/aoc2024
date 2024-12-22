import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day22 {
    private static final String TEST_INPUT1 = """
            1
            10
            100
            2024
            """;

    private static final String TEST_INPUT2 = """
            1
            2
            3
            2024
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(22);
        String realInput = AocUtils.download(22);
        partI(TEST_INPUT1);
        partII(TEST_INPUT2);
        System.out.println();
        partI(realInput);
        partII(realInput);
    }

    private static void partI(String input) {
        long res = 0;

        for (String line : input.lines().toList()) {
            long secret = Long.parseLong(line);

            for (int i = 0; i < 2000; i++) {
                secret = next(secret);
            }

            res += secret;
        }

        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        List<Map<List<Integer>, Integer>> seqToPriceList = new ArrayList<>();
        Set<List<Integer>> allSequences = new HashSet<>();

        for (String line : input.lines().toList()) {
            Map<List<Integer>, Integer> seqToPrice = seqToPrice(line);
            seqToPriceList.add(seqToPrice);
            allSequences.addAll(seqToPrice.keySet());
        }

        long bestSum = 0;

        for (List<Integer> seq : allSequences) {
            long sum = seqToPriceList.stream().mapToLong(seqToPrice -> seqToPrice.getOrDefault(seq, 0)).sum();
            bestSum = Math.max(bestSum, sum);
        }

        System.out.println("Part II: " + bestSum);
    }

    private static long next(long secret) {
        secret = prune(mix(secret * 64, secret));
        secret = prune(mix(secret / 32, secret));
        return prune(mix(secret * 2048, secret));
    }

    private static long prune(long l) {
        return AocUtils.modulo(l, 16777216);
    }

    private static long mix(long l, long m) {
        return l ^ m;
    }

    private static Map<List<Integer>, Integer> seqToPrice(String line) {
        Map<List<Integer>, Integer> seqToPrice = new HashMap<>();
        long secret = Long.parseLong(line);
        List<Integer> seq = new ArrayList<>();

        for (int i = 0; i < 2000; i++) {
            long next = next(secret);
            seq.add((int) (next % 10 - secret % 10));
            secret = next;

            if (seq.size() > 4) {
                seq.removeFirst();
            }

            if (seq.size() == 4) {
                seqToPrice.putIfAbsent(List.copyOf(seq), (int) (secret % 10));
            }
        }

        return seqToPrice;
    }
}
