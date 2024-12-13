import java.util.ArrayList;
import java.util.List;

public class Day13 {
    private static final String TEST_INPUT = """
            Button A: X+94, Y+34
            Button B: X+22, Y+67
            Prize: X=8400, Y=5400
            
            Button A: X+26, Y+66
            Button B: X+67, Y+21
            Prize: X=12748, Y=12176
            
            Button A: X+17, Y+86
            Button B: X+84, Y+37
            Prize: X=7870, Y=6450
            
            Button A: X+69, Y+23
            Button B: X+27, Y+71
            Prize: X=18641, Y=10279
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(13);
        String realInput = AocUtils.download(13);

        for (String input : List.of(TEST_INPUT, realInput)) {
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        List<Machine> machines = parse(input, 0L);
        int tokens = 0;

        for (var m : machines) {
            int best = Integer.MAX_VALUE;

            for (int a = 0; a <= 100; a++) {
                for (int b = 0; b <= 100; b++) {
                    if (m.aX * a + m.bX * b == m.prizeX && m.aY * a + m.bY * b == m.prizeY) {
                        int score = 3 * a + b;

                        if (score < best) {
                            best = score;
                        }
                    }
                }
            }

            if (best < Integer.MAX_VALUE) {
                tokens += best;
            }
        }

        System.out.println("Part I: " + tokens);
    }

    private static void partII(String input) {
        List<Machine> machines = parse(input, 10000000000000L);
        long tokens = 0;

        for (var m : machines) {
            long best = Long.MAX_VALUE;
            double ratio = (m.aY - m.aX) / (double) (m.bX - m.bY);
            double xPerCycle = m.aX + m.bX * ratio;
            double yPerCycle = m.aY + m.bY * ratio;
            long baseAPushes = Math.min((long) (m.prizeX / xPerCycle), (long) (m.prizeY / yPerCycle));
            long baseBPushes = (long) (baseAPushes * ratio);

            for (int extraAPushes = -1000; extraAPushes <= 1000; extraAPushes++) {
                long aPushes = baseAPushes + extraAPushes;

                for (int extraBPushes = -1000; extraBPushes <= 1000; extraBPushes++) {
                    long bPushes = baseBPushes + extraBPushes;

                    long x = aPushes * m.aX + bPushes * m.bX;
                    long y = aPushes * m.aY + bPushes * m.bY;

                    if (x == m.prizeX && y == m.prizeY) {
                        long score = 3L * aPushes + bPushes;

                        if (score < best) {
                            best = score;
                        }
                    }
                }
            }

            if (best < Long.MAX_VALUE) {
                tokens += best;
            }
        }

        System.out.println("Part II: " + tokens);
    }

    private static List<Machine> parse(String input, long prizeOffset) {
        List<Machine> machines = new ArrayList<>();
        List<String> lines = input.lines().toList();
        int offset = 0;

        while (offset + 2 < lines.size()) {
            String lineA = lines.get(offset);
            String lineB = lines.get(offset + 1);
            String lineP = lines.get(offset + 2);

            int aX = Integer.parseInt(lineA.substring(lineA.indexOf("+") + 1, lineA.indexOf(",")));
            int aY = Integer.parseInt(lineA.substring(lineA.lastIndexOf("+") + 1));

            int bX = Integer.parseInt(lineB.substring(lineB.indexOf("+") + 1, lineB.indexOf(",")));
            int bY = Integer.parseInt(lineB.substring(lineB.lastIndexOf("+") + 1));

            int prizeX = Integer.parseInt(lineP.substring(lineP.indexOf("=") + 1, lineP.indexOf(",")));
            int prizeY = Integer.parseInt(lineP.substring(lineP.lastIndexOf("=") + 1));

            machines.add(new Machine(aX, aY, bX, bY, prizeX + prizeOffset, prizeY + prizeOffset));
            offset += 4;
        }

        return machines;
    }

    record Machine(long aX, long aY, long bX, long bY, long prizeX, long prizeY) {}
}
