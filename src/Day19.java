public class Day19 {
    public static void main(String... args) {
        java.util.List<String> lines = AocUtils.download(19).lines().toList();
        java.util.List<String> patterns = lines.subList(2, lines.size());
        java.util.List<String> towels = java.util.List.of(lines.getFirst().split(", "));
        long count = patterns.stream().filter(pattern -> solutions(towels, pattern, 0, new java.util.HashMap<>()) > 0).count();
        long sum = patterns.stream().mapToLong(pattern -> solutions(towels, pattern, 0, new java.util.HashMap<>())).sum();
        System.out.println("Part I: " + count + "\nPart II: " + sum);
    }

    private static long solutions(java.util.List<String> towels, String pattern, int pos, java.util.Map<Integer, Long> cache) {
        if (pos < pattern.length() && !cache.containsKey(pos)) {
            cache.put(pos, towels.stream().filter(towel -> pattern.startsWith(towel, pos))
                                 .mapToLong(towel -> solutions(towels, pattern, pos + towel.length(), cache)).sum());
        }
        return pos == pattern.length() ? 1 : cache.get(pos);
    }
}
