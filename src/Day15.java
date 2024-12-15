import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day15 {
    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    private static final String TEST_INPUT = """
            ##########
            #..O..O.O#
            #......O.#
            #.OO..O.O#
            #..O@..O.#
            #O#..O...#
            #O..O..O.#
            #.OO.O.OO#
            #....O...#
            ##########
                        
            <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
            vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
            ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
            <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
            ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
            ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
            >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
            <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
            ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
            v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(15);
        String realInput = AocUtils.download(15);

        for (String input : List.of(TEST_INPUT, realInput)) {
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        Parsed parsed = parse(input);
        Map<Pos, Item> map = parsed.map();
        List<Direction> moves = parsed.moves();
        Pos robot = parsed.robot();

        for (Direction direction : moves) {
            Map<Pos, Item> toMove = new HashMap<>();

            if (canMove(map, robot, direction, toMove)) {
                toMove.forEach(parsed.map::remove);
                toMove.forEach((key, value) -> parsed.map.put(key.neighbor(direction), value));
                robot = robot.neighbor(direction);
            }
        }

        long res = map.keySet().stream().filter(pos -> map.get(pos) == Item.BOX).mapToLong(Pos::gps).sum();
        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        Parsed parsed = parse(input).widen();
        Map<Pos, Item> map = parsed.map();
        List<Direction> moves = parsed.moves();
        Pos robot = parsed.robot();

        for (Direction direction : moves) {
            Map<Pos, Item> toMove = new HashMap<>();

            if (canMove(map, robot, direction, toMove)) {
                toMove.forEach(parsed.map::remove);
                toMove.forEach((key, value) -> parsed.map.put(key.neighbor(direction), value));
                robot = robot.neighbor(direction);
            }
        }

        long res = map.keySet().stream().filter(pos -> map.get(pos) == Item.LARGE_BOX_LEFT).mapToLong(Pos::gps).sum();
        System.out.println("Part II: " + res);
    }

    private static Parsed parse(String input) {
        String[] sections = input.split("\n[ ]*\n");

        Pos robot = null;
        Map<Pos, Item> map = new HashMap<>();
        int y = 0;

        for (String line : sections[0].lines().toList()) {
            for (int x = 0; x < line.length(); x++) {
                Pos pos = new Pos(x, y);

                switch (line.charAt(x)) {
                    case '#' -> map.put(pos, Item.WALL);
                    case 'O' -> map.put(pos, Item.BOX);
                    case '@' -> robot = pos;
                }
            }

            y++;
        }

        List<Direction> moves = new ArrayList<>();

        for (char c : sections[1].toCharArray()) {
            switch (c) {
                case 'v' -> moves.add(Direction.DOWN);
                case '^' -> moves.add(Direction.UP);
                case '<' -> moves.add(Direction.LEFT);
                case '>' -> moves.add(Direction.RIGHT);
            }
        }

        return new Parsed(robot, map, moves);
    }

    private static boolean canMove(Map<Pos, Item> map, Pos pos, Direction direction, Map<Pos, Item> toMove) {
        Pos newPos = pos.neighbor(direction);
        Item atNewPos = map.get(newPos);
        boolean canMove = true;

        if (atNewPos == Item.WALL) {
            canMove = false;
        } else if (atNewPos == Item.LARGE_BOX_LEFT) {
            canMove = canMove(map, newPos, direction, toMove);

            if (direction == Direction.UP || direction == Direction.DOWN) {
                Pos right = newPos.neighbor(Direction.RIGHT);
                canMove &= toMove.containsKey(right) || canMove(map, right, direction, toMove);
            }
        } else if (atNewPos == Item.LARGE_BOX_RIGHT) {
            canMove = canMove(map, newPos, direction, toMove);

            if (direction == Direction.UP || direction == Direction.DOWN) {
                Pos left = newPos.neighbor(Direction.LEFT);
                canMove &= toMove.containsKey(left) || canMove(map, left, direction, toMove);
            }
        } else if (atNewPos == Item.BOX) {
            canMove = canMove(map, newPos, direction, toMove);
        }

        toMove.put(pos, map.get(pos));
        return canMove;
    }

    private record Parsed(Pos robot, Map<Pos, Item> map, List<Direction> moves) {
        public Parsed widen() {
            Map<Pos, Item> wideMap = new HashMap<>();

            for (Pos pos : map.keySet()) {
                Item item = map.get(pos);

                if (item != null) {
                    if (item == Item.BOX) {
                        wideMap.put(pos.widen(), Item.LARGE_BOX_LEFT);
                        wideMap.put(pos.widen().neighbor(Direction.RIGHT), Item.LARGE_BOX_RIGHT);
                    } else if (item == Item.WALL) {
                        wideMap.put(pos.widen(), Item.WALL);
                        wideMap.put(pos.widen().neighbor(Direction.RIGHT), Item.WALL);
                    } else {
                        wideMap.put(pos.widen(), item);
                    }
                }
            }

            return new Parsed(robot.widen(), wideMap, moves);
        }
    }

    private enum Item {
        WALL, BOX, LARGE_BOX_LEFT, LARGE_BOX_RIGHT
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private record Pos(int x, int y) {
        long gps() {
            return 100L * y + x;
        }

        Pos widen() {
            return new Pos(x * 2, y);
        }

        public Pos neighbor(Direction m) {
            return switch (m) {
                case UP -> new Pos(x, y - 1);
                case DOWN -> new Pos(x, y + 1);
                case LEFT -> new Pos(x - 1, y);
                case RIGHT -> new Pos(x + 1, y);
            };
        }
    }
}
