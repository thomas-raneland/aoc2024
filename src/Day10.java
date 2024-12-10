import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day10 {
    private static final String TEST_INPUT = """
            89010123
            78121874
            87430965
            96549874
            45678903
            32019012
            01329801
            10456732
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(10);
        String realInput = AocUtils.download(10);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    record Cell(int x, int y, int height) {
        boolean canMoveTo(Cell other) {
            return height + 1 == other.height &&
                   (x == other.x && Math.abs(y - other.y) == 1 ||
                    y == other.y && Math.abs(x - other.x) == 1);
        }
    }

    private static void partI(String input) {
        Set<Cell> cells = parse(input);
        AocUtils.Graph<Cell> graph = graph(cells);
        long score = 0;

        for (Cell start : cells) {
            if (start.height() == 0) {
                for (Cell end : cells) {
                    if (end.height() == 9 && graph.dijkstra(start, end) != Long.MAX_VALUE) {
                        score++;
                    }
                }
            }
        }

        System.out.println("Part I: " + score);
    }

    private static void partII(String input) {
        Set<Cell> cells = parse(input);
        Map<Cell, Set<Cell>> neighbors = neighbors(cells);
        long sum = 0;

        for (Cell start : cells) {
            if (start.height() == 0) {
                sum += countPathsToTop(start, neighbors);
            }
        }

        System.out.println("Part II: " + sum);
    }

    private static Set<Cell> parse(String input) {
        Set<Cell> cells = new HashSet<>();
        int y = 0;

        for (String line : input.lines().toList()) {
            for (int x = 0; x < line.length(); x++) {
                cells.add(new Cell(x, y, line.charAt(x) - '0'));
            }

            y++;
        }

        return cells;
    }

    private static AocUtils.Graph<Cell> graph(Set<Cell> cells) {
        AocUtils.Graph<Cell> graph = new AocUtils.Graph<>();

        for (Cell source : cells) {
            for (Cell dest : cells) {
                if (source.canMoveTo(dest)) {
                    graph.addEdge(source, dest, 1);
                }
            }
        }

        return graph;
    }

    private static Map<Cell, Set<Cell>> neighbors(Set<Cell> cells) {
        Map<Cell, Set<Cell>> neighbors = new HashMap<>();

        for (Cell source : cells) {
            for (Cell dest : cells) {
                if (source.canMoveTo(dest)) {
                    neighbors.computeIfAbsent(source, k -> new HashSet<>()).add(dest);
                }
            }
        }

        return neighbors;
    }

    private static long countPathsToTop(Cell current, Map<Cell, Set<Cell>> neighbors) {
        if (current.height() == 9) {
            return 1;
        } else {
            long sum = 0;

            for (Cell next : neighbors.getOrDefault(current, Set.of())) {
                sum += countPathsToTop(next, neighbors);
            }

            return sum;
        }
    }
}
