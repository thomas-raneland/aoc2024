import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Day05 {
    private static final String INPUT = """
            47|53
            97|13
            97|61
            97|47
            75|29
            61|13
            75|53
            29|13
            97|29
            53|29
            61|53
            97|53
            61|29
            47|13
            75|47
            97|75
            47|61
            75|61
            47|29
            75|13
            53|13
            
            75,47,61,53,29
            97,61,53,29,13
            75,29,13
            75,97,47,61,53
            61,13,29
            97,13,75,29,47""";

    public static void main(String... args) {
        partI();
        partII();
    }

    record Rule(int a, int b) {
        boolean isValid(List<Integer> update) {
            int aIx = update.indexOf(a);
            int bIx = update.indexOf(b);
            return aIx == -1 || bIx == -1 || aIx < bIx;
        }

        public void fix(List<Integer> s) {
            int aIx = s.indexOf(a);
            int bIx = s.indexOf(b);

            if (aIx != -1 && bIx != -1 && aIx > bIx) {
                Collections.swap(s, aIx, bIx);
            }
        }
    }

    private static void partI() {
        List<Rule> rules = parseRules();

        long res = parseUpdates(rules).filter(update -> isValidUpdate(update, rules))
                                      .mapToLong(Day05::middlePage)
                                      .sum();

        System.out.println("Part I: " + res);
    }

    private static void partII() {
        List<Rule> rules = parseRules();

        long res = parseUpdates(rules).filter(update -> !isValidUpdate(update, rules))
                                      .map(update -> sorted(update, rules))
                                      .mapToLong(Day05::middlePage)
                                      .sum();

        System.out.println("Part II: " + res);
    }

    private static List<Rule> parseRules() {
        return INPUT.lines()
                    .takeWhile(line -> !line.isEmpty())
                    .map(line -> line.split("\\|"))
                    .map(array -> new Rule(Integer.parseInt(array[0]), Integer.parseInt(array[1])))
                    .toList();
    }

    private static Stream<List<Integer>> parseUpdates(List<Rule> rules) {
        return INPUT.lines()
                    .skip(rules.size() + 1)
                    .map(line -> line.split(","))
                    .map(array -> Stream.of(array).map(Integer::parseInt).toList());
    }

    private static boolean isValidUpdate(List<Integer> update, List<Rule> rules) {
        return rules.stream().allMatch(rule -> rule.isValid(update));
    }

    private static int middlePage(List<Integer> update) {
        return update.get(update.size() / 2);
    }

    private static List<Integer> sorted(List<Integer> update, List<Rule> rules) {
        update = new ArrayList<>(update);

        while (!isValidUpdate(update, rules)) {
            for (Rule rule : rules) {
                rule.fix(update);
            }
        }

        return update;
    }
}
