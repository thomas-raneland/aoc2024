import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Day02 {
    private static final String INPUT = """
            7 6 4 2 1
            1 2 7 8 9
            9 7 6 2 1
            1 3 2 4 5
            8 6 4 4 1
            1 3 6 7 9""";

    public static void main(String... args) {
        partI();
        partII();
    }

    private static void partI() {
        int safeReports = 0;

        for (String report : INPUT.lines().toList()) {
            List<Integer> levels = Stream.of(report.split("\\s+")).map(Integer::parseInt).toList();

            if (isSafe(levels)) {
                safeReports++;
            }
        }

        System.out.println("Part I: " + safeReports);
    }

    private static boolean isSafe(List<Integer> levels) {
        Boolean inc = null;

        for (int i = 1; i < levels.size(); i++) {
            int diff = levels.get(i) - levels.get(i - 1);

            if (diff == 0 || Math.abs(diff) > 3) {
                return false;
            }

            if (inc == null) {
                inc = diff > 0;
            } else if (inc != diff > 0) {
                return false;
            }
        }

        return true;
    }

    private static void partII() {
        int safeReports = 0;

        for (String report : INPUT.lines().toList()) {
            List<Integer> levels = Arrays.stream(report.split("\\s+"))
                                         .map(Integer::parseInt)
                                         .toList();

            for (int skipPosition = 0; skipPosition < levels.size(); skipPosition++) {
                List<Integer> reducedLevels = new ArrayList<>(levels);
                //noinspection SuspiciousListRemoveInLoop
                reducedLevels.remove(skipPosition);

                if (isSafe(reducedLevels)) {
                    safeReports++;
                    break;
                }
            }
        }

        System.out.println("Part II: " + safeReports);
    }
}
