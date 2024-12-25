import java.util.ArrayList;
import java.util.List;

public class Day25 {
    private static final String TEST_INPUT = """
            #####
            .####
            .####
            .####
            .#.#.
            .#...
            .....
                        
            #####
            ##.##
            .#.##
            ...##
            ...#.
            ...#.
            .....
                        
            .....
            #....
            #....
            #...#
            #.#.#
            #.###
            #####
                        
            .....
            .....
            #.#..
            ###..
            ###.#
            ###.#
            #####
                        
            .....
            .....
            .....
            #....
            #.#..
            #.#.#
            #####
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(25);
        String realInput = AocUtils.download(25);
        partI(TEST_INPUT);
        partI(realInput);
    }

    private static void partI(String input) {
        List<int[]> keys = new ArrayList<>();
        List<int[]> locks = new ArrayList<>();

        for (String part : input.split("\n\n")) {
            List<String> lines = part.lines().toList();
            int[] cols = new int[5];

            for (int i = 1; i < 6; i++) {
                for (int c = 0; c < 5; c++) {
                    if (lines.get(i).charAt(c) == '#') {
                        cols[c]++;
                    }
                }
            }

            (lines.getFirst().startsWith("#") ? locks : keys).add(cols);
        }

        long res = 0;

        for (int[] key : keys) {
            for (int[] lock : locks) {
                if (fits(key, lock)) {
                    res++;
                }
            }
        }

        System.out.println("Part I: " + res);
    }

    private static boolean fits(int[] key, int[] lock) {
        for (int i = 0; i < 5; i++) {
            if (key[i] + lock[i] > 5) {
                return false;
            }
        }

        return true;
    }
}
