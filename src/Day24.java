import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Day24 {
    private static final String TEST_INPUT1 = """
            x00: 1
            x01: 0
            x02: 1
            x03: 1
            x04: 0
            y00: 1
            y01: 1
            y02: 1
            y03: 1
            y04: 1
            
            ntg XOR fgs -> mjb
            y02 OR x01 -> tnw
            kwq OR kpj -> z05
            x00 OR x03 -> fst
            tgd XOR rvg -> z01
            vdt OR tnw -> bfw
            bfw AND frj -> z10
            ffh OR nrd -> bqk
            y00 AND y03 -> djm
            y03 OR y00 -> psh
            bqk OR frj -> z08
            tnw OR fst -> frj
            gnj AND tgd -> z11
            bfw XOR mjb -> z00
            x03 OR x00 -> vdt
            gnj AND wpb -> z02
            x04 AND y00 -> kjc
            djm OR pbm -> qhw
            nrd AND vdt -> hwm
            kjc AND fst -> rvg
            y04 OR y02 -> fgs
            y01 AND x02 -> pbm
            ntg OR kjc -> kwq
            psh XOR fgs -> tgd
            qhw XOR tgd -> z09
            pbm OR djm -> kpj
            x03 XOR y03 -> ffh
            x00 XOR y04 -> ntg
            bfw OR bqk -> z06
            nrd XOR fgs -> wpb
            frj XOR qhw -> z04
            bqk OR frj -> z07
            y03 OR x01 -> nrd
            hwm AND bqk -> z03
            tgd XOR rvg -> z12
            tnw OR pbm -> gnj
            """;

    private static final String TEST_INPUT2 = """
            x00: 0
            x01: 1
            x02: 0
            x03: 1
            x04: 0
            x05: 1
            y00: 0
            y01: 0
            y02: 1
            y03: 1
            y04: 0
            y05: 1
            
            x00 AND y00 -> z05
            x01 AND y01 -> z02
            x02 AND y02 -> z01
            x03 AND y03 -> z03
            x04 AND y04 -> z04
            x05 AND y05 -> z00
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(24);
        String realInput = AocUtils.download(24);
        partI(TEST_INPUT1);
        partII(TEST_INPUT2, Formula.sixBitAnd());
        System.out.println();
        partI(realInput);
        partII(realInput, Formula.plus());
    }

    private static void partI(String input) {
        String[] parts = input.split("\n\n");
        Input deviceInput = Input.parse(parts[0]);
        Device device = Device.parse(parts[1]);
        long output = device.output(deviceInput);
        System.out.println("Part I: " + output);
    }

    private static void partII(String input, Map<String, Formula> expected) {
        String[] parts = input.split("\n\n");
        Device device = Device.parse(parts[1]);
        Set<String> swappedOuts = findOutsToSwap(device, expected);
        System.out.println("Part II: " + String.join(",", swappedOuts));
    }

    private static SortedSet<String> findOutsToSwap(Device device, Map<String, Formula> expected) {
        SortedSet<String> swappedOuts = new TreeSet<>();

        for (String out : device.externalOuts()) {
            Formula expectedFormula = expected.get(out);

            if (!device.formula(out).equals(expectedFormula)) {
                device = swapOuts(device, out, expectedFormula, swappedOuts);
            }
        }

        return swappedOuts;
    }

    private static Device swapOuts(Device device, String out, Formula expected, Set<String> swappedOuts) {
        Set<String> dependencies = device.dependencies(out);

        for (String oldOut : dependencies) {
            for (String newOut : device.gates().keySet()) {
                Device newDevice = device.swap(oldOut, newOut);

                if (!newDevice.isCircular(out, new HashSet<>()) && newDevice.formula(out).equals(expected)) {
                    swappedOuts.add(oldOut);
                    swappedOuts.add(newOut);
                    return newDevice;
                }
            }
        }

        throw new IllegalStateException("No outs to swap found");
    }

    private enum Operator {
        AND, OR, XOR;

        public int value(int a, int b) {
            return switch (this) {
                case AND -> a == 1 && b == 1 ? 1 : 0;
                case OR -> a == 1 || b == 1 ? 1 : 0;
                case XOR -> a != b ? 1 : 0;
            };
        }
    }

    private record Gate(Operator op, String a, String b, String out) {
        static Gate parse(String line) {
            String[] parts = line.split(" ");
            String op = parts[1];
            return new Gate(Operator.valueOf(op), parts[0], parts[2], parts[4]);
        }
    }

    private record Device(Map<String, Gate> gates) {
        static Device parse(String definition) {
            Map<String, Gate> gates = new HashMap<>();

            for (String line : definition.lines().toList()) {
                Gate gate = Gate.parse(line);
                gates.put(gate.out, gate);
            }

            return new Device(gates);
        }

        List<String> externalOuts() {
            return gates().values().stream().map(Gate::out).filter(out -> out.startsWith("z")).toList();
        }

        long output(Input input) {
            String zString = externalOuts().stream().sorted(Comparator.reverseOrder()).map(
                    out -> String.valueOf(compute(out, input))).collect(Collectors.joining());

            return Long.parseLong(zString, 2);
        }

        private int compute(String name, Input input) {
            if (name.startsWith("x") || name.startsWith("y")) {
                return input.values().getOrDefault(name, 0);
            }

            Gate gate = gates.get(name);
            return gate.op().value(compute(gate.a(), input), compute(gate.b(), input));
        }

        /**
         * Includes the gate itself
         */
        Set<String> dependencies(String out) {
            LinkedHashSet<String> dependencies = new LinkedHashSet<>();
            Gate gate = gates.get(out);

            if (gate != null) {
                dependencies.add(out);
                dependencies.addAll(dependencies(gate.a()));
                dependencies.addAll(dependencies(gate.b()));
            }

            return dependencies;
        }

        Device swap(String lhs, String rhs) {
            Map<String, Gate> swapped = new HashMap<>();

            gates.forEach((name, gate) -> {
                if (name.equals(lhs)) {
                    swapped.put(rhs, new Gate(gate.op(), gate.a(), gate.b(), rhs));
                } else if (name.equals(rhs)) {
                    swapped.put(lhs, new Gate(gate.op(), gate.a(), gate.b(), lhs));
                } else {
                    swapped.put(name, gate);
                }
            });

            return new Device(swapped);
        }

        private boolean isCircular(String out, Set<String> visited) {
            Gate gate = gates.get(out);

            if (gate == null) {
                return false;
            } else if (!visited.add(out)) {
                return true;
            }

            return isCircular(gate.a(), new HashSet<>(visited)) || isCircular(gate.b(), new HashSet<>(visited));
        }

        Formula formula(String out) {
            Gate g = gates.get(out);

            if (g == null) {
                return Formula.variable(out);
            } else {
                return Formula.operation(g.op(), formula(g.a()), formula(g.b()));
            }
        }
    }

    private record Input(Map<String, Integer> values) {
        static Input parse(String definition) {
            Map<String, Integer> values = new HashMap<>();

            for (String line : definition.lines().toList()) {
                String[] kv = line.split(": ");
                values.put(kv[0], Integer.parseInt(kv[1]));
            }

            return new Input(values);
        }
    }

    private interface Formula {
        static Formula variable(String name) {
            record Variable(String name) implements Formula {}
            return new Variable(name);
        }

        static Formula operation(Operator operator, Formula a, Formula b) {
            record Operation(Operator operator, Set<Formula> operands) implements Formula {}
            return new Operation(operator, Set.of(a, b));
        }

        static Map<String, Formula> sixBitAnd() {
            Map<String, Formula> sixBitAnd = new LinkedHashMap<>();
            sixBitAnd.put("z00", and(variable("x00"), variable("y00")));
            sixBitAnd.put("z01", and(variable("x01"), variable("y01")));
            sixBitAnd.put("z02", and(variable("x02"), variable("y02")));
            sixBitAnd.put("z03", and(variable("x03"), variable("y03")));
            sixBitAnd.put("z04", and(variable("x04"), variable("y04")));
            sixBitAnd.put("z05", and(variable("x05"), variable("y05")));
            return sixBitAnd;
        }

        static Map<String, Formula> plus() {
            Map<String, Formula> plus = new LinkedHashMap<>();

            var x = variable("x00");
            var y = variable("y00");
            plus.put("z00", xor(x, y));
            var and = and(x, y);
            var prevX = x;
            var prevY = y;

            x = variable("x01");
            y = variable("y01");
            var a = xor(x, y);
            var b = and;
            plus.put("z01", xor(a, b));
            and = and(a, b);
            prevX = x;
            prevY = y;

            for (int i = 2; i < 45; i++) {
                String suffix = new DecimalFormat("00").format(i);
                x = variable("x" + suffix);
                y = variable("y" + suffix);
                a = xor(x, y);
                b = or(and, and(prevX, prevY));
                plus.put("z" + suffix, xor(a, b));
                and = and(a, b);
                prevX = x;
                prevY = y;
            }

            plus.put("z45", or(and(prevX, prevY), and));
            return plus;
        }

        private static Formula xor(Formula a, Formula b) {
            return operation(Operator.XOR, a, b);
        }

        private static Formula and(Formula a, Formula b) {
            return operation(Operator.AND, a, b);
        }

        private static Formula or(Formula a, Formula b) {
            return operation(Operator.OR, a, b);
        }
    }
}
