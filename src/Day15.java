import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;
import javax.swing.JFrame;

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

        Visualization.create(parse(realInput).widen()).animate();
    }

    private static void partI(String input) {
        Parsed parsed = parse(input);
        Map<Pos, Item> map = parsed.map();
        List<Direction> moves = parsed.moves();
        Pos robot = parsed.robot();

        for (Direction direction : moves) {
            robot = move(direction, map, robot);
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
            robot = move(direction, map, robot);
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

    private static Pos move(Direction direction, Map<Pos, Item> map, Pos robot) {
        Map<Pos, Item> toMove = new HashMap<>();

        if (canMove(map, robot, direction, toMove)) {
            toMove.forEach(map::remove);
            toMove.forEach((key, value) -> {
                if (value != null) {
                    map.put(key.neighbor(direction), value);
                } else {
                    map.remove(key.neighbor(direction));
                }
            });

            return robot.neighbor(direction);
        }

        return robot;
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

    private enum Item {
        EMPTY, WALL, BOX, LARGE_BOX_LEFT, LARGE_BOX_RIGHT
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    ///////////////////// VISUALIZATION /////////////////////

    private record Visualization(JComponent comp, AtomicReference<Pos> robot, Map<Pos, Item> map, List<Direction> moves) {
        private static final int CELL_H = 20;
        private static final int CELL_W = 10;
        private static final int CELL_R = 5;

        static Visualization create(Parsed parsed) {
            AtomicReference<Pos> robot = new AtomicReference<>(parsed.robot);

            var comp = new JComponent() {
                {
                    setPreferredSize(new Dimension(
                            (parsed.map.keySet().stream().mapToInt(Pos::x).max().orElseThrow() + 1) * CELL_W,
                            (parsed.map.keySet().stream().mapToInt(Pos::x).max().orElseThrow() + 1) * CELL_H));
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    g.setColor(Color.decode("#c2c2d6"));
                    g.fillRect(0, 0, getWidth(), getHeight());

                    synchronized (parsed.map) {
                        for (Pos pos : parsed.map().keySet()) {
                            switch (parsed.map().getOrDefault(pos, Item.EMPTY)) {
                                case WALL -> draw(g, new Rectangle2D.Double(pos.x * CELL_W, pos.y * CELL_H, CELL_W, CELL_H),
                                        Color.decode("#33334d"));
                                case BOX -> draw(g, new RoundRectangle2D.Double(pos.x * CELL_W, pos.y * CELL_H, CELL_W, CELL_H, CELL_R, CELL_R),
                                        Color.decode("#862d2d"));
                                case LARGE_BOX_LEFT -> draw(g, leftRounded(pos.x * CELL_W, pos.y * CELL_H),
                                        Color.decode("#862d2d"));
                                case LARGE_BOX_RIGHT -> draw(g, rightRounded(pos.x * CELL_W, pos.y * CELL_H),
                                        Color.decode("#732626"));
                            }
                        }
                    }

                    Pos rPos = robot.get();
                    draw(g, new RoundRectangle2D.Double(rPos.x * CELL_W, rPos.y * CELL_H, CELL_W, CELL_H, CELL_R, CELL_R),
                            Color.decode("#00cc44"));
                }

                private static void draw(Graphics g, Shape shape, Color color) {
                    g.setColor(color);
                    ((Graphics2D) g).fill(shape);
                }

                private static Shape leftRounded(double x, double y) {
                    GeneralPath path = new GeneralPath();
                    path.moveTo(x + CELL_R, y);
                    path.lineTo(x + CELL_W, y);
                    path.lineTo(x + CELL_W, y + CELL_H);
                    path.lineTo(x + CELL_R, y + CELL_H);
                    path.quadTo(x, y + CELL_H, x, y + CELL_H - CELL_R);
                    path.lineTo(x, y + CELL_R);
                    path.quadTo(x, y, x + CELL_R, y);
                    path.closePath();
                    return path;
                }

                private static Shape rightRounded(double x, double y) {
                    GeneralPath path = new GeneralPath();
                    path.moveTo(x, y);
                    path.lineTo(x + CELL_W - CELL_R, y);
                    path.quadTo(x + CELL_W, y, x + CELL_W, y + CELL_R);
                    path.lineTo(x + CELL_W, y + CELL_H - CELL_R);
                    path.quadTo(x + CELL_W, y + CELL_H, x + CELL_W - CELL_R, y + CELL_H);
                    path.lineTo(x, y + CELL_H);
                    path.closePath();
                    return path;
                }
            };

            JFrame frame = new JFrame("Day 15");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(comp);
            frame.pack();
            frame.setVisible(true);
            return new Visualization(comp, robot, parsed.map, parsed.moves);
        }

        public void animate() {
            for (Direction direction : moves) {
                synchronized (map) {
                    robot.set(move(direction, map, robot.get()));
                }

                comp.invalidate();
                comp.validate();
                comp.repaint();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
