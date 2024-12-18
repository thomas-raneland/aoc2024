import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Day18 {
    private static final String TEST_INPUT = """
            5,4
            4,2
            4,5
            3,0
            2,1
            6,3
            2,4
            1,5
            0,6
            3,3
            2,6
            5,1
            1,2
            5,5
            2,5
            6,5
            1,4
            0,4
            6,4
            1,1
            6,1
            1,0
            0,5
            1,6
            2,0
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(18);
        String realInput = AocUtils.download(18);

        partI(TEST_INPUT, 7, 12);
        partII(TEST_INPUT, 7);
        System.out.println();

        partI(realInput, 71, 1024);
        partII(realInput, 71);
    }

    private static void partI(String input, int size, int nbrOfBytes) {
        List<Pos> corruptedBytes = parse(input).limit(nbrOfBytes).toList();
        System.out.println("Part I: " + shortestPath(size, corruptedBytes));
    }

    private static void partII(String input, int size) {
        List<Pos> corruptedBytes = parse(input).toList();
        int start = 0;
        int end = corruptedBytes.size();

        while (start < end) {
            int mid = (start + end) / 2;

            if (shortestPath(size, corruptedBytes.subList(0, mid)) == Long.MAX_VALUE) {
                end = mid;
            } else {
                start = mid + 1;
            }
        }

        System.out.println("Part II: " + corruptedBytes.get(start - 1));
    }

    private static Stream<Pos> parse(String input) {
        return input.lines().map(l -> new Pos(Integer.parseInt(l.split(",")[0]), Integer.parseInt(l.split(",")[1])));
    }

    private static long shortestPath(int size, List<Pos> corruptedBytes) {
        Set<Pos> corrupted = new HashSet<>(corruptedBytes);
        AocUtils.Graph<Pos> graph = new AocUtils.Graph<>();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Pos from = new Pos(x, y);

                from.neighbors()
                    .filter(to -> to.x() >= 0 && to.x() < size && to.y() >= 0 && to.y() < size)
                    .filter(to -> !corrupted.contains(from) && !corrupted.contains(to))
                    .forEach(to -> graph.addEdge(from, to, 1));
            }
        }

        return graph.dijkstra(new Pos(0, 0), new Pos(size - 1, size - 1));
    }

    private record Pos(int x, int y) {
        Stream<Pos> neighbors() {
            return Stream.of(new Pos(x - 1, y), new Pos(x, y - 1), new Pos(x + 1, y), new Pos(x, y + 1));
        }
    }
}
