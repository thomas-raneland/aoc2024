import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day08 {
    private static final String INPUT = """
            ............
            ........0...
            .....0......
            .......0....
            ....0.......
            ......A.....
            ............
            ............
            ........A...
            .........A..
            ............
            ............
            """;

    public static void main(String... args) {
        partI();
        partII();
    }

    private static void partI() {
        Grid grid = parse();
        Set<Pos> antinodes = new HashSet<>();

        grid.antennas().forEach((pos1, c1) -> grid.antennas().forEach((pos2, c2) -> {
            if (!pos1.equals(pos2) && c1.equals(c2)) {
                int dx = pos1.x - pos2.x;
                int dy = pos1.y - pos2.y;
                Pos antinode = pos1.move(dx, dy);

                if (grid.isInside(antinode)) {
                    antinodes.add(antinode);
                }
            }
        }));

        System.out.println("Part I: " + antinodes.size());
    }

    private static void partII() {
        Grid grid = parse();
        Set<Pos> antinodes = new HashSet<>();

        grid.antennas().forEach((pos1, c1) -> grid.antennas().forEach((pos2, c2) -> {
            if (!pos1.equals(pos2) && c1.equals(c2)) {
                int dx = pos1.x - pos2.x;
                int dy = pos1.y - pos2.y;
                Pos antinode = pos1;

                while (grid.isInside(antinode)) {
                    antinodes.add(antinode);
                    antinode = antinode.move(dx, dy);
                }
            }
        }));

        System.out.println("Part II: " + antinodes.size());
    }

    private static Grid parse() {
        List<String> lines = INPUT.lines().toList();
        int width = lines.getFirst().length();
        int height = lines.size();
        Map<Pos, Character> antennas = new HashMap<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = lines.get(y).charAt(x);

                if (c != '.') {
                    antennas.put(new Pos(x, y), c);
                }
            }
        }

        return new Grid(width, height, antennas);
    }

    private record Grid(int width, int height, Map<Pos, Character> antennas) {
        boolean isInside(Pos pos) {
            return pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height;
        }
    }

    private record Pos(int x, int y) {
        Pos move(int dx, int dy) {
            return new Pos(x + dx, y + dy);
        }
    }
}
