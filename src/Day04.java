public class Day04 {
    private static final String INPUT = """
            MMMSXXMASM
            MSAMXMSMSA
            AMXSXMAAMM
            MSAMASMSMX
            XMASAMXAMM
            XXAMMXXAMA
            SMSMSASXSS
            SAXAMASAAA
            MAMMMXMMMM
            MXMXAXMASX""";

    public static void main(String... args) {
        partI();
        partII();
    }

    private static void partI() {
        char[][] grid = INPUT.lines().map(String::toCharArray).toArray(char[][]::new);
        String s = "XMAS";
        int count = 0;

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                count += matchDown(grid, row, col, s) +
                         matchRight(grid, row, col, s) +
                         matchDownRight(grid, row, col, s) +
                         matchDownLeft(grid, row, col, s);
            }
        }

        System.out.println("Part I: " + count);
    }

    private static void partII() {
        char[][] grid = INPUT.lines().map(String::toCharArray).toArray(char[][]::new);
        String s = "MAS";
        int count = 0;

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                count += Math.min(matchDownRight(grid, row, col, s), matchDownLeft(grid, row, col + s.length() - 1, s));
            }
        }

        System.out.println("Part II: " + count);
    }

    private static int matchDownLeft(char[][] grid, int row, int col, String s) {
        return match(grid, row, col, 1, -1, s) +
               match(grid, row, col, 1, -1, reverse(s));
    }

    private static int matchDownRight(char[][] grid, int row, int col, String s) {
        return match(grid, row, col, 1, 1, s) +
               match(grid, row, col, 1, 1, reverse(s));
    }

    private static int matchRight(char[][] grid, int row, int col, @SuppressWarnings("SameParameterValue") String s) {
        return match(grid, row, col, 0, 1, s) +
               match(grid, row, col, 0, 1, reverse(s));
    }

    private static int matchDown(char[][] grid, int row, int col, @SuppressWarnings("SameParameterValue") String s) {
        return match(grid, row, col, 1, 0, s) +
               match(grid, row, col, 1, 0, reverse(s));
    }

    private static int match(char[][] grid, int row, int col, int dRow, int dCol, String s) {
        for (int k = 0; k < s.length(); k++) {
            int rowK = row + k * dRow;
            int colK = col + k * dCol;

            if (rowK < 0 || rowK >= grid.length || colK < 0 || colK >= grid.length || grid[rowK][colK] != s.charAt(k)) {
                return 0;
            }
        }

        return 1;
    }

    private static String reverse(String xmas) {
        return new StringBuilder(xmas).reverse().toString();
    }
}
