import java.util.Arrays;
import java.util.List;

public class Day14 {
    private static final String TEST_INPUT = """
            p=0,4 v=3,-3
            p=6,3 v=-1,-3
            p=10,3 v=-1,2
            p=2,0 v=2,-1
            p=0,0 v=1,3
            p=3,0 v=-2,-2
            p=7,6 v=-1,-3
            p=3,0 v=-1,-2
            p=9,3 v=2,3
            p=7,3 v=-1,2
            p=2,4 v=2,-3
            p=9,5 v=-3,-3
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(14);
        String realInput = AocUtils.download(14);
        partI(TEST_INPUT, 11, 7);
        partI(realInput, 101, 103);
        partII(realInput, 101, 103);
    }

    private static void partI(String input, int width, int height) {
        List<Robot> robots = parse(input);

        for (int i = 0; i < 100; i++) {
            robots = robots.stream().map(robot -> robot.move(width, height)).toList();
        }

        long q1 = robots.stream().filter(robot -> robot.x < (width - 1) / 2 && robot.y < (height - 1) / 2).count();
        long q2 = robots.stream().filter(robot -> robot.x > (width - 1) / 2 && robot.y < (height - 1) / 2).count();
        long q3 = robots.stream().filter(robot -> robot.x < (width - 1) / 2 && robot.y > (height - 1) / 2).count();
        long q4 = robots.stream().filter(robot -> robot.x > (width - 1) / 2 && robot.y > (height - 1) / 2).count();
        System.out.println("Part I: " + q1 * q2 * q3 * q4);
    }

    @SuppressWarnings("SameParameterValue")
    private static void partII(String input, int width, int height) {
        int seconds = 0;
        List<Robot> robots = parse(input);

        while (!hasPictureFrame(robots, width, height)) {
            robots = robots.stream().map(robot -> robot.move(width, height)).toList();
            seconds++;
        }

        draw(robots, width, height);
        System.out.println("Part II: " + seconds);
    }

    private static List<Robot> parse(String input) {
        return input.lines()
                    .map(line -> Arrays.stream(line.split("[^\\d-]+")).skip(1).mapToInt(Integer::parseInt).toArray())
                    .map(numbers -> new Robot(numbers[0], numbers[1], numbers[2], numbers[3]))
                    .toList();
    }

    private static boolean hasPictureFrame(List<Robot> robots, int width, int height) {
        boolean[] frameTop = {true, true, true, true, true, true, true, true, true, true};
        boolean[][] grid = grid(robots, width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width - frameTop.length; x++) {
                if (Arrays.equals(grid[y], x, x + frameTop.length, frameTop, 0, frameTop.length)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void draw(List<Robot> robots, int width, int height) {
        boolean[][] grid = grid(robots, width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(grid[y][x] ? '#' : '.');
            }

            System.out.println();
        }
    }

    private static boolean[][] grid(List<Robot> robots, int width, int height) {
        boolean[][] grid = new boolean[height][width];

        for (Robot robot : robots) {
            grid[robot.y][robot.x] = true;
        }

        return grid;
    }

    private record Robot(int x, int y, int vx, int vy) {
        Robot move(int width, int height) {
            int nx = AocUtils.modulo(x + vx, width);
            int ny = AocUtils.modulo(y + vy, height);
            return new Robot(nx, ny, vx, vy);
        }
    }
}
