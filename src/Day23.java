import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Day23 {
    private static final String TEST_INPUT = """
            kh-tc
            qp-kh
            de-cg
            ka-co
            yn-aq
            qp-ub
            cg-tb
            vc-aq
            tb-ka
            wh-tc
            yn-cg
            kh-ub
            ta-co
            de-co
            tc-td
            tb-wq
            wh-td
            ta-ka
            td-qp
            aq-cg
            wq-ub
            ub-vc
            de-ta
            wq-aq
            wq-vc
            wh-yn
            ka-de
            kh-ta
            co-tc
            wh-qp
            tb-vc
            td-yn
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(23);
        String realInput = AocUtils.download(23);

        for (String input : List.of(TEST_INPUT, realInput)) {
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        Map<String, Set<String>> graph = parse(input);
        Set<Set<String>> found = new HashSet<>();

        for (String c1 : graph.keySet()) {
            if (c1.startsWith("t")) {
                for (String c2 : graph.get(c1)) {
                    for (String c3 : graph.get(c1)) {
                        if (graph.get(c2).contains(c3)) {
                            found.add(Set.of(c1, c2, c3));
                        }
                    }
                }
            }
        }

        System.out.println("Part I: " + found.size());
    }

    private static void partII(String input) {
        Set<String> largest = Set.of();
        Set<Set<String>> seen = new HashSet<>();
        Map<String, Set<String>> graph = parse(input);
        graph.forEach((c, links) -> links.add(c));

        for (Set<String> cluster : graph.values()) {
            for (Set<String> subset : subsets(cluster, largest.size() + 1, seen)) {
                if (subset.size() > largest.size() && subset.stream().allMatch(c -> graph.get(c).containsAll(subset))) {
                    largest = subset;
                }
            }
        }

        String res = largest.stream().sorted().collect(Collectors.joining(","));
        System.out.println("Part II: " + res);
    }

    private static Map<String, Set<String>> parse(String input) {
        Map<String, Set<String>> graph = new HashMap<>();

        for (String line : input.lines().toList()) {
            String[] parts = line.split("-");
            graph.computeIfAbsent(parts[0], k -> new HashSet<>()).add(parts[1]);
            graph.computeIfAbsent(parts[1], k -> new HashSet<>()).add(parts[0]);
        }

        return graph;
    }

    private static Set<Set<String>> subsets(Set<String> all, int minSize, Set<Set<String>> seen) {
        if (all.size() < minSize || !seen.add(all)) {
            return Set.of();
        }

        Set<Set<String>> subsets = new HashSet<>();
        subsets.add(all);

        if (all.size() - 1 >= minSize) {
            for (String toRemove : all) {
                Set<String> reduced = all.stream().filter(c -> !c.equals(toRemove)).collect(Collectors.toSet());
                subsets.addAll(subsets(reduced, minSize, seen));
            }
        }

        return subsets;
    }
}
