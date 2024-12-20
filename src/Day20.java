import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day20 {
    private static final String TEST_INPUT = """
            ###############
            #...#...#.....#
            #.#.#.#.#.###.#
            #S#...#.#.#...#
            #######.#.#.###
            #######.#.#...#
            #######.#.###.#
            ###..E#...#...#
            ###.#######.###
            #...###...#...#
            #.#####.#.###.#
            #.#...#.#.#...#
            #.#.#.#.#.#.###
            #...#...#...###
            ###############
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(20);
        String realInput = AocUtils.download(20);
        partI(TEST_INPUT, 15);
        partII(TEST_INPUT, 50);
        System.out.println();
        partI(realInput, 100);
        partII(realInput, 100);
    }

    private static void partI(String input, int minGain) {
        System.out.println("Part I: " + cheats(input, 2, minGain));
    }

    private static void partII(String input, int minGain) {
        System.out.println("Part II: " + cheats(input, 20, minGain));
    }

    private static long cheats(String input, int maxCheat, int minGain) {
        Maze maze = Maze.parse(input);
        Map<Pos, Integer> distances = distances(maze);
        Pos pos = maze.start();
        long cheats = 0;

        while (!pos.equals(maze.end())) {
            int distance = distances.get(pos);

            for (Pos destination : distances.keySet()) {
                int cheatDistance = pos.manhattanDistance(destination);

                if (cheatDistance <= maxCheat && distance - distances.get(destination) - cheatDistance >= minGain) {
                    cheats++;
                }
            }

            for (Pos n : pos.neighbors()) {
                if (distances.getOrDefault(n, Integer.MAX_VALUE) < distance) {
                    pos = n;
                    break;
                }
            }
        }

        return cheats;
    }

    private static Map<Pos, Integer> distances(Maze maze) {
        Map<Pos, Integer> distances = new HashMap<>();
        Pos pos = maze.end();
        int distance = 0;

        while (!pos.equals(maze.start())) {
            distances.put(pos, distance);

            for (Pos n : pos.neighbors()) {
                if (!distances.containsKey(n) && !maze.walls().contains(n)) {
                    pos = n;
                    distance++;
                    break;
                }
            }
        }

        distances.put(pos, distance);
        return distances;
    }

    private record Pos(int x, int y) {
        List<Pos> neighbors() {
            return List.of(new Pos(x + 1, y), new Pos(x - 1, y), new Pos(x, y + 1), new Pos(x, y - 1));
        }

        int manhattanDistance(Pos that) {
            return Math.abs(x - that.x) + Math.abs(y - that.y);
        }
    }

    private record Maze(Set<Pos> walls, Pos start, Pos end) {
        static Maze parse(String input) {
            Set<Pos> walls = new HashSet<>();
            Pos start = null;
            Pos end = null;

            List<String> lines = input.lines().toList();

            for (int y = 0; y < lines.size(); y++) {
                for (int x = 0; x < lines.get(y).length(); x++) {
                    switch (lines.get(y).charAt(x)) {
                        case 'S' -> start = new Pos(x, y);
                        case 'E' -> end = new Pos(x, y);
                        case '#' -> walls.add(new Pos(x, y));
                    }
                }
            }

            return new Maze(walls, start, end);
        }
    }
}
