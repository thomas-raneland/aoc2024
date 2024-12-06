import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Day06 {
    private static final String INPUT = """
            ....#.....
            .........#
            ..........
            ..#.......
            .......#..
            ..........
            .#..^.....
            ........#.
            #.........
            ......#...""";

    public static void main(String... args) {
        partI();
        partII();
    }

    private static void partI() {
        Parsed parsed = parse(INPUT);
        //noinspection DataFlowIssue
        int res = walk(parsed.width(), parsed.height(), parsed.pos(), parsed.dir(), parsed.obstacles()).size();
        System.out.println("Part I: " + res);
    }

    private static void partII() {
        Parsed parsed = parse(INPUT);
        int res = 0;

        //noinspection DataFlowIssue
        for (Pos candidate : walk(parsed.width(), parsed.height(), parsed.pos(), parsed.dir(), parsed.obstacles())) {
            if (!parsed.pos().equals(candidate)) {
                Set<Pos> obstacles = new HashSet<>(parsed.obstacles());
                obstacles.add(candidate);

                if (walk(parsed.width(), parsed.height(), parsed.pos(), parsed.dir(), obstacles) == null) {
                    res++;
                }
            }
        }

        System.out.println("Part II: " + res);
    }

    private static Parsed parse(@SuppressWarnings("SameParameterValue") String input) {
        List<String> lines = input.lines().toList();
        Set<Pos> obstacles = new HashSet<>();
        Pos pos = null;

        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);

            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);

                if (c == '#') {
                    obstacles.add(new Pos(col, row));
                } else if (c == '^') {
                    pos = new Pos(col, row);
                }
            }
        }

        return new Parsed(pos, Direction.UP, Set.copyOf(obstacles), lines.getFirst().length(), lines.size());
    }

    private static Set<Pos> walk(int width, int height, Pos pos, Direction dir, Set<Pos> obstacles) {
        record DirPos(Direction dir, Pos pos) {}
        Set<DirPos> travelled = new HashSet<>();

        while (pos.x() >= 0 && pos.x() < width && pos.y() >= 0 && pos.y() < height) {
            if (!travelled.add(new DirPos(dir, pos))) {
                return null;
            }

            while (obstacles.contains(pos.move(dir.dx(), dir.dy()))) {
                dir = dir.turnRight();
            }

            pos = pos.move(dir.dx(), dir.dy());
        }

        return travelled.stream().map(DirPos::pos).collect(Collectors.toSet());
    }

    record Parsed(Pos pos, Direction dir, Set<Pos> obstacles, int width, int height) {}

    record Pos(int x, int y) {
        Pos move(int dx, int dy) {
            return new Pos(x + dx, y + dy);
        }
    }

    record Direction(int dx, int dy) {
        static final Direction UP = new Direction(0, -1);
        static final Direction DOWN = new Direction(0, 1);
        static final Direction LEFT = new Direction(-1, 0);
        static final Direction RIGHT = new Direction(1, 0);

        Direction turnRight() {
            if (this == UP) {
                return RIGHT;
            } else if (this == RIGHT) {
                return DOWN;
            } else if (this == DOWN) {
                return LEFT;
            } else {
                return UP;
            }
        }
    }
}
