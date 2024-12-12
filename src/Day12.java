import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Day12 {
    private static final String TEST_INPUT = """
            RRRRIICCFF
            RRRRIICCCF
            VVRRRCCFFF
            VVRCCCJFFF
            VVVVCJJCFE
            VVIVCCJJEE
            VVIIICJJEE
            MIIIIIJJEE
            MIIISIJEEE
            MMMISSJEEE
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(12);
        String realInput = AocUtils.download(12);

        for (String input : List.of(TEST_INPUT, realInput)) {
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        List<Region> regions = parse(input);
        int res = regions.stream().mapToInt(r -> r.area() * r.perimeter()).sum();
        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        List<Region> regions = parse(input);
        int res = regions.stream().mapToInt(r -> r.area() * r.sides()).sum();
        System.out.println("Part II: " + res);
    }

    private static List<Region> parse(String input) {
        Map<Character, Set<Pos>> regions = new HashMap<>();
        int y = 0;

        for (String line : input.split("\n")) {
            for (int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);
                regions.computeIfAbsent(c, k -> new HashSet<>()).add(new Pos(x, y));
            }

            y++;
        }

        return regions.entrySet().stream().map(e -> new Region(e.getKey(), e.getValue())).flatMap(Region::split).toList();
    }

    private record Region(Character c, Set<Pos> points) {
        Stream<Region> split() {
            Set<Region> regions = new HashSet<>();
            Set<Pos> visited = new HashSet<>();

            for (Pos p : points) {
                if (visited.contains(p)) {
                    continue;
                }

                Set<Pos> region = new HashSet<>();
                Set<Pos> toVisit = new HashSet<>();
                toVisit.add(p);

                while (!toVisit.isEmpty()) {
                    Pos current = toVisit.iterator().next();
                    toVisit.remove(current);

                    if (visited.contains(current)) {
                        continue;
                    }

                    visited.add(current);
                    region.add(current);

                    for (Pos n : current.neighbors()) {
                        if (points.contains(n)) {
                            toVisit.add(n);
                        }
                    }
                }

                regions.add(new Region(c, region));
            }

            return regions.stream();
        }

        int area() {
            return points.size();
        }

        int sides() {
            Set<Side> all = new HashSet<>();

            for (Pos p : points) {
                List<Side> pSides = List.of(
                        new Side(p.x, p.x + 1, p.y, p.y),
                        new Side(p.x, p.x + 1, p.y + 1, p.y + 1),
                        new Side(p.x, p.x, p.y, p.y + 1),
                        new Side(p.x + 1, p.x + 1, p.y, p.y + 1));

                for (Side s : pSides) {
                    if (!all.contains(s)) {
                        all.add(s);
                    } else {
                        all.remove(s);
                    }
                }
            }

            boolean found;

            do {
                found = false;

                for (Side s : all) {
                    for (Side r : all) {
                        if (s.equals(r)) {
                            continue;
                        }

                        if (r.isAdjacent(s) && inSide(r) == inSide(s)) {
                            all.remove(s);
                            all.remove(r);
                            all.add(s.combine(r));
                            found = true;
                            break;
                        }
                    }

                    if (found) {
                        break;
                    }
                }
            } while (found);

            return all.size();
        }

        private int inSide(Side side) {
            if (points.contains(new Pos(side.x1(), side.y1()))) {
                return 1;
            } else {
                return -1;
            }
        }

        int perimeter() {
            int total = 0;

            for (Pos p : points) {
                for (Pos n : p.neighbors()) {
                    if (!points.contains(n)) {
                        total++;
                    }
                }
            }

            return total;
        }
    }

    private record Pos(int x, int y) {
        List<Pos> neighbors() {
            return List.of(new Pos(x - 1, y), new Pos(x + 1, y), new Pos(x, y - 1), new Pos(x, y + 1));
        }
    }

    private record Side(int x1, int x2, int y1, int y2) {
        boolean isAdjacent(Side that) {
            if (y1 == y2) {
                return that.y1 == that.y2 && this.y1 == that.y1 && (this.x1() == that.x2() || this.x2() == that.x1());
            } else {
                return that.x1 == that.x2 && this.x1 == that.x1 && (this.y1() == that.y2() || this.y2() == that.y1());
            }
        }

        Side combine(Side that) {
            return new Side(Math.min(x1, that.x1), Math.max(x2, that.x2), Math.min(y1, that.y1), Math.max(y2, that.y2));
        }
    }
}
