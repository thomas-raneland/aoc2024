import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Day16 {
    private static final String TEST_INPUT = """
            ###############
            #.......#....E#
            #.#.###.#.###.#
            #.....#.#...#.#
            #.###.#####.#.#
            #.#.#.......#.#
            #.#.#####.###.#
            #...........#.#
            ###.#.#####.#.#
            #...#.....#.#.#
            #.#.#.###.#.#.#
            #.....#...#.#.#
            #.###.#.#.#.#.#
            #S..#.....#...#
            ###############""";

    public static void main(String... args) {
        AocUtils.waitForStartTime(16);
        String realInput = AocUtils.download(16);

        for (String input : List.of(TEST_INPUT, realInput)) {
            partIAndII(input);
            System.out.println();
        }
    }

    private static void partIAndII(String input) {
        Maze maze = parse(input);
        Set<State> seen = new HashSet<>();
        PriorityQueue<StateCost> queue = new PriorityQueue<>(Comparator.comparingLong(StateCost::cost));
        Map<StateCost, Set<StateCost>> previous = new HashMap<>();
        queue.add(new StateCost(new State(maze.start(), Direction.EAST), 0));

        while (!queue.isEmpty()) {
            StateCost current = queue.poll();
            State state = current.state();

            if (!seen.add(state)) {
                continue;
            }

            if (state.pos().equals(maze.end())) {
                System.out.println("Part I: " + current.cost());
                Set<Pos> onBestPath = new HashSet<>();
                addToBestPath(onBestPath, current, previous);
                System.out.println("Part II: " + onBestPath.size());
                return;
            }

            maze.directions(state.pos(), state.direction()).forEach((direction, turnCost) -> {
                StateCost next = new StateCost(new State(state.pos().walk(direction), direction), current.cost() + turnCost + 1);
                previous.computeIfAbsent(next, k -> new HashSet<>()).add(current);
                queue.add(next);
            });
        }
    }

    private static Maze parse(String input) {
        Map<Pos, Character> tiles = new HashMap<>();
        List<String> lines = input.lines().toList();

        for (int y = 0; y < lines.size(); y++) {
            for (int x = 0; x < lines.get(y).length(); x++) {
                tiles.put(new Pos(x, y), lines.get(y).charAt(x));
            }
        }

        Pos start = tiles.entrySet().stream().filter(e -> e.getValue() == 'S').findFirst().orElseThrow().getKey();
        Pos end = tiles.entrySet().stream().filter(e -> e.getValue() == 'E').findFirst().orElseThrow().getKey();
        return new Maze(tiles, start, end);
    }

    private static void addToBestPath(Set<Pos> onPath, StateCost current, Map<StateCost, Set<StateCost>> paths) {
        onPath.add(current.state().pos());

        for (var prev : paths.getOrDefault(current, Set.of())) {
            addToBestPath(onPath, prev, paths);
        }
    }

    private record Maze(Map<Pos, Character> map, Pos start, Pos end) {
        Map<Direction, Integer> directions(Pos pos, Direction direction) {
            Map<Direction, Integer> dirs = new HashMap<>();

            if (map.get(pos.walk(direction)) != '#') {
                dirs.put(direction, 0);
            }

            if (map.get(pos.walk(direction.left())) != '#') {
                dirs.put(direction.left(), 1_000);
            }

            if (map.get(pos.walk(direction.right())) != '#') {
                dirs.put(direction.right(), 1_000);
            }

            return dirs;
        }
    }

    private record Pos(int x, int y) {
        Pos walk(Direction direction) {
            return switch (direction) {
                case NORTH -> new Pos(x, y - 1);
                case EAST -> new Pos(x + 1, y);
                case SOUTH -> new Pos(x, y + 1);
                case WEST -> new Pos(x - 1, y);
            };
        }
    }

    private enum Direction {
        NORTH, EAST, SOUTH, WEST;

        Direction left() {
            return values()[(ordinal() + 3) % 4];
        }

        Direction right() {
            return values()[(ordinal() + 1) % 4];
        }
    }

    private record State(Pos pos, Direction direction) {}

    private record StateCost(State state, int cost) {}
}
