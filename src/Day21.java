import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day21 {
    private static final String TEST_INPUT = """
            029A
            980A
            179A
            456A
            379A
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(21);
        String realInput = AocUtils.download(21);

        for (String input : List.of(TEST_INPUT, realInput)) {
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        long sum = 0;

        for (String code : input.lines().toList()) {
            int numericPartOfCode = Integer.parseInt(code.substring(0, 3));
            List<String> directions = directionCodes(code, NUM_KEYPAD);
            long minCodeLength = directions.stream().mapToLong(d -> minCodeLength(d, 2)).min().orElseThrow();
            sum += minCodeLength * numericPartOfCode;
        }

        System.out.println("Part I: " + sum);
    }

    private static void partII(String input) {
        long sum = 0;

        for (String code : input.lines().toList()) {
            int numericPartOfCode = Integer.parseInt(code.substring(0, 3));
            List<String> directions = directionCodes(code, NUM_KEYPAD);
            long minCodeLength = directions.stream().mapToLong(d -> minCodeLength(d, 25)).min().orElseThrow();
            sum += minCodeLength * numericPartOfCode;
        }

        System.out.println("Part II: " + sum);
    }

    private static final Map<CacheKey, Long> minCodeLengthCache = new HashMap<>();

    private static long minCodeLength(String directions, int dirKeypads) {
        if (dirKeypads == 0) {
            return directions.length();
        } else if (directions.indexOf('A') < directions.length() - 1) {
            long sum = 0;
            int start = 0;

            for (int i = 0; i < directions.length(); i++) {
                if (directions.charAt(i) == 'A') {
                    int end = i + 1;
                    sum += minCodeLength(directions.substring(start, end), dirKeypads);
                    start = end;
                }
            }

            return sum;
        } else {
            CacheKey key = new CacheKey(directions, dirKeypads);

            if (minCodeLengthCache.containsKey(key)) {
                return minCodeLengthCache.get(key);
            }

            long minCodeLength = Long.MAX_VALUE;

            for (String newDirections : directionCodes(directions, DIR_KEYPAD)) {
                long codeLength = minCodeLength(newDirections, dirKeypads - 1);

                if (codeLength < minCodeLength) {
                    minCodeLength = codeLength;
                }
            }

            minCodeLengthCache.put(key, minCodeLength);
            return minCodeLength;
        }
    }

    private static List<String> directionCodes(String code, Map<Character, Pos> keypad) {
        List<String> directionCodes = new ArrayList<>();
        directionCodes.add("");
        char start = 'A';

        for (char end : code.toCharArray()) {
            List<String> newDirectionCodes = new ArrayList<>();

            for (String path : shortestPaths(keypad.get(start), keypad.get(end), keypad.values())) {
                for (String directionCode : directionCodes) {
                    newDirectionCodes.add(directionCode + path + "A");
                }
            }

            directionCodes = newDirectionCodes;
            start = end;
        }

        return directionCodes;
    }

    private static List<String> shortestPaths(Pos start, Pos end, Collection<Pos> validPositions) {
        record State(Pos pos, String path) {}
        List<String> paths = new ArrayList<>();
        List<State> queue = new ArrayList<>();
        queue.add(new State(start, ""));

        while (!queue.isEmpty()) {
            State state = queue.removeFirst();

            if (!paths.isEmpty() && paths.getFirst().length() < state.path().length()) {
                break;
            }

            if (state.pos().equals(end)) {
                paths.add(state.path());
                continue;
            }

            for (Direction direction : Direction.values()) {
                Pos n = direction.move(state.pos());

                if (validPositions.contains(n)) {
                    queue.add(new State(n, state.path() + direction.toChar()));
                }
            }
        }

        return paths;
    }

    private static final Map<Character, Pos> DIR_KEYPAD = Map.ofEntries(
            Map.entry('^', new Pos(1, 0)),
            Map.entry('A', new Pos(2, 0)),
            Map.entry('<', new Pos(0, 1)),
            Map.entry('v', new Pos(1, 1)),
            Map.entry('>', new Pos(2, 1))
    );

    private static final Map<Character, Pos> NUM_KEYPAD = Map.ofEntries(
            Map.entry('7', new Pos(0, 0)),
            Map.entry('8', new Pos(1, 0)),
            Map.entry('9', new Pos(2, 0)),
            Map.entry('4', new Pos(0, 1)),
            Map.entry('5', new Pos(1, 1)),
            Map.entry('6', new Pos(2, 1)),
            Map.entry('1', new Pos(0, 2)),
            Map.entry('2', new Pos(1, 2)),
            Map.entry('3', new Pos(2, 2)),
            Map.entry('0', new Pos(1, 3)),
            Map.entry('A', new Pos(2, 3))
    );

    record Pos(int x, int y) {}

    private enum Direction {
        UP, DOWN, LEFT, RIGHT;

        char toChar() {
            return switch (this) {
                case UP -> '^';
                case DOWN -> 'v';
                case LEFT -> '<';
                case RIGHT -> '>';
            };
        }

        Pos move(Pos pos) {
            return switch (this) {
                case UP -> new Pos(pos.x, pos.y - 1);
                case DOWN -> new Pos(pos.x, pos.y + 1);
                case LEFT -> new Pos(pos.x - 1, pos.y);
                case RIGHT -> new Pos(pos.x + 1, pos.y);
            };
        }
    }

    private record CacheKey(String directions, int dirKeypads) {}
}
